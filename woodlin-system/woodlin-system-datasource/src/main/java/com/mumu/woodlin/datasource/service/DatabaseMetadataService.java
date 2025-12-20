package com.mumu.woodlin.datasource.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractorFactory;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import com.mumu.woodlin.datasource.mapper.InfraDatasourceMapper;

/**
 * 数据库元数据服务
 * <p>
 * 提供数据库元数据提取功能，集成woodlin-common中的元数据提取器基础设施。
 * 支持多种数据库类型的Schema、表、字段等元数据信息获取。
 * </p>
 * <p>
 * 优化说明：本服务使用轻量级连接池（HikariCP）来提高元数据提取性能，
 * 避免每次都创建新连接导致的延迟。连接池针对元数据提取场景进行了优化配置。
 * </p>
 * 
 * @author mumu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMetadataService {
    
    private final InfraDatasourceMapper datasourceMapper;
    private final DatabaseMetadataExtractorFactory extractorFactory = DatabaseMetadataExtractorFactory.getInstance();
    
    /**
     * 元数据连接池缓存
     * <p>
     * 为每个数据源维护一个轻量级连接池，用于元数据提取操作。
     * 连接池配置了较小的最大连接数和较短的超时时间。
     * </p>
     */
    private final Map<String, HikariDataSource> metadataDataSources = new ConcurrentHashMap<>();
    
    /**
     * 获取数据源的完整元数据
     * <p>
     * 使用连接池优化性能，避免每次创建连接的开销
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return 数据库元数据
     */
    public DatabaseMetadata getDatabaseMetadata(String datasourceCode) {
        log.info("获取数据源 {} 的元数据", datasourceCode);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        HikariDataSource dataSource = getOrCreateMetadataDataSource(config);
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetadataExtractor extractor = findExtractor(connection);
            
            // 使用Connection直接提取元数据
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            DatabaseMetadata metadata = DatabaseMetadata.builder()
                    .databaseName(connection.getCatalog())
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .majorVersion(metaData.getDatabaseMajorVersion())
                    .minorVersion(metaData.getDatabaseMinorVersion())
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .supportsSchemas(metaData.supportsSchemasInTableDefinitions())
                    .build();
            
            return metadata;
            
        } catch (SQLException e) {
            log.error("获取数据库元数据失败，数据源: {}", datasourceCode, e);
            throw new BusinessException("获取数据库元数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取数据源的Schema列表
     * <p>
     * 使用连接池优化性能
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return Schema列表
     */
    public List<SchemaMetadata> getSchemas(String datasourceCode) {
        log.info("获取数据源 {} 的Schema列表", datasourceCode);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        HikariDataSource dataSource = getOrCreateMetadataDataSource(config);
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetadataExtractor extractor = findExtractor(connection);
            String databaseName = connection.getCatalog();
            return extractor.extractSchemas(connection, databaseName);
            
        } catch (SQLException e) {
            log.error("获取Schema列表失败，数据源: {}", datasourceCode, e);
            throw new BusinessException("获取Schema列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取数据源的表列表
     * <p>
     * 使用连接池优化性能
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return 表列表
     */
    public List<TableMetadata> getTables(String datasourceCode) {
        log.info("获取数据源 {} 的表列表", datasourceCode);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        HikariDataSource dataSource = getOrCreateMetadataDataSource(config);
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetadataExtractor extractor = findExtractor(connection);
            String databaseName = connection.getCatalog();
            String schemaName = getSchema(connection);
            return extractor.extractTables(connection, databaseName, schemaName);
            
        } catch (SQLException e) {
            log.error("获取表列表失败，数据源: {}", datasourceCode, e);
            throw new BusinessException("获取表列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取指定表的字段列表
     * <p>
     * 使用连接池优化性能
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @param tableName 表名
     * @return 字段列表
     */
    public List<ColumnMetadata> getColumns(String datasourceCode, String tableName) {
        log.info("获取数据源 {} 表 {} 的字段列表", datasourceCode, tableName);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        HikariDataSource dataSource = getOrCreateMetadataDataSource(config);
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetadataExtractor extractor = findExtractor(connection);
            String databaseName = connection.getCatalog();
            String schemaName = getSchema(connection);
            return extractor.extractColumns(connection, databaseName, schemaName, tableName);
            
        } catch (SQLException e) {
            log.error("获取字段列表失败，数据源: {}, 表: {}", datasourceCode, tableName, e);
            throw new BusinessException("获取字段列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 刷新元数据缓存
     * <p>
     * 关闭并重新创建数据源连接池，用于处理数据源配置变更的情况
     * </p>
     * 
     * @param datasourceCode 数据源编码
     */
    public void refreshMetadataCache(String datasourceCode) {
        log.info("刷新数据源 {} 的元数据缓存", datasourceCode);
        
        HikariDataSource dataSource = metadataDataSources.remove(datasourceCode);
        if (dataSource != null) {
            dataSource.close();
            log.debug("已关闭数据源 {} 的元数据连接池", datasourceCode);
        }
    }
    
    /**
     * 获取数据源配置
     * 
     * @param datasourceCode 数据源编码
     * @return 数据源配置
     */
    private InfraDatasourceConfig getDatasourceConfig(String datasourceCode) {
        InfraDatasourceConfig config = datasourceMapper.selectOne(
                new QueryWrapper<InfraDatasourceConfig>().eq("datasource_code", datasourceCode)
        );
        
        if (config == null) {
            throw new BusinessException("数据源不存在: " + datasourceCode);
        }
        
        if (config.getStatus() != null && config.getStatus() == 0) {
            throw new BusinessException("数据源已禁用: " + datasourceCode);
        }
        
        return config;
    }
    
    /**
     * 获取或创建元数据连接池
     * <p>
     * 为元数据提取操作创建优化配置的连接池：
     * - 最小连接数: 1（保持一个活跃连接以避免重复建立连接）
     * - 最大连接数: 3（元数据提取为低并发操作）
     * - 连接超时: 10秒（避免长时间等待）
     * - 空闲超时: 5分钟（及时释放不用的连接）
     * - 最大生命周期: 30分钟（定期刷新连接）
     * </p>
     * 
     * @param config 数据源配置
     * @return HikariCP数据源
     */
    private HikariDataSource getOrCreateMetadataDataSource(InfraDatasourceConfig config) {
        return metadataDataSources.computeIfAbsent(config.getDatasourceCode(), code -> {
            log.info("为数据源 {} 创建元数据连接池", code);
            
            try {
                HikariConfig hikariConfig = new HikariConfig();
                
                // 加载驱动类
                String driverClass = resolveDriver(config.getDriverClass(), config.getJdbcUrl());
                hikariConfig.setDriverClassName(driverClass);
                
                // 基本连接信息
                hikariConfig.setJdbcUrl(config.getJdbcUrl());
                hikariConfig.setUsername(config.getUsername());
                hikariConfig.setPassword(config.getPassword());
                
                // 连接池优化配置（针对元数据提取场景）
                hikariConfig.setMinimumIdle(1);  // 保持至少一个空闲连接
                hikariConfig.setMaximumPoolSize(3);  // 最大3个连接，元数据提取并发度低
                hikariConfig.setConnectionTimeout(10000);  // 10秒连接超时，避免长时间等待
                hikariConfig.setIdleTimeout(300000);  // 5分钟空闲超时
                hikariConfig.setMaxLifetime(1800000);  // 30分钟最大生命周期
                hikariConfig.setValidationTimeout(5000);  // 5秒验证超时
                
                // 连接池名称（便于监控和调试）
                hikariConfig.setPoolName("MetadataPool-" + code);
                
                // 连接测试配置
                hikariConfig.setConnectionTestQuery(resolveTestQuery(config));
                
                // 禁用自动提交（元数据查询为只读操作）
                hikariConfig.setAutoCommit(false);
                hikariConfig.setReadOnly(true);
                
                // 初始化连接池
                HikariDataSource dataSource = new HikariDataSource(hikariConfig);
                
                // 验证连接可用性
                try (Connection conn = dataSource.getConnection()) {
                    log.info("成功创建并验证元数据连接池: {}", code);
                }
                
                return dataSource;
                
            } catch (Exception e) {
                log.error("创建元数据连接池失败，数据源: {}", code, e);
                throw new BusinessException("创建元数据连接池失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 解析测试查询SQL
     * 
     * @param config 数据源配置
     * @return 测试SQL
     */
    private String resolveTestQuery(InfraDatasourceConfig config) {
        if (config.getTestSql() != null && !config.getTestSql().isEmpty()) {
            return config.getTestSql();
        }
        
        String databaseType = config.getDatasourceType();
        if (databaseType != null && !databaseType.isEmpty()) {
            String testSql = extractorFactory.getDefaultTestQuery(databaseType);
            if (testSql != null) {
                return testSql;
            }
        }
        
        return "SELECT 1";
    }
    
    /**
     * 清理所有元数据连接池
     * <p>
     * 在应用关闭时自动调用
     * </p>
     */
    @PreDestroy
    public void closeAllMetadataDataSources() {
        log.info("关闭所有元数据连接池，共 {} 个", metadataDataSources.size());
        metadataDataSources.values().forEach(dataSource -> {
            try {
                dataSource.close();
            } catch (Exception e) {
                log.warn("关闭元数据连接池失败", e);
            }
        });
        metadataDataSources.clear();
    }
    
    /**
     * 创建原生JDBC连接（已废弃，使用连接池代替）
     * <p>
     * 保留此方法仅用于兼容性，新代码应使用 getOrCreateMetadataDataSource
     * </p>
     * 
     * @param config 数据源配置
     * @return JDBC连接
     * @throws SQLException SQL异常
     * @deprecated 使用 {@link #getOrCreateMetadataDataSource(InfraDatasourceConfig)} 代替
     */
    @Deprecated
    private Connection createJdbcConnection(InfraDatasourceConfig config) throws SQLException {
        try {
            // 加载驱动类
            String driverClass = resolveDriver(config.getDriverClass(), config.getJdbcUrl());
            Class.forName(driverClass);
            
            // 设置连接超时属性
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("connectTimeout", "10000");  // 10秒连接超时
            props.setProperty("socketTimeout", "30000");  // 30秒套接字超时
            
            // 使用DriverManager创建原生JDBC连接
            DriverManager.setLoginTimeout(10);  // 设置全局登录超时
            Connection connection = DriverManager.getConnection(
                    config.getJdbcUrl(),
                    props
            );
            
            // 设置只读模式和查询超时
            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            
            log.debug("成功创建JDBC连接: {}", config.getJdbcUrl());
            return connection;
            
        } catch (ClassNotFoundException e) {
            log.error("找不到JDBC驱动: {}", config.getDriverClass(), e);
            throw new BusinessException("找不到JDBC驱动: " + config.getDriverClass(), e);
        } catch (SQLException e) {
            log.error("创建JDBC连接失败: {}", config.getJdbcUrl(), e);
            throw new BusinessException("创建数据库连接失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析驱动类名
     * <p>
     * 优先使用配置的驱动类名，如果未配置则通过DatabaseMetadataExtractorFactory
     * 根据JDBC URL推断数据库类型并获取对应的驱动类名
     * </p>
     * 
     * @param driverClassName 配置的驱动类名
     * @param jdbcUrl JDBC URL
     * @return 驱动类全限定名
     */
    private String resolveDriver(String driverClassName, String jdbcUrl) {
        // 如果已配置驱动类名，直接返回
        if (driverClassName != null && !driverClassName.isEmpty()) {
            return driverClassName;
        }
        
        // 通过JDBC URL推断数据库类型
        String databaseType = extractorFactory.inferDatabaseType(jdbcUrl);
        if (databaseType == null) {
            throw new BusinessException("无法识别JDBC URL的数据库类型，请指定 driverClass");
        }
        
        // 获取该数据库类型的默认驱动类名
        String defaultDriver = extractorFactory.getDefaultDriverClass(databaseType);
        if (defaultDriver == null) {
            throw new BusinessException("无法获取数据库类型 " + databaseType + " 的驱动类名，请指定 driverClass");
        }
        
        log.debug("根据JDBC URL推断数据库类型: {}, 使用驱动: {}", databaseType, defaultDriver);
        return defaultDriver;
    }
    
    /**
     * 查找合适的元数据提取器
     * 
     * @param connection 数据库连接
     * @return 元数据提取器
     * @throws SQLException SQL异常
     */
    private DatabaseMetadataExtractor findExtractor(Connection connection) throws SQLException {
        return extractorFactory.getExtractor(connection)
                .orElseThrow(() -> new BusinessException("不支持的数据库类型，无法提取元数据"));
    }
    
    /**
     * 安全地获取Schema名称
     * 
     * @param connection 数据库连接
     * @return Schema名称，如果不支持则返回null
     */
    private String getSchema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (SQLException e) {
            // Some databases don't support schema concept or getSchema() operation
            log.debug("Failed to get schema from connection: {}", e.getMessage());
            return null;
        }
    }
}
