package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * 人大金仓数据库元数据提取器
 * <p>
 * 人大金仓数据库（KingbaseES）基于PostgreSQL开发，是国产数据库。
 * 兼容PostgreSQL协议和大部分SQL语法。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class KingbaseMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.KINGBASE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("kingbase");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.kingbase8.Driver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
