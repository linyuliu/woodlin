package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseType;
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
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
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
    public int getPriority() {
        // 比通用 MySQL 提取器优先级更高，因为它是版本特定的
        return 5;
    }
}
