package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * 华为openGauss数据库元数据提取器
 * <p>
 * 华为openGauss数据库是GaussDB的开源版本，基于PostgreSQL开发。
 * 兼容PostgreSQL协议和大部分SQL语法。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class OpenGaussMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.OPENGAUSS;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("opengauss");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.postgresql.Driver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
