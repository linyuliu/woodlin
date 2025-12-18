package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * PostgreSQL数据库元数据提取器
 * <p>
 * PostgreSQL数据库的元数据提取实现，支持10.x及以上版本。
 * </p>
 * <p>
 * 版本差异支持：
 * <ul>
 *   <li>PostgreSQL 10+: 声明式分区、Identity列</li>
 *   <li>PostgreSQL 11+: 存储过程、JIT编译</li>
 *   <li>PostgreSQL 12+: 生成列、JSON路径查询</li>
 *   <li>PostgreSQL 13+: 增量排序、并行 B-tree 索引构建</li>
 *   <li>PostgreSQL 14+: MULTIRANGE类型</li>
 *   <li>PostgreSQL 15+: MERGE 语句</li>
 * </ul>
 * </p>
 * 
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class PostgreSQLMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
    
    @Override
    public String getMinSupportedVersion() {
        return "10";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("postgresql");
    }
    
    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        if (!supports(connection)) {
            return false;
        }
        // 支持 PostgreSQL 10+
        return majorVersion >= 10;
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
