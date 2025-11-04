package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mumu.woodlin.sql2api.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * 达梦数据库元数据提取器
 * 
 * @author mumu
 * @description DM8支持MySQL兼容模式，继承MySQL基础实现并处理达梦特定类型
 * @since 2025-01-04
 */
public class DM8MetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    public DM8MetadataExtractor() {
        super();
        // 达梦特定类型映射
        Map<String, String> dm8Types = new HashMap<>();
        dm8Types.put("text", "String");
        dm8Types.put("clob", "String");
        dm8Types.put("blob", "byte[]");
        versionSpecificTypeMappings.put("8.0", dm8Types);
    }
    
    @Override
    public String getDatabaseType() {
        return "DM8";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("dm") || 
                productName.toLowerCase().contains("达梦"));
    }
    
    @Override
    protected boolean supportsSchemas() {
        return true; // DM8支持Schema
    }
    
    @Override
    public int getPriority() {
        return 20; // 高于通用MySQL
    }
}
