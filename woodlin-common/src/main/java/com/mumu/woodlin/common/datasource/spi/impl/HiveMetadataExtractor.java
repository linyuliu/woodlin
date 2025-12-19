package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * Apache Hive数据仓库元数据提取器
 * <p>
 * Apache Hive是基于Hadoop的数据仓库工具，提供SQL查询接口。
 * 适用于大数据批处理分析场景。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class HiveMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // Hive使用Database概念
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.HIVE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("hive");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.apache.hive.jdbc.HiveDriver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
