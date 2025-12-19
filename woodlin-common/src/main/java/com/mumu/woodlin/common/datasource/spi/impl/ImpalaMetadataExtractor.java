package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * Apache Impala数据库元数据提取器
 * <p>
 * Apache Impala是基于Hadoop的MPP SQL查询引擎。
 * 提供低延迟的交互式SQL查询，适用于实时查询场景。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class ImpalaMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // Impala使用Database概念
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.IMPALA;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("impala");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.cloudera.impala.jdbc.Driver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
