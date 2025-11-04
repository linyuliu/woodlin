package com.mumu.woodlin.sql2api.spi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * PostgreSQL数据库元数据提取器
 * 
 * @author mumu
 * @description PostgreSQL数据库的元数据提取实现，支持10.x及以上版本
 * @since 2025-01-04
 */
@Slf4j
public class PostgreSQLMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("postgresql");
    }
    
    @Override
    protected void extractCharsetInfo(Connection connection, String databaseName, DatabaseMetadata dbMetadata) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT pg_encoding_to_char(encoding) as encoding, " +
                "datcollate as collation " +
                "FROM pg_database WHERE datname = ?")) {
            pstmt.setString(1, databaseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dbMetadata.setCharset(rs.getString("encoding"));
                    dbMetadata.setCollation(rs.getString("collation"));
                }
            }
        }
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级
    }
}
