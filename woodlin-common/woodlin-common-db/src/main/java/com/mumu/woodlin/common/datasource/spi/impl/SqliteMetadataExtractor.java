package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.BaseJdbcMetadataExtractor;

/**
 * SQLite数据库元数据提取器
 * <p>
 * SQLite是C语言开发的嵌入式关系型数据库。
 * 零配置、无需服务器，常用于移动应用和小型项目。
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
public class SqliteMetadataExtractor extends BaseJdbcMetadataExtractor {
    
    @Override
    protected boolean supportsSchema() {
        return false; // SQLite不支持Schema
    }
    
    @Override
    protected boolean supportsCatalog() {
        return true;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLITE;
    }
    
    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("sqlite");
    }
    
    @Override
    public String getDefaultDriverClass() {
        return "org.sqlite.JDBC";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
}
