package com.mumu.woodlin.common.datasource.spi.base;

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

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;

/**
 * MySQL兼容数据库的抽象提取器基类
 * <p>
 * 为MySQL兼容的数据库（如MySQL、MariaDB、TiDB、DM8等）提供可继承的基础实现。
 * 支持版本特定的类型映射和SQL差异处理。
 * </p>
 * <p>
 * 版本差异支持示例：
 * <ul>
 *   <li>MySQL 5.7 vs 8.0: DATETIME精度、JSON类型支持等</li>
 *   <li>MariaDB: SEQUENCE支持、JSON处理差异</li>
 *   <li>TiDB: 分布式特性相关元数据</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public abstract class AbstractMySQLCompatibleExtractor implements DatabaseMetadataExtractor {
    
    /** 基础类型映射 */
    protected Map<String, String> typeMapping = new HashMap<>();
    
    /** 版本特定的类型映射，key为最低版本号 */
    protected Map<String, Map<String, String>> versionSpecificTypeMappings = new HashMap<>();
    
    public AbstractMySQLCompatibleExtractor() {
        initializeDefaultTypeMapping();
        initializeVersionSpecificMappings();
    }
    
    /**
     * 初始化默认类型映射
     */
    protected void initializeDefaultTypeMapping() {
        // 数字类型
        typeMapping.put("tinyint", "Byte");
        typeMapping.put("smallint", "Short");
        typeMapping.put("mediumint", "Integer");
        typeMapping.put("int", "Integer");
        typeMapping.put("integer", "Integer");
        typeMapping.put("bigint", "Long");
        typeMapping.put("float", "Float");
        typeMapping.put("double", "Double");
        typeMapping.put("decimal", "BigDecimal");
        typeMapping.put("numeric", "BigDecimal");
        
        // 字符串类型
        typeMapping.put("char", "String");
        typeMapping.put("varchar", "String");
        typeMapping.put("tinytext", "String");
        typeMapping.put("text", "String");
        typeMapping.put("mediumtext", "String");
        typeMapping.put("longtext", "String");
        
        // 日期时间类型
        typeMapping.put("date", "LocalDate");
        typeMapping.put("time", "LocalTime");
        typeMapping.put("datetime", "LocalDateTime");
        typeMapping.put("timestamp", "LocalDateTime");
        typeMapping.put("year", "Integer");
        
        // 二进制类型
        typeMapping.put("binary", "byte[]");
        typeMapping.put("varbinary", "byte[]");
        typeMapping.put("tinyblob", "byte[]");
        typeMapping.put("blob", "byte[]");
        typeMapping.put("mediumblob", "byte[]");
        typeMapping.put("longblob", "byte[]");
        
        // 布尔类型
        typeMapping.put("bit", "Boolean");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("bool", "Boolean");
        
        // 枚举和集合
        typeMapping.put("enum", "String");
        typeMapping.put("set", "String");
    }
    
    /**
     * 初始化版本特定的类型映射
     * 子类可覆盖此方法添加版本特定映射
     */
    protected void initializeVersionSpecificMappings() {
        // MySQL 5.7+ JSON支持
        Map<String, String> mysql57Mappings = new HashMap<>();
        mysql57Mappings.put("json", "String");
        versionSpecificTypeMappings.put("5.7", mysql57Mappings);
        
        // MySQL 8.0+ 增强特性
        Map<String, String> mysql80Mappings = new HashMap<>();
        mysql80Mappings.put("json", "String");
        // 8.0支持更精确的datetime
        versionSpecificTypeMappings.put("8.0", mysql80Mappings);
    }
    
    /**
     * 获取数据库版本信息
     */
    protected String getDatabaseVersion(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductVersion();
    }
    
    /**
     * 解析版本号中的主版本号
     */
    protected int getMajorVersion(String version) {
        if (version == null) return 0;
        try {
            String[] parts = version.split("\\.");
            return Integer.parseInt(parts[0].replaceAll("\\D.*", ""));
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 解析版本号中的次版本号
     */
    protected int getMinorVersion(String version) {
        if (version == null) return 0;
        try {
            String[] parts = version.split("\\.");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].replaceAll("\\D.*", ""));
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1";
    }
    
    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = connection.getCatalog();
            String version = getDatabaseVersion(connection);
            
            DatabaseMetadata dbMetadata = DatabaseMetadata.builder()
                    .databaseName(databaseName)
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(version)
                    .majorVersion(getMajorVersion(version))
                    .minorVersion(getMinorVersion(version))
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(supportsSchemas())
                    .build();
            
            extractCharsetInfo(connection, databaseName, dbMetadata);
            return dbMetadata;
        }
    }
    
    /**
     * 提取字符集信息，子类可覆盖
     */
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        // 默认实现，子类可覆盖
    }
    
    @Override
    public List<SchemaMetadata> extractSchemas(Connection connection, String databaseName) throws SQLException {
        // MySQL不支持Schema概念，返回空列表
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
    
    /**
     * 获取查询表列表的SQL，子类可覆盖以支持不同版本
     */
    protected String getTablesQuery() {
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE " +
               "FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";
    }
    
    /**
     * 构建表元数据
     */
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
    
    /**
     * 获取查询列信息的SQL，子类可覆盖以支持不同版本
     */
    protected String getColumnsQuery() {
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
    }
    
    /**
     * 构建列元数据
     */
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
    
    /**
     * 查找表的主键
     */
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
    
    /**
     * 将SQL类型映射到Java类型，支持版本特定映射
     */
    protected String mapToJavaType(String sqlType, String version) {
        if (sqlType == null) return "Object";
        String type = sqlType.toLowerCase();
        
        // 首先检查版本特定映射
        for (Map.Entry<String, Map<String, String>> entry : versionSpecificTypeMappings.entrySet()) {
            if (isVersionGreaterOrEqual(version, entry.getKey())) {
                String javaType = entry.getValue().get(type);
                if (javaType != null) return javaType;
            }
        }
        
        // 然后使用默认映射
        return typeMapping.getOrDefault(type, "Object");
    }
    
    /**
     * 比较版本号
     */
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
    
    /**
     * 是否支持Schema概念
     */
    protected boolean supportsSchemas() {
        return false;
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
}
