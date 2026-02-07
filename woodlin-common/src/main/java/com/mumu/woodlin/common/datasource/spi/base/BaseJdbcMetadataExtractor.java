package com.mumu.woodlin.common.datasource.spi.base;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;

/**
 * JDBC元数据提取器基础抽象类
 * <p>
 * 提供80%通用的JDBC元数据提取逻辑，子类只需实现特定的数据库识别和驱动配置。
 * 使用标准JDBC DatabaseMetaData API提取数据库、表和列信息。
 * </p>
 * <p>
 * 子类需要实现：
 * <ul>
 *   <li>{@link #getDatabaseType()} - 返回数据库类型枚举</li>
 *   <li>{@link #supports(Connection)} - 判断是否支持该数据库连接</li>
 *   <li>{@link #getDefaultDriverClass()} - 返回JDBC驱动类名</li>
 * </ul>
 * </p>
 * <p>
 * 子类可选覆盖：
 * <ul>
 *   <li>{@link #supportsSchema()} - 是否支持Schema概念（默认true）</li>
 *   <li>{@link #supportsCatalog()} - 是否支持Catalog概念（默认false）</li>
 *   <li>{@link #getTableComment(Connection, String, String, String)} - 获取表注释</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public abstract class BaseJdbcMetadataExtractor implements DatabaseMetadataExtractor {
    
    /**
     * 是否支持Schema概念
     * <p>
     * Schema是数据库对象的逻辑分组，类似命名空间。
     * 例如：PostgreSQL、SQL Server支持Schema，MySQL不支持。
     * </p>
     * 
     * @return true表示支持Schema，false表示不支持
     */
    protected boolean supportsSchema() {
        return true;
    }
    
    /**
     * 是否支持Catalog概念
     * <p>
     * Catalog通常对应物理数据库实例或数据库。
     * 例如：MySQL使用Catalog表示数据库，PostgreSQL使用Schema。
     * </p>
     * 
     * @return true表示支持Catalog，false表示不支持
     */
    protected boolean supportsCatalog() {
        return false;
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            DatabaseMetadata db = new DatabaseMetadata();
            db.setDatabaseProductName(meta.getDatabaseProductName());
            db.setDatabaseProductVersion(meta.getDatabaseProductVersion());
            db.setDriverName(meta.getDriverName());
            db.setDriverVersion(meta.getDriverVersion());
            
            return db;
        }
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection conn, String databaseName) throws SQLException {
        if (!supportsSchema()) {
            return List.of();
        }
        
        List<SchemaMetadata> list = new ArrayList<>();
        try (ResultSet rs = conn.getMetaData().getSchemas()) {
            while (rs.next()) {
                SchemaMetadata schema = new SchemaMetadata();
                schema.setSchemaName(rs.getString("TABLE_SCHEM"));
                // TABLE_CATALOG might not exist in all databases
                try {
                    schema.setDatabaseName(rs.getString("TABLE_CATALOG"));
                } catch (SQLException e) {
                    // Ignore if TABLE_CATALOG doesn't exist
                }
                list.add(schema);
            }
        }
        return list;
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection conn, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();

        String catalog = supportsCatalog() ? databaseName : null;
        String schema = supportsSchema() ? schemaName : null;
        if (schema == null && supportsSchema()) {
            schema = safeSchema(conn);
        }

        try (ResultSet rs = conn.getMetaData().getTables(
                catalog, schema, "%", new String[]{"TABLE", "VIEW"})) {

            while (rs.next()) {
                TableMetadata table = new TableMetadata();
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setSchemaName(rs.getString("TABLE_SCHEM"));
                table.setDatabaseName(databaseName);
                table.setTableType(rs.getString("TABLE_TYPE"));

                // 尝试获取表注释（某些数据库在REMARKS字段提供）
                String remarks = rs.getString("REMARKS");
                if (remarks == null || remarks.isBlank()) {
                    remarks = getTableComment(conn, databaseName, table.getSchemaName(), table.getTableName());
                }
                table.setComment(remarks);

                tables.add(table);
            }
        }
        return tables;
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection conn, String databaseName, String schemaName, String tableName)
            throws SQLException {
        
        List<ColumnMetadata> columns = new ArrayList<>();
        
        String catalog = supportsCatalog() ? databaseName : null;
        String schema = supportsSchema() ? schemaName : null;
        if (schema == null && supportsSchema()) {
            schema = safeSchema(conn);
        }

        Set<String> primaryKeys = findPrimaryKeys(conn, catalog, schema, tableName);

        try (ResultSet rs = conn.getMetaData().getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                ColumnMetadata col = new ColumnMetadata();
                String columnName = rs.getString("COLUMN_NAME");
                col.setColumnName(columnName);
                col.setTableName(tableName);
                col.setSchemaName(schema);
                col.setDatabaseName(databaseName);
                col.setDataType(rs.getString("TYPE_NAME"));
                col.setJdbcType(rs.getInt("DATA_TYPE"));
                col.setColumnSize(getOptionalInteger(rs, "COLUMN_SIZE"));
                col.setDecimalDigits(getOptionalInteger(rs, "DECIMAL_DIGITS"));
                col.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                col.setDefaultValue(getOptionalString(rs, "COLUMN_DEF"));
                col.setOrdinalPosition(getOptionalInteger(rs, "ORDINAL_POSITION"));
                col.setAutoIncrement("YES".equalsIgnoreCase(getOptionalString(rs, "IS_AUTOINCREMENT")));
                col.setPrimaryKey(primaryKeys.contains(columnName));
                col.setJavaType(mapJdbcTypeToJavaType(col.getJdbcType(), col.getDataType()));

                // 尝试获取列注释（某些数据库在REMARKS字段提供）
                String remarks = rs.getString("REMARKS");
                if (remarks != null && !remarks.isEmpty()) {
                    col.setComment(remarks);
                }
                
                columns.add(col);
            }
        }
        return columns;
    }
    
    @Override
    public String getTableComment(Connection conn, String db, String schema, String table) throws SQLException {
        // 默认实现返回null，子类可覆盖以提供特定数据库的表注释获取逻辑
        return null;
    }

    protected Set<String> findPrimaryKeys(Connection conn, String catalog, String schema, String tableName) {
        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet pkRs = conn.getMetaData().getPrimaryKeys(catalog, schema, tableName)) {
            while (pkRs.next()) {
                String column = pkRs.getString("COLUMN_NAME");
                if (column != null && !column.isBlank()) {
                    primaryKeys.add(column);
                }
            }
        } catch (SQLException ignore) {
            // 部分驱动可能不支持，忽略后按非主键处理
        }
        return primaryKeys;
    }

    protected String safeSchema(Connection conn) {
        try {
            return conn.getSchema();
        } catch (Exception ignore) {
            return null;
        }
    }

    protected String getOptionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException ignore) {
            return null;
        }
    }

    protected Integer getOptionalInteger(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException ignore) {
            return null;
        }
    }

    protected String mapJdbcTypeToJavaType(Integer jdbcType, String sqlTypeName) {
        if (jdbcType == null) {
            return "Object";
        }
        return switch (jdbcType) {
            case java.sql.Types.TINYINT -> "Byte";
            case java.sql.Types.SMALLINT -> "Short";
            case java.sql.Types.INTEGER -> "Integer";
            case java.sql.Types.BIGINT -> "Long";
            case java.sql.Types.FLOAT, java.sql.Types.REAL -> "Float";
            case java.sql.Types.DOUBLE -> "Double";
            case java.sql.Types.NUMERIC, java.sql.Types.DECIMAL -> "BigDecimal";
            case java.sql.Types.BIT, java.sql.Types.BOOLEAN -> "Boolean";
            case java.sql.Types.CHAR, java.sql.Types.VARCHAR, java.sql.Types.LONGVARCHAR -> "String";
            case java.sql.Types.NCHAR, java.sql.Types.NVARCHAR, java.sql.Types.LONGNVARCHAR -> "String";
            case java.sql.Types.DATE -> "LocalDate";
            case java.sql.Types.TIME -> "LocalTime";
            case java.sql.Types.TIMESTAMP, java.sql.Types.TIMESTAMP_WITH_TIMEZONE -> "LocalDateTime";
            case java.sql.Types.BINARY, java.sql.Types.VARBINARY, java.sql.Types.LONGVARBINARY, java.sql.Types.BLOB -> "byte[]";
            default -> {
                if (sqlTypeName != null && sqlTypeName.toLowerCase().contains("json")) {
                    yield "String";
                }
                yield "Object";
            }
        };
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1";
    }
}
