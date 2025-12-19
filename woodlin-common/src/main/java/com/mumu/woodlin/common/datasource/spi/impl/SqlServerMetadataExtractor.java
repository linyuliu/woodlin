package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * Microsoft SQL Server数据库元数据提取器
 * <p>
 * Microsoft SQL Server是微软开发的关系型数据库。
 * 支持Windows和Linux平台，提供企业级功能。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class SqlServerMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return true; // SQL Server支持Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true; // SQL Server同时支持Catalog（数据库）
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLSERVER;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("sql server") || 
                productName.toLowerCase().contains("microsoft"));
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }
    
    @Override
    public String getDefaultTestQuery() {
        return "SELECT 1";
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
}
