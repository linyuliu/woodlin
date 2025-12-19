package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * TiDB数据库元数据提取器
 * <p>
 * TiDB是PingCAP开发的NewSQL分布式数据库，兼容MySQL 5.7协议。
 * 支持水平扩展、强一致性和高可用性。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class TiDbMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // TiDB使用Catalog，不使用Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.TIDB;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("tidb");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
