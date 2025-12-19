package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * 南大通用GBase数据库元数据提取器
 * <p>
 * 南大通用GBase数据库（GBase 8s/8a/8t）是国产数据库，支持SQL标准。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class GBaseMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.GBASE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("gbase");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.gbase.jdbc.Driver";
    }
    
    @Override
    protected boolean supportsSchema() {
        return true;
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
