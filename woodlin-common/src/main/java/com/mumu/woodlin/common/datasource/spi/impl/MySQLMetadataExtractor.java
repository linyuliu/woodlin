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
 * MySQL数据库元数据提取器
 * <p>
 * MySQL/MariaDB数据库的元数据提取实现，支持5.x和8.x版本。
 * </p>
 * <p>
 * 版本差异支持：
 * <ul>
 *   <li>MySQL 5.6: 基础功能</li>
 *   <li>MySQL 5.7+: JSON类型支持、生成列</li>
 *   <li>MySQL 8.0+: 窗口函数、CTE、不可见列等</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
public class MySQLMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    @Override
    public String getMinSupportedVersion() {
        return "5.6";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("mysql") || 
                productName.toLowerCase().contains("mariadb"));
    }
    
    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        if (!supports(connection)) {
            return false;
        }
        // 支持 MySQL 5.6+
        if (majorVersion < 5) {
            return false;
        }
        if (majorVersion == 5 && minorVersion < 6) {
            return false;
        }
        return true;
    }
    
    @Override
    protected void initializeVersionSpecificMappings() {
        super.initializeVersionSpecificMappings();
        
        // MySQL 5.7+ JSON支持和生成列
        Map<String, String> mysql57Mappings = new HashMap<>();
        mysql57Mappings.put("json", "String");
        versionSpecificTypeMappings.put("5.7", mysql57Mappings);
        
        // MySQL 8.0+ 增强特性
        Map<String, String> mysql80Mappings = new HashMap<>();
        mysql80Mappings.put("json", "String");
        // 8.0 支持更好的默认值表达式
        versionSpecificTypeMappings.put("8.0", mysql80Mappings);
    }
    
    @Override
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
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
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, ENGINE, " +
               "TABLE_COLLATION, CREATE_TIME, UPDATE_TIME " +
               "FROM information_schema.TABLES " +
               "WHERE TABLE_SCHEMA = ?";
    }
    
    @Override
    protected String getColumnsQuery() {
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, " +
               "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
               "ORDER BY ORDINAL_POSITION";
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级
    }
}
