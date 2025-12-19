package com.mumu.woodlin.datasource.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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
 * 注意：本服务使用原生JDBC连接（DriverManager）而非连接池，因为元数据提取通常是
 * 低频操作，不需要连接池的性能优化，且使用原生连接可以避免连接池管理的复杂性。
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
     * 获取数据源的完整元数据
     * <p>
     * 注意：此方法不使用缓存，因为元数据可能会变化
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return 数据库元数据
     */
    public DatabaseMetadata getDatabaseMetadata(String datasourceCode) {
        log.info("获取数据源 {} 的元数据", datasourceCode);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        
        try (Connection connection = createJdbcConnection(config)) {
            DatabaseMetadataExtractor extractor = findExtractor(connection);
            
            // 使用Connection直接提取元数据，避免创建DataSource
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
     * 注意：某些数据库（如MySQL）不支持Schema概念，会返回空列表
     * 此方法不使用缓存，因为Schema可能会变化
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return Schema列表
     */
    public List<SchemaMetadata> getSchemas(String datasourceCode) {
        log.info("获取数据源 {} 的Schema列表", datasourceCode);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        
        try (Connection connection = createJdbcConnection(config)) {
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
     * 此方法不使用缓存，因为表列表可能会变化
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return 表列表
     */
    public List<TableMetadata> getTables(String datasourceCode) {
        log.info("获取数据源 {} 的表列表", datasourceCode);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        
        try (Connection connection = createJdbcConnection(config)) {
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
     * 此方法不使用缓存，因为字段列表可能会变化
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @param tableName 表名
     * @return 字段列表
     */
    public List<ColumnMetadata> getColumns(String datasourceCode, String tableName) {
        log.info("获取数据源 {} 表 {} 的字段列表", datasourceCode, tableName);
        
        InfraDatasourceConfig config = getDatasourceConfig(datasourceCode);
        
        try (Connection connection = createJdbcConnection(config)) {
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
     * 注意：元数据提取不再使用缓存，此方法保留用于兼容性
     * </p>
     * 
     * @param datasourceCode 数据源编码
     */
    @Deprecated
    public void refreshMetadataCache(String datasourceCode) {
        log.info("刷新数据源 {} 的元数据缓存（已废弃，元数据不再使用缓存）", datasourceCode);
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
     * 创建原生JDBC连接
     * <p>
     * 使用DriverManager创建简单的JDBC连接，不使用连接池。
     * 元数据提取是低频操作，不需要连接池的性能优化。
     * </p>
     * 
     * @param config 数据源配置
     * @return JDBC连接
     * @throws SQLException SQL异常
     */
    private Connection createJdbcConnection(InfraDatasourceConfig config) throws SQLException {
        try {
            // 加载驱动类
            String driverClass = resolveDriver(config.getDriverClass(), config.getJdbcUrl());
            Class.forName(driverClass);
            
            // 使用DriverManager创建原生JDBC连接
            Connection connection = DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.getUsername(),
                    config.getPassword()
            );
            
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
