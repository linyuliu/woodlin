package com.mumu.woodlin.datasource.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.SchemaMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractor;
import com.mumu.woodlin.common.datasource.spi.DatabaseMetadataExtractorFactory;
import com.mumu.woodlin.common.exception.BusinessException;

/**
 * 数据库元数据服务
 * <p>
 * 提供数据库元数据提取功能，集成woodlin-common中的元数据提取器基础设施。
 * 支持多种数据库类型的Schema、表、字段等元数据信息获取。
 * </p>
 * 
 * @author mumu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMetadataService {
    
    private final InfraDatasourceService datasourceService;
    private final DatabaseMetadataExtractorFactory extractorFactory = DatabaseMetadataExtractorFactory.getInstance();
    
    /**
     * 获取数据源的完整元数据
     * 
     * @param datasourceCode 数据源编码
     * @return 数据库元数据
     */
    @Cacheable(value = "infraDatabaseMetadata", key = "#datasourceCode")
    public DatabaseMetadata getDatabaseMetadata(String datasourceCode) {
        log.info("获取数据源 {} 的元数据", datasourceCode);
        
        try {
            DataSource dataSource = datasourceService.getDataSourceByCode(datasourceCode);
            DatabaseMetadataExtractor extractor = findExtractor(dataSource);
            
            return extractor.extractDatabaseMetadata(dataSource);
            
        } catch (SQLException e) {
            log.error("获取数据库元数据失败，数据源: {}", datasourceCode, e);
            throw new BusinessException("获取数据库元数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取数据源的Schema列表
     * <p>
     * 注意：某些数据库（如MySQL）不支持Schema概念，会返回空列表
     * </p>
     * 
     * @param datasourceCode 数据源编码
     * @return Schema列表
     */
    @Cacheable(value = "infraDatabaseSchemas", key = "#datasourceCode")
    public List<SchemaMetadata> getSchemas(String datasourceCode) {
        log.info("获取数据源 {} 的Schema列表", datasourceCode);
        
        try {
            DataSource dataSource = datasourceService.getDataSourceByCode(datasourceCode);
            DatabaseMetadataExtractor extractor = findExtractor(dataSource);
            
            try (Connection connection = dataSource.getConnection()) {
                String databaseName = connection.getCatalog();
                return extractor.extractSchemas(connection, databaseName);
            }
            
        } catch (SQLException e) {
            log.error("获取Schema列表失败，数据源: {}", datasourceCode, e);
            throw new BusinessException("获取Schema列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取数据源的表列表
     * 
     * @param datasourceCode 数据源编码
     * @return 表列表
     */
    @Cacheable(value = "infraDatabaseTables", key = "#datasourceCode")
    public List<TableMetadata> getTables(String datasourceCode) {
        log.info("获取数据源 {} 的表列表", datasourceCode);
        
        try {
            DataSource dataSource = datasourceService.getDataSourceByCode(datasourceCode);
            DatabaseMetadataExtractor extractor = findExtractor(dataSource);
            
            try (Connection connection = dataSource.getConnection()) {
                String databaseName = connection.getCatalog();
                String schemaName = getSchema(connection);
                return extractor.extractTables(connection, databaseName, schemaName);
            }
            
        } catch (SQLException e) {
            log.error("获取表列表失败，数据源: {}", datasourceCode, e);
            throw new BusinessException("获取表列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取指定表的字段列表
     * 
     * @param datasourceCode 数据源编码
     * @param tableName 表名
     * @return 字段列表
     */
    @Cacheable(value = "infraTableColumns", key = "#datasourceCode + ':' + #tableName")
    public List<ColumnMetadata> getColumns(String datasourceCode, String tableName) {
        log.info("获取数据源 {} 表 {} 的字段列表", datasourceCode, tableName);
        
        try {
            DataSource dataSource = datasourceService.getDataSourceByCode(datasourceCode);
            DatabaseMetadataExtractor extractor = findExtractor(dataSource);
            
            try (Connection connection = dataSource.getConnection()) {
                String databaseName = connection.getCatalog();
                String schemaName = getSchema(connection);
                return extractor.extractColumns(connection, databaseName, schemaName, tableName);
            }
            
        } catch (SQLException e) {
            log.error("获取字段列表失败，数据源: {}, 表: {}", datasourceCode, tableName, e);
            throw new BusinessException("获取字段列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 刷新元数据缓存
     * 
     * @param datasourceCode 数据源编码
     */
    @CacheEvict(value = {"infraDatabaseMetadata", "infraDatabaseSchemas", "infraDatabaseTables", "infraTableColumns"}, 
                allEntries = true)
    public void refreshMetadataCache(String datasourceCode) {
        log.info("刷新数据源 {} 的元数据缓存", datasourceCode);
    }
    
    /**
     * 查找合适的元数据提取器
     * 
     * @param dataSource 数据源
     * @return 元数据提取器
     * @throws SQLException SQL异常
     */
    private DatabaseMetadataExtractor findExtractor(DataSource dataSource) throws SQLException {
        return extractorFactory.getExtractor(dataSource)
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
