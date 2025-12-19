package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * Greenplum数据库元数据提取器
 * <p>
 * Greenplum是基于PostgreSQL的MPP大规模并行处理数据仓库。
 * 支持PB级数据分析，兼容PostgreSQL协议。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class GreenplumMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.GREENPLUM;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("greenplum");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.postgresql.Driver";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
