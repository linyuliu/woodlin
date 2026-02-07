package com.mumu.woodlin.common.datasource.spi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.datasource.model.DatabaseType;
import com.mumu.woodlin.common.datasource.spi.impl.ClickHouseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.DmMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.DorisMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.GBaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.GaussDbMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.GreenplumMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.H2MetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.HiveMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.ImpalaMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.InfluxDbMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.KingbaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.MariaDbMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.MySQL5MetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.MySQL8MetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.MySQLMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.OceanBaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.OpenGaussMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.OracleMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.OscarMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.PolarDbMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.PostgreSQLMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.SqlServerMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.SqliteMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.StarRocksMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.TDengineMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.TiDbMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.UxdbMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.VastbaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.impl.VitessMetadataExtractor;

/**
 * 数据库元数据提取器工厂
 * <p>
 * 负责管理和选择合适的数据库元数据提取器。
 * 支持 SPI 机制动态加载和手动注册。
 * </p>
 * <p>
 * 选择优先级：
 * <ol>
 *   <li>版本特定的提取器（如 MySQL5, MySQL8）</li>
 *   <li>通用类型提取器（如 MySQL）</li>
 *   <li>按优先级排序</li>
 * </ol>
 * </p>
 *
 * @author mumu
 * @since 2025-01-04
 */
@Slf4j
public class DatabaseMetadataExtractorFactory {

    private static final DatabaseMetadataExtractorFactory INSTANCE = new DatabaseMetadataExtractorFactory();
    private static final Map<String, DatabaseType> JDBC_PREFIX_TO_TYPE = createJdbcPrefixMapping();
    private static final Map<String, DatabaseType> TYPE_ALIAS_TO_DATABASE_TYPE = createTypeAliasMapping();

    private final List<DatabaseMetadataExtractor> extractors = new CopyOnWriteArrayList<>();

    private DatabaseMetadataExtractorFactory() {
        // 加载 SPI 提供的提取器
        loadSpiExtractors();
        // 注册内置提取器
        registerBuiltinExtractors();
    }

    /**
     * 获取单例实例
     */
    public static DatabaseMetadataExtractorFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 通过 SPI 加载提取器
     */
    private void loadSpiExtractors() {
        try {
            ServiceLoader<DatabaseMetadataExtractor> loader = ServiceLoader.load(DatabaseMetadataExtractor.class);
            for (DatabaseMetadataExtractor extractor : loader) {
                extractors.add(extractor);
                log.debug("Loaded metadata extractor via SPI: {}", extractor.getDatabaseType().name());
            }
        } catch (Exception e) {
            log.warn("Failed to load metadata extractors via SPI: {}", e.getMessage());
        }
    }

    /**
     * 注册内置提取器
     */
    private void registerBuiltinExtractors() {
        // 注册版本特定的提取器（优先级更高）
        registerIfNotExists(new MySQL5MetadataExtractor());
        registerIfNotExists(new MySQL8MetadataExtractor());

        // 注册主流关系型数据库提取器
        registerIfNotExists(new MySQLMetadataExtractor());
        registerIfNotExists(new MariaDbMetadataExtractor());
        registerIfNotExists(new PostgreSQLMetadataExtractor());
        registerIfNotExists(new OracleMetadataExtractor());
        registerIfNotExists(new SqlServerMetadataExtractor());

        // 注册MySQL协议兼容数据库提取器
        registerIfNotExists(new TiDbMetadataExtractor());
        registerIfNotExists(new OceanBaseMetadataExtractor());
        registerIfNotExists(new PolarDbMetadataExtractor());
        registerIfNotExists(new VitessMetadataExtractor());

        // 注册国产数据库提取器
        registerIfNotExists(new DmMetadataExtractor());
        registerIfNotExists(new KingbaseMetadataExtractor());
        registerIfNotExists(new GBaseMetadataExtractor());
        registerIfNotExists(new GaussDbMetadataExtractor());
        registerIfNotExists(new OpenGaussMetadataExtractor());
        registerIfNotExists(new OscarMetadataExtractor());
        registerIfNotExists(new UxdbMetadataExtractor());
        registerIfNotExists(new VastbaseMetadataExtractor());

        // 注册分析型/数据仓库提取器
        registerIfNotExists(new ClickHouseMetadataExtractor());
        registerIfNotExists(new StarRocksMetadataExtractor());
        registerIfNotExists(new DorisMetadataExtractor());
        registerIfNotExists(new GreenplumMetadataExtractor());
        registerIfNotExists(new HiveMetadataExtractor());
        registerIfNotExists(new ImpalaMetadataExtractor());

        // 注册时序数据库提取器
        registerIfNotExists(new TDengineMetadataExtractor());
        registerIfNotExists(new InfluxDbMetadataExtractor());

        // 注册嵌入式数据库提取器
        registerIfNotExists(new H2MetadataExtractor());
        registerIfNotExists(new SqliteMetadataExtractor());
    }

    /**
     * 如果不存在则注册提取器
     */
    private void registerIfNotExists(DatabaseMetadataExtractor extractor) {
        boolean exists = extractors.stream()
                .anyMatch(e -> e.getClass().equals(extractor.getClass()));
        if (!exists) {
            extractors.add(extractor);
            log.debug("Registered builtin metadata extractor: {}", extractor.getDatabaseType().name());
        }
    }

    /**
     * 手动注册提取器
     *
     * @param extractor 要注册的提取器
     */
    public void register(DatabaseMetadataExtractor extractor) {
        if (extractor != null) {
            extractors.add(extractor);
            log.info("Registered metadata extractor: {}", extractor.getDatabaseType().name());
        }
    }

    /**
     * 获取所有已注册的提取器
     *
     * @return 提取器列表，按优先级排序
     */
    public List<DatabaseMetadataExtractor> getAllExtractors() {
        List<DatabaseMetadataExtractor> result = new ArrayList<>(extractors);
        result.sort(Comparator.comparingInt(DatabaseMetadataExtractor::getPriority));
        return result;
    }

    /**
     * 根据数据源自动选择合适的提取器
     *
     * @param dataSource 数据源
     * @return 匹配的提取器
     * @throws SQLException SQL异常
     */
    public Optional<DatabaseMetadataExtractor> getExtractor(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return getExtractor(connection);
        }
    }

    /**
     * 根据连接自动选择合适的提取器
     *
     * @param connection 数据库连接
     * @return 匹配的提取器
     * @throws SQLException SQL异常
     */
    public Optional<DatabaseMetadataExtractor> getExtractor(Connection connection) throws SQLException {
        int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
        int minorVersion = connection.getMetaData().getDatabaseMinorVersion();

        // 首先尝试找到版本特定的提取器
        List<DatabaseMetadataExtractor> candidates = new ArrayList<>();

        for (DatabaseMetadataExtractor extractor : extractors) {
            try {
                if (extractor.supports(connection)) {
                    // 检查版本支持
                    if (extractor.supportsVersion(connection, majorVersion, minorVersion)) {
                        candidates.add(extractor);
                    }
                }
            } catch (Exception e) {
                log.debug("Extractor {} check failed: {}", extractor.getDatabaseType().name(), e.getMessage());
            }
        }

        // 按优先级排序，优先级数字越小越优先
        candidates.sort(Comparator.comparingInt(DatabaseMetadataExtractor::getPriority));

        if (!candidates.isEmpty()) {
            DatabaseMetadataExtractor selected = candidates.get(0);
            log.debug("Selected metadata extractor: {} (priority: {})",
                    selected.getDatabaseType().name(), selected.getPriority());
            return Optional.of(selected);
        }

        log.warn("No matching metadata extractor found for database");
        return Optional.empty();
    }

    /**
     * 根据数据库类型名称获取提取器
     *
     * @param databaseType 数据库类型，如 "MySQL", "PostgreSQL", "Oracle"
     * @return 匹配的提取器
     */
    public Optional<DatabaseMetadataExtractor> getExtractorByType(String databaseType) {
        if (StrUtil.isBlank(databaseType)) {
            return Optional.empty();
        }

        String normalized = normalizeTypeToken(databaseType);
        DatabaseType exactType = TYPE_ALIAS_TO_DATABASE_TYPE.get(normalized);

        if (exactType != null) {
            return extractors.stream()
                    .filter(e -> e.getDatabaseType() == exactType)
                    .min(Comparator.comparingInt(DatabaseMetadataExtractor::getPriority));
        }

        return extractors.stream()
                .filter(e -> {
                    String normalizedEnum = normalizeTypeToken(e.getDatabaseType().name());
                    return normalizedEnum.contains(normalized) || normalized.contains(normalizedEnum);
                })
                .min(Comparator.comparingInt(DatabaseMetadataExtractor::getPriority));
    }

    /**
     * 获取默认的连通性测试SQL
     *
     * @param databaseType 数据库类型
     * @return 测试SQL
     */
    public String getDefaultTestQuery(String databaseType) {
        return getExtractorByType(databaseType)
                .map(DatabaseMetadataExtractor::getDefaultTestQuery)
                .orElse("SELECT 1");
    }

    /**
     * 获取默认的JDBC驱动类名
     *
     * @param databaseType 数据库类型
     * @return 驱动类全限定名
     */
    public String getDefaultDriverClass(String databaseType) {
        return getExtractorByType(databaseType)
                .map(DatabaseMetadataExtractor::getDefaultDriverClass)
                .orElse(null);
    }

    /**
     * 根据JDBC URL推断数据库类型
     *
     * @param jdbcUrl JDBC URL
     * @return 数据库类型
     */
    public String inferDatabaseType(String jdbcUrl) {
        if (StrUtil.isBlank(jdbcUrl)) {
            return null;
        }

        String url = jdbcUrl.toLowerCase();

        for (Map.Entry<String, DatabaseType> entry : JDBC_PREFIX_TO_TYPE.entrySet()) {
            if (url.startsWith(entry.getKey())) {
                return entry.getValue().name();
            }
        }

        return null;
    }

    private static Map<String, DatabaseType> createJdbcPrefixMapping() {
        Map<String, DatabaseType> mapping = new LinkedHashMap<>();
        mapping.put("jdbc:mysql", DatabaseType.MYSQL);
        mapping.put("jdbc:mariadb", DatabaseType.MARIADB);
        mapping.put("jdbc:tidb", DatabaseType.TIDB);
        mapping.put("jdbc:oceanbase", DatabaseType.OCEANBASE);
        mapping.put("jdbc:postgresql", DatabaseType.POSTGRESQL);
        mapping.put("jdbc:opengauss", DatabaseType.OPENGAUSS);
        mapping.put("jdbc:gaussdb", DatabaseType.GAUSSDB);
        mapping.put("jdbc:vastbase", DatabaseType.VASTBASE);
        mapping.put("jdbc:uxdb", DatabaseType.UXDB);
        mapping.put("jdbc:oracle", DatabaseType.ORACLE);
        mapping.put("jdbc:sqlserver", DatabaseType.SQLSERVER);
        mapping.put("jdbc:microsoft:sqlserver", DatabaseType.SQLSERVER);
        mapping.put("jdbc:dm", DatabaseType.DM);
        mapping.put("jdbc:gbase", DatabaseType.GBASE);
        mapping.put("jdbc:oscar", DatabaseType.OSCAR);
        mapping.put("jdbc:kingbase", DatabaseType.KINGBASE);
        mapping.put("jdbc:kingbase8", DatabaseType.KINGBASE);
        mapping.put("jdbc:clickhouse", DatabaseType.CLICKHOUSE);
        mapping.put("jdbc:doris", DatabaseType.DORIS);
        mapping.put("jdbc:starrocks", DatabaseType.STARROCKS);
        mapping.put("jdbc:h2", DatabaseType.H2);
        mapping.put("jdbc:sqlite", DatabaseType.SQLITE);
        return Collections.unmodifiableMap(mapping);
    }

    private static Map<String, DatabaseType> createTypeAliasMapping() {
        Map<String, DatabaseType> mapping = new LinkedHashMap<>();
        for (DatabaseType type : DatabaseType.values()) {
            mapping.put(normalizeTypeToken(type.name()), type);
        }

        mapping.put(normalizeTypeToken("kingbasees"), DatabaseType.KINGBASE);
        mapping.put(normalizeTypeToken("postgres"), DatabaseType.POSTGRESQL);
        mapping.put(normalizeTypeToken("mssql"), DatabaseType.SQLSERVER);
        mapping.put(normalizeTypeToken("dameng"), DatabaseType.DM);

        return Collections.unmodifiableMap(mapping);
    }

    private static String normalizeTypeToken(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
