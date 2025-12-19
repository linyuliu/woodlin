package com.mumu.woodlin.common.datasource.spi.base;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        
        try (ResultSet rs = conn.getMetaData().getTables(
                catalog, schema, "%", new String[]{"TABLE"})) {
            
            while (rs.next()) {
                TableMetadata table = new TableMetadata();
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setSchemaName(rs.getString("TABLE_SCHEM"));
                table.setTableType(rs.getString("TABLE_TYPE"));
                
                // 尝试获取表注释（某些数据库在REMARKS字段提供）
                String remarks = rs.getString("REMARKS");
                if (remarks != null && !remarks.isEmpty()) {
                    table.setComment(remarks);
                }
                
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
        
        try (ResultSet rs = conn.getMetaData().getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                ColumnMetadata col = new ColumnMetadata();
                col.setColumnName(rs.getString("COLUMN_NAME"));
                col.setDataType(rs.getString("TYPE_NAME"));
                col.setJdbcType(rs.getInt("DATA_TYPE"));
                col.setColumnSize(rs.getInt("COLUMN_SIZE"));
                col.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                
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
    
    @Override
    public int getPriority() {
        return 100;
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1";
    }
}
