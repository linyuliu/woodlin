package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mumu.woodlin.sql2api.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * 人大金仓数据库元数据提取器
 * 
 * @author mumu
 * @description KingbaseES是基于PostgreSQL的国产数据库，继承PostgreSQL基础实现
 * @since 2025-01-04
 */
public class KingbaseESMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    public KingbaseESMetadataExtractor() {
        super();
        // 金仓特定类型映射
        Map<String, String> kingbaseV8Types = new HashMap<>();
        kingbaseV8Types.put("nvarchar", "String");
        kingbaseV8Types.put("nchar", "String");
        versionSpecificTypeMappings.put("8.0", kingbaseV8Types);
    }
    
    @Override
    public String getDatabaseType() {
        return "KingbaseES";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("kingbase") || 
                productName.toLowerCase().contains("金仓"));
    }
    
    @Override
    public int getPriority() {
        return 20; // 高于通用PostgreSQL
    }
}
