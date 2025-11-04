package com.mumu.woodlin.sql2api.spi.base;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.SchemaMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;

/**
 * MySQL兼容数据库的抽象提取器基类
 * 
 * @author mumu
 * @description 为MySQL兼容的数据库（如TiDB、MariaDB、DM8等）提供可继承的基础实现
 *              支持类型映射覆盖和版本特定处理
 * @since 2025-01-04
 */
@Slf4j
public abstract class AbstractMySQLCompatibleExtractor implements DatabaseMetadataExtractor {
    
    protected Map<String, String> typeMapping = new HashMap<>();
    protected Map<String, Map<String, String>> versionSpecificTypeMappings = new HashMap<>();
    
    public AbstractMySQLCompatibleExtractor() {
        initializeDefaultTypeMapping();
    }
    
    protected void initializeDefaultTypeMapping() {
        typeMapping.put("tinyint", "Byte");
        typeMapping.put("smallint", "Short");
        typeMapping.put("int", "Integer");
        typeMapping.put("bigint", "Long");
        typeMapping.put("float", "Float");
        typeMapping.put("double", "Double");
        typeMapping.put("decimal", "BigDecimal");
        typeMapping.put("varchar", "String");
        typeMapping.put("text", "String");
        typeMapping.put("date", "LocalDate");
        typeMapping.put("datetime", "LocalDateTime");
        typeMapping.put("timestamp", "LocalDateTime");
    }
    
    protected String getDatabaseVersion(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductVersion();
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = connection.getCatalog();
            
            DatabaseMetadata dbMetadata = DatabaseMetadata.builder()
                    .databaseName(databaseName)
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(supportsSchemas())
                    .build();
            
            extractCharsetInfo(connection, databaseName, dbMetadata);
            return dbMetadata;
        }
    }
    
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        // 默认实现，子类可覆盖
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        return new ArrayList<>();
    }
    
    @Override
    public List<TableMetadata> extractTables(Connection connection, String databaseName, String schemaName) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();
        String sql = getTablesQuery();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableMetadata table = buildTableMetadata(rs, databaseName);
                    table.setColumns(extractColumns(connection, databaseName, null, table.getTableName()));
                    table.setPrimaryKey(findPrimaryKey(connection, databaseName, table.getTableName()));
                    tables.add(table);
                }
            }
        }
        
        return tables;
    }
    
    protected String getTablesQuery() {
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE " +
               "FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";
    }
    
    protected TableMetadata buildTableMetadata(ResultSet rs, String databaseName) throws SQLException {
        return TableMetadata.builder()
                .tableName(rs.getString("TABLE_NAME"))
                .databaseName(databaseName)
                .comment(rs.getString("TABLE_COMMENT"))
                .tableType(rs.getString("TABLE_TYPE"))
                .build();
    }
    
    @Override
    public List<ColumnMetadata> extractColumns(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        String sql = getColumnsQuery();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                String version = getDatabaseVersion(connection);
                while (rs.next()) {
                    columns.add(buildColumnMetadata(rs, databaseName, tableName, version));
                }
            }
        }
        
        return columns;
    }
    
    protected String getColumnsQuery() {
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
    }
    
    protected ColumnMetadata buildColumnMetadata(ResultSet rs, String databaseName, String tableName, String version) throws SQLException {
        String dataType = rs.getString("DATA_TYPE");
        boolean isPrimaryKey = "PRI".equals(rs.getString("COLUMN_KEY"));
        String extra = rs.getString("EXTRA");
        boolean isAutoIncrement = extra != null && extra.toLowerCase().contains("auto_increment");
        
        return ColumnMetadata.builder()
                .columnName(rs.getString("COLUMN_NAME"))
                .tableName(tableName)
                .databaseName(databaseName)
                .comment(rs.getString("COLUMN_COMMENT"))
                .dataType(dataType)
                .nullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")))
                .defaultValue(rs.getString("COLUMN_DEFAULT"))
                .primaryKey(isPrimaryKey)
                .autoIncrement(isAutoIncrement)
                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                .javaType(mapToJavaType(dataType, version))
                .build();
    }
    
    @Override
    public String getTableComment(Connection connection, String databaseName, String schemaName, String tableName) throws SQLException {
        String sql = "SELECT TABLE_COMMENT FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TABLE_COMMENT");
                }
            }
        }
        return null;
    }
    
    protected String findPrimaryKey(Connection connection, String databaseName, String tableName) throws SQLException {
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = 'PRI' LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, databaseName);
            pstmt.setString(2, tableName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("COLUMN_NAME");
                }
            }
        }
        return null;
    }
    
    protected String mapToJavaType(String sqlType, String version) {
        if (sqlType == null) return "Object";
        String type = sqlType.toLowerCase();
        
        for (Map.Entry<String, Map<String, String>> entry : versionSpecificTypeMappings.entrySet()) {
            if (isVersionGreaterOrEqual(version, entry.getKey())) {
                String javaType = entry.getValue().get(type);
                if (javaType != null) return javaType;
            }
        }
        
        return typeMapping.getOrDefault(type, "Object");
    }
    
    protected boolean isVersionGreaterOrEqual(String currentVersion, String requiredVersion) {
        if (currentVersion == null || requiredVersion == null) return false;
        
        try {
            String[] current = currentVersion.split("\\.");
            String[] required = requiredVersion.split("\\.");
            
            for (int i = 0; i < Math.min(current.length, required.length); i++) {
                int c = Integer.parseInt(current[i].replaceAll("\\D.*", ""));
                int r = Integer.parseInt(required[i].replaceAll("\\D.*", ""));
                if (c > r) return true;
                if (c < r) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    protected boolean supportsSchemas() {
        return false;
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
}
