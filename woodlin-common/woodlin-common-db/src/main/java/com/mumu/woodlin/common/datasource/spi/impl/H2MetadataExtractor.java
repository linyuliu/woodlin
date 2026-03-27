package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * H2数据库元数据提取器
 * <p>
 * H2是Java开发的嵌入式关系型数据库。
 * 支持内存模式和文件模式，常用于开发测试环境。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class H2MetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return true; // H2支持Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return false;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.H2;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("h2");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.h2.Driver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
