package com.mumu.woodlin.sql2api.service;

import java.util.List;

import com.mumu.woodlin.common.datasource.model.ColumnMetadata;
import com.mumu.woodlin.common.datasource.model.DatabaseMetadata;
import com.mumu.woodlin.common.datasource.model.TableMetadata;

/**
 * 数据库元数据服务接口
 * 
 * @author mumu
 * @description 提供数据库元数据的查询和管理功能
 * @since 2025-01-01
 */
public interface DatabaseMetadataService {
    
    /**
     * 获取指定数据源的数据库元数据
     * 
     * @param datasourceName 数据源名称
     * @return 数据库元数据
     */
    DatabaseMetadata getDatabaseMetadata(String datasourceName);
    
    /**
     * 获取指定数据源的所有表信息
     * 
     * @param datasourceName 数据源名称
     * @return 表列表
     */
    List<TableMetadata> getTables(String datasourceName);
    
    /**
     * 获取指定表的列信息
     * 
     * @param datasourceName 数据源名称
     * @param tableName 表名称
     * @return 列列表
     */
    List<ColumnMetadata> getColumns(String datasourceName, String tableName);
    
    /**
     * 获取支持的数据库类型列表
     * 
     * @return 数据库类型列表
     */
    List<String> getSupportedDatabaseTypes();
    
    /**
     * 刷新元数据缓存
     * 
     * @param datasourceName 数据源名称
     */
    void refreshMetadataCache(String datasourceName);
}
