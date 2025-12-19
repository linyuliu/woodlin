package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * PolarDB数据库元数据提取器
 * <p>
 * PolarDB是阿里云开发的云原生关系型数据库，兼容MySQL协议。
 * 支持读写分离、计算存储分离和弹性扩展。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class PolarDbMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // PolarDB使用Catalog，不使用Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.POLARDB;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("polardb");
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
