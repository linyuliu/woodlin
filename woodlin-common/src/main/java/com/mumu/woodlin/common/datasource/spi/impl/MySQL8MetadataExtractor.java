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
 * MySQL 8.x 数据库元数据提取器
 * <p>
 * 专门针对 MySQL 8.x 版本优化的元数据提取器。
 * 利用 MySQL 8.x 的新特性提供更丰富的元数据信息。
 * </p>
 * <p>
 * MySQL 8.x 特有功能：
 * <ul>
 *   <li>窗口函数支持</li>
 *   <li>CTE (WITH 子句) 支持</li>
 *   <li>不可见索引</li>
 *   <li>降序索引</li>
 *   <li>功能索引</li>
 *   <li>原子 DDL</li>
 *   <li>默认表达式增强</li>
 *   <li>8.0.13+ 不可见列</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class MySQL8MetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "MySQL8";
    }
    
    @Override
    public String getMinSupportedVersion() {
        return "8.0";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        if (productName == null || !productName.toLowerCase().contains("mysql")) {
            return false;
        }
        
        // 检查版本是否在 8.x 范围内
        String version = connection.getMetaData().getDatabaseProductVersion();
        int major = getMajorVersion(version);
        return major >= 8;
    }
    
    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        return majorVersion >= 8;
    }
    
    @Override
    protected void initializeVersionSpecificMappings() {
        super.initializeVersionSpecificMappings();
        
        // MySQL 8.0 JSON 和其他类型
        Map<String, String> mysql80Mappings = new HashMap<>();
        mysql80Mappings.put("json", "String");
        versionSpecificTypeMappings.put("8.0", mysql80Mappings);
    }
    
    @Override
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        // MySQL 8.x 默认使用 utf8mb4
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
        // MySQL 8.x 可以获取更多表信息
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, ENGINE, " +
               "TABLE_COLLATION, CREATE_TIME, UPDATE_TIME, TABLE_ROWS " +
               "FROM information_schema.TABLES " +
               "WHERE TABLE_SCHEMA = ?";
    }
    
    @Override
    protected String getColumnsQuery() {
        // MySQL 8.x 的列查询，包括 GENERATION_EXPRESSION 用于生成列
        // 注意：MySQL 8.0.13+ 支持 IS_VISIBLE 列
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, " +
               "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION, " +
               "GENERATION_EXPRESSION, SRS_ID " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
               "ORDER BY ORDINAL_POSITION";
    }
    
    /**
     * MySQL 8.0.13+ 支持查询不可见列
     */
    protected String getColumnsQueryWithVisibility() {
        return "SELECT c.COLUMN_NAME, c.COLUMN_COMMENT, c.DATA_TYPE, c.COLUMN_TYPE, " +
               "c.CHARACTER_MAXIMUM_LENGTH, c.NUMERIC_PRECISION, c.NUMERIC_SCALE, " +
               "c.IS_NULLABLE, c.COLUMN_DEFAULT, c.COLUMN_KEY, c.EXTRA, c.ORDINAL_POSITION, " +
               "c.GENERATION_EXPRESSION, c.SRS_ID, " +
               "COALESCE((SELECT 'NO' FROM information_schema.COLUMNS ic " +
               "WHERE ic.TABLE_SCHEMA = c.TABLE_SCHEMA AND ic.TABLE_NAME = c.TABLE_NAME " +
               "AND ic.COLUMN_NAME = c.COLUMN_NAME AND ic.EXTRA LIKE '%INVISIBLE%'), 'YES') AS IS_VISIBLE " +
               "FROM information_schema.COLUMNS c " +
               "WHERE c.TABLE_SCHEMA = ? AND c.TABLE_NAME = ? " +
               "ORDER BY c.ORDINAL_POSITION";
    }
    
    @Override
    public int getPriority() {
        // 比通用 MySQL 提取器优先级更高，因为它是版本特定的
        return 5;
    }
}
