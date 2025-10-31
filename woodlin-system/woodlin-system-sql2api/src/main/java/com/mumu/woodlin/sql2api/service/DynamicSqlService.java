package com.mumu.woodlin.sql2api.service;

import java.util.List;
import java.util.Map;

import com.mumu.woodlin.sql2api.entity.SqlApiConfig;

/**
 * 动态SQL执行服务接口
 * 
 * @author mumu
 * @description 基于SQL配置动态执行SQL并返回结果
 * @since 2025-01-01
 */
public interface DynamicSqlService {
    
    /**
     * 执行SQL查询（单条）
     * 
     * @param config SQL API配置
     * @param params 请求参数
     * @return 查询结果
     */
    Map<String, Object> executeSingleQuery(SqlApiConfig config, Map<String, Object> params);
    
    /**
     * 执行SQL查询（列表）
     * 
     * @param config SQL API配置
     * @param params 请求参数
     * @return 查询结果列表
     */
    List<Map<String, Object>> executeListQuery(SqlApiConfig config, Map<String, Object> params);
    
    /**
     * 执行SQL查询（分页）
     * 
     * @param config SQL API配置
     * @param params 请求参数
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Map<String, Object> executePageQuery(SqlApiConfig config, Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     * 
     * @param config SQL API配置
     * @param params 请求参数
     * @return 影响行数
     */
    int executeUpdate(SqlApiConfig config, Map<String, Object> params);
    
    /**
     * 批量执行SQL更新
     * 
     * @param config SQL API配置
     * @param batchParams 批量参数
     * @return 影响行数数组
     */
    int[] executeBatchUpdate(SqlApiConfig config, List<Map<String, Object>> batchParams);
}
