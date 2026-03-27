package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * InfluxDB时序数据库元数据提取器
 * <p>
 * InfluxDB是开源的时序数据库，专为时间序列数据设计。
 * 适用于监控、实时分析和IoT应用场景。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class InfluxDbMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // InfluxDB使用Database概念
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.INFLUXDB;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("influxdb");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.influxdb.jdbc.InfluxDBDriver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
