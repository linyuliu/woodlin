package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * StarRocks数据库元数据提取器
 * <p>
 * StarRocks是极速统一的OLAP数据库，兼容MySQL协议。
 * 支持实时数据分析和多维分析，性能优异。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class StarRocksMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false;
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.STARROCKS;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("starrocks");
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
