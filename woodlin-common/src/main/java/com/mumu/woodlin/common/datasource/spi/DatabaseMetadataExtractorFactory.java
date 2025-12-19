package com.mumu.woodlin.common.datasource.spi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

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
import org.springframework.context.annotation.ComponentScan;

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
@ComponentScan(basePackages = "com.mumu.woodlin.sql2api")
public class DatabaseMetadataExtractorFactory {

    private static final DatabaseMetadataExtractorFactory INSTANCE = new DatabaseMetadataExtractorFactory();

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
        if (databaseType == null || databaseType.isEmpty()) {
            return Optional.empty();
        }

        String type = databaseType.toLowerCase();

        return extractors.stream()
                .filter(e -> e.getDatabaseType().name().toLowerCase().contains(type))
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
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return null;
        }

        String url = jdbcUrl.toLowerCase();

        if (url.startsWith("jdbc:mysql")) {
            return "MySQL";
        } else if (url.startsWith("jdbc:mariadb")) {
            return "MariaDB";
        } else if (url.startsWith("jdbc:postgresql")) {
            return "PostgreSQL";
        } else if (url.startsWith("jdbc:oracle")) {
            return "Oracle";
        } else if (url.startsWith("jdbc:sqlserver") || url.startsWith("jdbc:microsoft:sqlserver")) {
            return "SQLServer";
        } else if (url.startsWith("jdbc:dm")) {
            return "DM";
        } else if (url.startsWith("jdbc:kingbase") || url.startsWith("jdbc:kingbase8")) {
            return "KingbaseES";
        } else if (url.startsWith("jdbc:h2")) {
            return "H2";
        } else if (url.startsWith("jdbc:sqlite")) {
            return "SQLite";
        }

        return null;
    }
}
