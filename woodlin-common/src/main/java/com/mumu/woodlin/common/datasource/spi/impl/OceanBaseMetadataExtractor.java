package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * OceanBase数据库元数据提取器
 * <p>
 * OceanBase是蚂蚁金服开发的分布式关系型数据库，兼容MySQL协议。
 * 支持金融级高可用、ACID事务和水平扩展。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class OceanBaseMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // OceanBase使用Catalog，不使用Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.OCEANBASE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("oceanbase");
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
