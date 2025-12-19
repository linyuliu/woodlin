package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * 达梦数据库元数据提取器
 * <p>
 * 达梦数据库（DM8）的元数据提取实现。达梦是国产数据库，支持SQL标准。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class DmMetadataExtractor extends BaseJdbcMetadataExtractor {
    
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
    protected boolean supportsSchema() {
        return true; // DM支持Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return false;
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
