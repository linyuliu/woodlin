package com.mumu.woodlin.etl.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据库方言解析器。
 *
 * @author mumu
 * @since 1.0.0
 */
@Slf4j
@Component
public class DatabaseDialectResolver {

    private final Map<DatabaseDialectType, DatabaseDialect> dialectRegistry = new EnumMap<>(DatabaseDialectType.class);

    public DatabaseDialectResolver(List<DatabaseDialect> dialects) {
        for (DatabaseDialect dialect : dialects) {
            dialectRegistry.put(dialect.getDialectType(), dialect);
        }
    }

    /**
     * 根据连接元信息解析数据库方言。
     *
     * @param connection JDBC 连接
     * @return 对应方言实现
     * @throws SQLException 元信息读取失败
     */
    public DatabaseDialect resolve(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String productName = safeLower(metaData.getDatabaseProductName());
        String url = safeLower(metaData.getURL());
        DatabaseDialectType dialectType = resolveDialectType(productName, url);
        DatabaseDialect dialect = dialectRegistry.getOrDefault(dialectType, dialectRegistry.get(DatabaseDialectType.GENERIC));
        if (dialect == null) {
            throw new IllegalStateException("未注册通用数据库方言实现");
        }
        log.debug("解析数据库方言: product={}, url={}, dialect={}", productName, url, dialectType);
        return dialect;
    }

    private DatabaseDialectType resolveDialectType(String productName, String url) {
        if (containsAny(productName, url, "mysql", "mariadb", "tidb", "polar", "starrocks")) {
            return DatabaseDialectType.MYSQL;
        }
        if (containsAny(productName, url, "postgresql", "postgres", "opengauss", "kingbase", "gaussdb")) {
            return DatabaseDialectType.POSTGRESQL;
        }
        if (containsAny(productName, url, "oracle")) {
            return DatabaseDialectType.ORACLE;
        }
        if (containsAny(productName, url, "sql server", "microsoft sql server", "jdbc:sqlserver")) {
            return DatabaseDialectType.SQL_SERVER;
        }
        return DatabaseDialectType.GENERIC;
    }

    private boolean containsAny(String productName, String url, String... candidates) {
        for (String candidate : candidates) {
            if (productName.contains(candidate) || url.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
