package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.spi.base.AbstractMySQLCompatibleExtractor;

/**
 * MySQL数据库元数据提取器
 * 
 * @author mumu
 * @description MySQL/MariaDB数据库的元数据提取实现，支持5.x和8.x版本
 * @since 2025-01-01
 */
@Slf4j
public class MySQLMetadataExtractor extends AbstractMySQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && 
               (productName.toLowerCase().contains("mysql") || 
                productName.toLowerCase().contains("mariadb"));
    }
    
    @Override
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME " +
                     "FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'")) {
            if (rs.next()) {
                dbMetadata.setCharset(rs.getString("DEFAULT_CHARACTER_SET_NAME"));
                dbMetadata.setCollation(rs.getString("DEFAULT_COLLATION_NAME"));
            }
        }
    }
    
    @Override
    protected String getTablesQuery() {
        return "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, ENGINE, " +
               "TABLE_COLLATION, CREATE_TIME, UPDATE_TIME " +
               "FROM information_schema.TABLES " +
               "WHERE TABLE_SCHEMA = ?";
    }
    
    @Override
    protected String getColumnsQuery() {
        return "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, " +
               "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, " +
               "IS_NULLABLE, COLUMN_DEFAULT, COLUMN_KEY, EXTRA, ORDINAL_POSITION " +
               "FROM information_schema.COLUMNS " +
               "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
               "ORDER BY ORDINAL_POSITION";
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级
    }
}
