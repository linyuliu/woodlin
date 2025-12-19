package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * MariaDB数据库元数据提取器
 * <p>
 * MariaDB是MySQL的分支，由MySQL创始人开发。
 * 完全兼容MySQL协议，提供了许多增强特性。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class MariaDbMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MARIADB;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("mariadb");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.mariadb.jdbc.Driver";
    }
    
    @Override
    public int getPriority() {
        return 20; // 高于MySQL的优先级，优先匹配MariaDB
    }
}
