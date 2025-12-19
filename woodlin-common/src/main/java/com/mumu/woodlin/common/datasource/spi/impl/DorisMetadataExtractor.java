package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * Apache Doris数据库元数据提取器
 * <p>
 * Apache Doris是现代化的MPP分析型数据库，兼容MySQL协议。
 * 支持实时数据分析、联邦查询和物化视图等特性。
 * </p>
 * <p>
 * 注意：StarRocks从Doris分支而来，因此supports方法同时检测doris和starrocks。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class DorisMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
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
        return DatabaseType.DORIS;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        if (productName == null) {
            return false;
        }
        String name = productName.toLowerCase();
        // Doris和StarRocks都可以识别，但StarRocks有专用extractor，优先级更高
        return name.contains("doris") || name.contains("starrocks");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }
    
    @Override
    public int getPriority() {
        return 60; // 低于StarRocks的优先级，让StarRocks extractor优先匹配
    }
}
