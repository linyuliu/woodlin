package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * TDengine时序数据库元数据提取器
 * <p>
 * TDengine是涛思数据开发的高性能时序数据库。
 * 专为物联网、工业互联网等时序数据场景设计。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class TDengineMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // TDengine使用Database概念
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.TDENGINE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("taos");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.taosdata.jdbc.TSDBDriver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
