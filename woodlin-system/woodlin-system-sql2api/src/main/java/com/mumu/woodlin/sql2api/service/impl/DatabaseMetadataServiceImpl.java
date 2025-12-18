package com.mumu.woodlin.sql2api.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.DatabaseMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.service.DatabaseMetadataService;
import com.mumu.woodlin.sql2api.service.Sql2ApiDataSourceService;
import com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor;

/**
 * 数据库元数据服务实现
 * 
 * @author mumu
 * @description 实现数据库元数据的查询和管理功能，支持多数据源和缓存
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMetadataServiceImpl implements DatabaseMetadataService {
    
    private final DataSource dataSource;
    private final List<DatabaseMetadataExtractor> metadataExtractors;
    private final Sql2ApiDataSourceService sql2ApiDataSourceService;
    
    @Override
    @Cacheable(value = "databaseMetadata", key = "#datasourceName")
    public DatabaseMetadata getDatabaseMetadata(String datasourceName) {
        log.info("获取数据源 {} 的元数据", datasourceName);
        
        try {
            DataSource targetDataSource = getTargetDataSource(datasourceName);
            DatabaseMetadataExtractor extractor = findExtractor(targetDataSource);
            
            if (extractor == null) {
                log.error("未找到适配的元数据提取器，数据源: {}", datasourceName);
                throw new BusinessException("不支持的数据库类型");
            }
            
            return extractor.extractDatabaseMetadata(targetDataSource);
            
        } catch (SQLException e) {
            log.error("获取数据库元数据失败，数据源: {}", datasourceName, e);
            throw new BusinessException("获取数据库元数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Cacheable(value = "databaseTables", key = "#datasourceName")
    public List<TableMetadata> getTables(String datasourceName) {
        log.info("获取数据源 {} 的表列表", datasourceName);
        
        try {
            DataSource targetDataSource = getTargetDataSource(datasourceName);
            DatabaseMetadataExtractor extractor = findExtractor(targetDataSource);
            
            if (extractor == null) {
                throw new BusinessException("不支持的数据库类型");
            }
            
            try (Connection connection = targetDataSource.getConnection()) {
                String databaseName = connection.getCatalog();
                String schemaName = safeSchema(connection);
                return extractor.extractTables(connection, databaseName, schemaName);
            }
            
        } catch (SQLException e) {
            log.error("获取表列表失败，数据源: {}", datasourceName, e);
            throw new BusinessException("获取表列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Cacheable(value = "tableColumns", key = "#datasourceName + ':' + #tableName")
    public List<ColumnMetadata> getColumns(String datasourceName, String tableName) {
        log.info("获取数据源 {} 表 {} 的列信息", datasourceName, tableName);
        
        try {
            DataSource targetDataSource = getTargetDataSource(datasourceName);
            DatabaseMetadataExtractor extractor = findExtractor(targetDataSource);
            
            if (extractor == null) {
                throw new BusinessException("不支持的数据库类型");
            }
            
            try (Connection connection = targetDataSource.getConnection()) {
                String databaseName = connection.getCatalog();
                String schemaName = safeSchema(connection);
                return extractor.extractColumns(connection, databaseName, schemaName, tableName);
            }
            
        } catch (SQLException e) {
            log.error("获取列信息失败，数据源: {}, 表: {}", datasourceName, tableName, e);
            throw new BusinessException("获取列信息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> getSupportedDatabaseTypes() {
        return metadataExtractors.stream()
                .map(DatabaseMetadataExtractor::getDatabaseType)
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    @CacheEvict(value = {"databaseMetadata", "databaseTables", "tableColumns"}, allEntries = true)
    public void refreshMetadataCache(String datasourceName) {
        log.info("刷新数据源 {} 的元数据缓存", datasourceName);
    }
    
    /**
     * 获取目标数据源
     */
    private DataSource getTargetDataSource(String datasourceName) {
        try {
            return sql2ApiDataSourceService.getDataSourceByCode(datasourceName);
        } catch (BusinessException ignore) {
            // fallback to dynamic routing if configured
            if (dataSource instanceof DynamicRoutingDataSource dynamicDataSource) {
                DataSource targetDs = dynamicDataSource.getDataSource(datasourceName);
                if (targetDs != null) {
                    return targetDs;
                }
            }
            throw ignore;
        }
    }
    
    /**
     * 查找合适的元数据提取器
     */
    private DatabaseMetadataExtractor findExtractor(DataSource ds) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            // 按优先级排序
            List<DatabaseMetadataExtractor> sortedExtractors = metadataExtractors.stream()
                    .sorted(Comparator.comparingInt(DatabaseMetadataExtractor::getPriority))
                    .toList();
            
            for (DatabaseMetadataExtractor extractor : sortedExtractors) {
                if (extractor.supports(connection)) {
                    log.info("找到匹配的元数据提取器: {}", extractor.getDatabaseType());
                    return extractor;
                }
            }
        }
        
        return null;
    }

    private String safeSchema(Connection connection) throws SQLException {
        try {
            return connection.getSchema();
        } catch (SQLFeatureNotSupportedException e) {
            return null;
        }
    }
}
