package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;

/**
 * 达梦数据库元数据提取器（Oracle兼容视图）
 * <p>
 * 达梦数据库（DM8）在 Oracle 兼容模式下提供 ALL_TAB_COMMENTS / ALL_COL_COMMENTS 等系统视图。
 * 为了提升注释、主键、自增等元数据完整性，这里使用显式 SQL，而不是完全依赖 JDBC REMARKS。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class DmMetadataExtractor extends OracleMetadataExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.DM;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("dm");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "dm.jdbc.driver.DmDriver";
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1 FROM DUAL";
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        List<SchemaMetadata> schemas = new ArrayList<>();
        String sql = "SELECT USERNAME FROM ALL_USERS ORDER BY USERNAME";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                schemas.add(SchemaMetadata.builder()
                        .schemaName(rs.getString("USERNAME"))
                        .databaseName(databaseName)
                        .build());
            }
        }
        return schemas;
    }

    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        String targetSchema = resolveSchema(connection, schemaName);
        Map<String, String> primaryKeyMap = loadPrimaryKeyMap(connection, targetSchema);
        String sql = "SELECT t.TABLE_NAME, tc.COMMENTS " +
                "FROM ALL_TABLES t " +
                "LEFT JOIN ALL_TAB_COMMENTS tc ON t.OWNER = tc.OWNER AND t.TABLE_NAME = tc.TABLE_NAME " +
                "WHERE t.OWNER = ? " +
                "ORDER BY t.TABLE_NAME";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(TableMetadata.builder()
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .comment(rs.getString("COMMENTS"))
                            .tableType("TABLE")
                            .primaryKey(primaryKeyMap.get(tableName))
                            .build());
                }
            }
        }
        return tables;
    }

    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName)
            throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        String targetSchema = resolveSchema(connection, schemaName);
        Set<String> primaryKeys = findPrimaryKeys(connection, targetSchema, tableName);
        String sql = "SELECT c.COLUMN_NAME, c.DATA_TYPE, c.DATA_LENGTH, c.DATA_PRECISION, c.DATA_SCALE, " +
                "c.NULLABLE, c.DATA_DEFAULT, c.COLUMN_ID, cc.COMMENTS, c.IDENTITY_COLUMN " +
                "FROM ALL_TAB_COLUMNS c " +
                "LEFT JOIN ALL_COL_COMMENTS cc ON c.OWNER = cc.OWNER AND c.TABLE_NAME = cc.TABLE_NAME " +
                "AND c.COLUMN_NAME = cc.COLUMN_NAME " +
                "WHERE c.OWNER = ? AND c.TABLE_NAME = ? " +
                "ORDER BY c.COLUMN_ID";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataType = rs.getString("DATA_TYPE");
                    Integer columnSize = getNumericOrNull(rs, "DATA_PRECISION");
                    if (columnSize == null) {
                        columnSize = getNumericOrNull(rs, "DATA_LENGTH");
                    }
                    columns.add(ColumnMetadata.builder()
                            .columnName(rs.getString("COLUMN_NAME"))
                            .tableName(tableName)
                            .schemaName(targetSchema)
                            .databaseName(databaseName)
                            .comment(rs.getString("COMMENTS"))
                            .dataType(dataType)
                            .columnSize(columnSize)
                            .decimalDigits(getNumericOrNull(rs, "DATA_SCALE"))
                            .nullable("Y".equalsIgnoreCase(rs.getString("NULLABLE")))
                            .defaultValue(trimDefault(rs.getString("DATA_DEFAULT")))
                            .primaryKey(primaryKeys.contains(rs.getString("COLUMN_NAME")))
                            .autoIncrement("YES".equalsIgnoreCase(rs.getString("IDENTITY_COLUMN")))
                            .ordinalPosition(getNumericOrNull(rs, "COLUMN_ID"))
                            .javaType(mapDmTypeToJavaType(dataType))
                            .build());
                }
            }
        }
        return columns;
    }

    @Override
    public String getTableComment(Connection connection, String databaseName, String schemaName, String tableName)
            throws SQLException {
        String targetSchema = resolveSchema(connection, schemaName);
        String sql = "SELECT COMMENTS FROM ALL_TAB_COMMENTS WHERE OWNER = ? AND TABLE_NAME = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetSchema);
            pstmt.setString(2, tableName.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("COMMENTS");
                }
            }
        }
        return null;
    }

    private String resolveSchema(Connection connection, String schemaName) throws SQLException {
        if (schemaName != null && !schemaName.isBlank()) {
            return schemaName.toUpperCase();
        }
        String user = connection.getMetaData().getUserName();
        return user == null ? null : user.toUpperCase();
    }

    private Set<String> findPrimaryKeys(Connection connection, String schemaName, String tableName) throws SQLException {
        Set<String> keys = new HashSet<>();
        String sql = "SELECT acc.COLUMN_NAME " +
                "FROM ALL_CONSTRAINTS ac " +
                "JOIN ALL_CONS_COLUMNS acc ON ac.OWNER = acc.OWNER AND ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME " +
                "WHERE ac.OWNER = ? AND ac.TABLE_NAME = ? AND ac.CONSTRAINT_TYPE = 'P'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schemaName);
            pstmt.setString(2, tableName.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("COLUMN_NAME"));
                }
            }
        }
        return keys;
    }

    private Map<String, String> loadPrimaryKeyMap(Connection connection, String schemaName) throws SQLException {
        HashMap<String, String> primaryKeyMap = new HashMap<>();
        String sql = "SELECT ac.TABLE_NAME, MIN(acc.COLUMN_NAME) AS PRIMARY_KEY " +
                "FROM ALL_CONSTRAINTS ac " +
                "JOIN ALL_CONS_COLUMNS acc ON ac.OWNER = acc.OWNER AND ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME " +
                "WHERE ac.OWNER = ? AND ac.CONSTRAINT_TYPE = 'P' " +
                "GROUP BY ac.TABLE_NAME";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schemaName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    primaryKeyMap.put(rs.getString("TABLE_NAME"), rs.getString("PRIMARY_KEY"));
                }
            }
        }
        return primaryKeyMap;
    }

    private Integer getNumericOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private String trimDefault(String value) {
        return value == null ? null : value.trim();
    }

    private String mapDmTypeToJavaType(String dataType) {
        if (dataType == null) {
            return "Object";
        }
        String type = dataType.toUpperCase();
        return switch (type) {
            case "NUMBER", "DECIMAL", "NUMERIC" -> "BigDecimal";
            case "TINYINT" -> "Byte";
            case "SMALLINT" -> "Short";
            case "INT", "INTEGER" -> "Integer";
            case "BIGINT" -> "Long";
            case "FLOAT", "BINARY_FLOAT" -> "Float";
            case "DOUBLE", "BINARY_DOUBLE" -> "Double";
            case "DATE" -> "LocalDateTime";
            case "TIMESTAMP", "TIMESTAMP WITH TIME ZONE", "TIMESTAMP WITH LOCAL TIME ZONE" -> "LocalDateTime";
            case "CHAR", "NCHAR", "VARCHAR", "VARCHAR2", "NVARCHAR", "NVARCHAR2", "CLOB", "NCLOB", "TEXT" -> "String";
            case "BLOB", "RAW", "LONG RAW" -> "byte[]";
            case "BIT", "BOOLEAN" -> "Boolean";
            default -> "Object";
        };
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
