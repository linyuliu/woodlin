package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * ClickHouse数据库元数据提取器
 * <p>
 * ClickHouse是Yandex开源的OLAP列式存储数据库。
 * 适用于大数据实时分析场景，支持高性能查询。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class ClickHouseMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // ClickHouse使用Database概念
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.CLICKHOUSE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("clickhouse");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.clickhouse.jdbc.ClickHouseDriver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
