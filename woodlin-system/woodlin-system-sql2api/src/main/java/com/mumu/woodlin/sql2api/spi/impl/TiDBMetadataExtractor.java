package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mumu.woodlin.sql2api.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * TiDB数据库元数据提取器
 * 
 * @author mumu
 * @description TiDB是MySQL兼容的分布式数据库，继承MySQL基础实现并处理特定差异
 * @since 2025-01-04
 */
public class TiDBMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    public TiDBMetadataExtractor() {
        super();
        // TiDB特定类型映射
        Map<String, String> tidb5Types = new HashMap<>();
        tidb5Types.put("json", "String");
        versionSpecificTypeMappings.put("5.0", tidb5Types);
    }
    
    @Override
    public String getDatabaseType() {
        return "TiDB";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        // TiDB的productName通常包含TiDB
        return productName != null && productName.toLowerCase().contains("tidb");
    }
    
    @Override
    public int getPriority() {
        return 20; // 高于通用MySQL
    }
}
