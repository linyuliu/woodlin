package com.mumu.woodlin.common.datasource.spi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.base.AbstractPostgreSQLCompatibleExtractor;

/**
 * 人大金仓（KingbaseES）元数据提取器。
 *
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class KingbaseMetadataExtractor extends AbstractPostgreSQLCompatibleExtractor {

    @Getter
    @RequiredArgsConstructor
    public enum CompatibilityMode {
        PG("pg", "PostgreSQL"),
        ORACLE("oracle", "Oracle"),
        MYSQL("mysql", "MySQL"),
        MSSQL("mssql", "SQL Server");

        private final String code;
        private final String description;

        public static CompatibilityMode fromCode(String code) {
            if (StrUtil.isBlank(code)) {
                return PG;
            }
            String normalized = code.trim().toLowerCase();
            for (CompatibilityMode mode : values()) {
                if (mode.code.equals(normalized)) {
                    return mode;
                }
            }
            return PG;
        }
    }

    private final ThreadLocal<CompatibilityMode> currentMode = ThreadLocal.withInitial(() -> CompatibilityMode.PG);

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.KINGBASE;
    }

    @Override
    public boolean supports(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();
        if (StrUtil.isBlank(productName)) {
            return false;
        }
        String normalized = productName.toLowerCase();
        return normalized.contains("kingbase") || normalized.contains("kingbasees");
    }

    @Override
    public String getDefaultDriverClass() {
        return "com.kingbase8.Driver";
    }

    @Override
    public DatabaseMetadata extractDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            CompatibilityMode mode = detectCompatibilityMode(connection);
            currentMode.set(mode);
            log.info("Detected Kingbase compatibility mode: {}", mode.getDescription());

            DatabaseMetadata metadata = super.extractDatabaseMetadata(dataSource);
            if (StrUtil.isNotBlank(metadata.getDatabaseProductVersion())) {
                metadata.setDatabaseProductVersion(
                        metadata.getDatabaseProductVersion() + " [" + mode.getDescription() + " mode]");
            }
            return metadata;
        } finally {
            currentMode.remove();
        }
    }

    @Override
    protected void initializeDefaultTypeMapping() {
        super.initializeDefaultTypeMapping();

        typeMapping.put("number", "BigDecimal");
        typeMapping.put("varchar2", "String");
        typeMapping.put("nvarchar2", "String");
        typeMapping.put("clob", "String");
        typeMapping.put("blob", "byte[]");
        typeMapping.put("raw", "byte[]");

        typeMapping.put("tinyint", "Byte");
        typeMapping.put("mediumint", "Integer");
        typeMapping.put("datetime", "LocalDateTime");
        typeMapping.put("longtext", "String");
        typeMapping.put("mediumtext", "String");
        typeMapping.put("tinytext", "String");

        typeMapping.put("nvarchar", "String");
        typeMapping.put("nchar", "String");
        typeMapping.put("ntext", "String");
        typeMapping.put("image", "byte[]");
        typeMapping.put("money", "BigDecimal");
        typeMapping.put("smallmoney", "BigDecimal");
        typeMapping.put("uniqueidentifier", "UUID");
    }

    @Override
    public String getDefaultTestQuery() {
        return currentMode.get() == CompatibilityMode.ORACLE ? "SELECT 1 FROM DUAL" : "SELECT 1";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public String getMinSupportedVersion() {
        return "8.0";
    }

    @Override
    public boolean supportsVersion(Connection connection, int majorVersion, int minorVersion) throws SQLException {
        return supports(connection) && majorVersion >= 8;
    }

    private CompatibilityMode detectCompatibilityMode(Connection connection) {
        CompatibilityMode mode = queryCompatibilityMode(connection, "SHOW database_mode", null);
        if (mode != null) {
            return mode;
        }

        mode = queryCompatibilityMode(
                connection,
                "SELECT setting AS mode FROM pg_settings WHERE name = 'database_mode'",
                "mode");
        if (mode != null) {
            return mode;
        }

        mode = queryCompatibilityMode(
                connection,
                "SELECT current_setting('database_mode') AS mode",
                "mode");
        return mode == null ? CompatibilityMode.PG : mode;
    }

    private CompatibilityMode queryCompatibilityMode(Connection connection, String sql, String columnName) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            String raw = columnName == null ? rs.getString(1) : rs.getString(columnName);
            return CompatibilityMode.fromCode(raw);
        } catch (SQLException ex) {
            log.debug("Query Kingbase compatibility mode failed. sql={}, msg={}", sql, ex.getMessage());
            return null;
        }
    }
}
