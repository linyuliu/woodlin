package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * MySQL 5.x 数据库元数据提取器
 * <p>
 * 专门针对 MySQL 5.x 版本优化的元数据提取器。
 * 某些功能在 MySQL 5.x 中的行为与 8.x 不同，此提取器处理这些差异。
 * </p>
 * <p>
 * MySQL 5.x 特有限制：
 * <ul>
 *   <li>不支持窗口函数</li>
 *   <li>不支持 CTE (WITH 子句)</li>
 *   <li>JSON 支持从 5.7 开始</li>
 *   <li>不支持不可见索引</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class MySQL5MetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "MySQL5";
    }
    
    @Override
    public String getMinSupportedVersion() {
        return "5.5";
    }
    
    @Override
    public String getMaxSupportedVersion() {
        return "5.7";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        if (productName == null || !productName.toLowerCase().contains("mysql")) {
            return false;
        }
        
        // 检查版本是否在 5.x 范围内
        String version = connection.getMetaData().getDatabaseProductVersion();
        int major = getMajorVersion(version);
        return major == 5;
    }
    
    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        return majorVersion == 5 && minorVersion >= 5;
    }
    
    @Override
    protected void initializeVersionSpecificMappings() {
        // MySQL 5.5 基础类型
        // 不添加 JSON 类型，因为 5.5 不支持
        
        // MySQL 5.6 略微增强
        Map<String, String> mysql56Mappings = new HashMap<>();
        versionSpecificTypeMappings.put("5.6", mysql56Mappings);
        
        // MySQL 5.7 开始支持 JSON
        Map<String, String> mysql57Mappings = new HashMap<>();
        mysql57Mappings.put("json", "String");
        versionSpecificTypeMappings.put("5.7", mysql57Mappings);
    }
    
    @Override
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        // MySQL 5.x 使用相同的查询
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME " +
                     "FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'")) {
            if (rs.next()) {
                dbMetadata.setCharset(rs.getString("DEFAULT_CHARACTER_SET_NAME"));
                dbMetadata.setCollation(rs.getString("DEFAULT_COLLATION_NAME"));
            }
        }
    }
    
    @Override
    protected String getTablesQuery() {
        // MySQL 5.x 的表查询（与 8.x 相同，但为了清晰起见单独定义）
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, ENGINE, " +
               "TABLE_COLLATION, CREATE_TIME, UPDATE_TIME " +
               "FROM information_schema.TABLES " +
               "WHERE TABLE_SCHEMA = ?";
    }
    
    @Override
    protected String getColumnsQuery() {
        // MySQL 5.x 的列查询
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, " +
               "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
               "ORDER BY ORDINAL_POSITION";
    }
    
    @Override
    public int getPriority() {
        // 比通用 MySQL 提取器优先级更高，因为它是版本特定的
        return 5;
    }
}
