package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * 神舟通用Oscar数据库元数据提取器
 * <p>
 * 神舟通用Oscar数据库是国产数据库，支持SQL标准。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class OscarMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.OSCAR;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("oscar");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.oscar.Driver";
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
