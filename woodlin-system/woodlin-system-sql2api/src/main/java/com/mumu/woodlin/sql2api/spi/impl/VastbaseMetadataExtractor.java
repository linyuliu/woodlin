package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.sql2api.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * 瀚高数据库元数据提取器
 * 
 * @author mumu
 * @description Vastbase是基于PostgreSQL的国产数据库，继承PostgreSQL基础实现
 * @since 2025-01-04
 */
public class VastbaseMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "Vastbase";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("vastbase") || 
                productName.toLowerCase().contains("瀚高"));
    }
    
    @Override
    public int getPriority() {
        return 20; // 高于通用PostgreSQL
    }
}
