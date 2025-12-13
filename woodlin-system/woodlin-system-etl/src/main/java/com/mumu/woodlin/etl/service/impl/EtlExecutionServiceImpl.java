package com.mumu.woodlin.etl.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.enums.SyncMode;
import com.mumu.woodlin.etl.service.IEtlExecutionLogService;
import com.mumu.woodlin.etl.service.IEtlExecutionService;
import com.mumu.woodlin.sql2api.model.ColumnMetadata;
import com.mumu.woodlin.sql2api.model.TableMetadata;
import com.mumu.woodlin.sql2api.service.DatabaseMetadataService;

/**
 * ETL执行服务实现
 * 
 * @author mumu
 * @description ETL任务执行服务实现，负责实际的数据抽取、转换、加载操作
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlExecutionServiceImpl implements IEtlExecutionService {
    
    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final DatabaseMetadataService databaseMetadataService;
    private final IEtlExecutionLogService executionLogService;
    
    @Async
    @Override
    public void execute(EtlJob job) {
        log.info("开始执行ETL任务: {}", job.getJobName());
        
        Long logId = executionLogService.recordExecutionStart(job);
        
        long extractedRows = 0;
        long transformedRows = 0;
        long loadedRows = 0;
        long failedRows = 0;
        
        try {
            // 1. Extract - 数据提取
            List<Map<String, Object>> extractedData = extractData(job);
            extractedRows = extractedData.size();
            log.info("提取数据完成，记录数: {}", extractedRows);
            
            // 2. Transform - 数据转换
            List<Map<String, Object>> transformedData = transformData(job, extractedData);
            transformedRows = transformedData.size();
            log.info("转换数据完成，记录数: {}", transformedRows);
            
            // 3. Load - 数据加载
            loadedRows = loadData(job, transformedData);
            log.info("加载数据完成，记录数: {}", loadedRows);
            
            // 记录执行成功
            String executionDetail = String.format(
                "提取: %d条, 转换: %d条, 加载: %d条",
                extractedRows, transformedRows, loadedRows
            );
            executionLogService.recordExecutionSuccess(
                logId, extractedRows, transformedRows, loadedRows, executionDetail
            );
            
            log.info("ETL任务执行成功: {}", job.getJobName());
            
        } catch (Exception e) {
            log.error("ETL任务执行失败: {}", job.getJobName(), e);
            
            // 记录执行失败
            failedRows = extractedRows - loadedRows;
            executionLogService.recordExecutionFailure(
                logId, extractedRows, transformedRows, loadedRows, failedRows, 
                e.getMessage()
            );
        }
    }
    
    /**
     * 数据提取
     */
    private List<Map<String, Object>> extractData(EtlJob job) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        
        DataSource sourceDataSource = dynamicRoutingDataSource.getDataSource(job.getSourceDatasource());
        if (sourceDataSource == null) {
            throw new IllegalArgumentException("源数据源不存在: " + job.getSourceDatasource());
        }
        
        try (Connection conn = sourceDataSource.getConnection()) {
            String query = buildExtractQuery(job);
            log.debug("提取SQL: {}", query);
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 构建提取查询SQL
     */
    private String buildExtractQuery(EtlJob job) {
        // 如果配置了自定义查询SQL，直接使用
        if (job.getSourceQuery() != null && !job.getSourceQuery().trim().isEmpty()) {
            String query = job.getSourceQuery();
            
            // 如果配置了过滤条件，添加到WHERE子句
            if (job.getFilterCondition() != null && !job.getFilterCondition().trim().isEmpty()) {
                if (query.toUpperCase().contains("WHERE")) {
                    query += " AND " + job.getFilterCondition();
                } else {
                    query += " WHERE " + job.getFilterCondition();
                }
            }
            
            return query;
        }
        
        // 否则根据表名构建查询
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        
        // 添加Schema（如果有）
        if (job.getSourceSchema() != null && !job.getSourceSchema().trim().isEmpty()) {
            sql.append(job.getSourceSchema()).append(".");
        }
        
        sql.append(job.getSourceTable());
        
        // 添加过滤条件
        if (job.getFilterCondition() != null && !job.getFilterCondition().trim().isEmpty()) {
            sql.append(" WHERE ").append(job.getFilterCondition());
        }
        
        // 增量同步逻辑
        if (SyncMode.INCREMENTAL.getCode().equals(job.getSyncMode()) 
                && job.getIncrementalColumn() != null) {
            if (job.getFilterCondition() != null && !job.getFilterCondition().trim().isEmpty()) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append(job.getIncrementalColumn())
               .append(" > (SELECT MAX(").append(job.getIncrementalColumn())
               .append(") FROM ");
            
            if (job.getTargetSchema() != null && !job.getTargetSchema().trim().isEmpty()) {
                sql.append(job.getTargetSchema()).append(".");
            }
            sql.append(job.getTargetTable()).append(")");
        }
        
        return sql.toString();
    }
    
    /**
     * 数据转换
     */
    private List<Map<String, Object>> transformData(EtlJob job, List<Map<String, Object>> data) {
        // 如果没有字段映射配置，直接返回原数据
        if (job.getColumnMapping() == null || job.getColumnMapping().trim().isEmpty()) {
            return data;
        }
        
        // TODO: 实现字段映射和数据转换逻辑
        // 这里可以解析JSON格式的columnMapping配置
        // 例如: {"source_col1": "target_col1", "source_col2": "target_col2"}
        
        return data;
    }
    
    /**
     * 数据加载
     */
    private long loadData(EtlJob job, List<Map<String, Object>> data) throws Exception {
        if (data.isEmpty()) {
            return 0;
        }
        
        DataSource targetDataSource = dynamicRoutingDataSource.getDataSource(job.getTargetDatasource());
        if (targetDataSource == null) {
            throw new IllegalArgumentException("目标数据源不存在: " + job.getTargetDatasource());
        }
        
        long loadedRows = 0;
        int batchSize = job.getBatchSize();
        
        try (Connection conn = targetDataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            String insertSql = buildInsertSql(job, data.get(0));
            log.debug("插入SQL: {}", insertSql);
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                int batchCount = 0;
                
                for (Map<String, Object> row : data) {
                    int paramIndex = 1;
                    for (Object value : row.values()) {
                        pstmt.setObject(paramIndex++, value);
                    }
                    pstmt.addBatch();
                    batchCount++;
                    
                    // 达到批次大小，执行批处理
                    if (batchCount >= batchSize) {
                        int[] results = pstmt.executeBatch();
                        loadedRows += results.length;
                        conn.commit();
                        batchCount = 0;
                        log.debug("批量插入 {} 条记录", results.length);
                    }
                }
                
                // 执行剩余的批处理
                if (batchCount > 0) {
                    int[] results = pstmt.executeBatch();
                    loadedRows += results.length;
                    conn.commit();
                    log.debug("批量插入 {} 条记录", results.length);
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        
        return loadedRows;
    }
    
    /**
     * 构建插入SQL
     */
    private String buildInsertSql(EtlJob job, Map<String, Object> sampleRow) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        
        // 添加Schema（如果有）
        if (job.getTargetSchema() != null && !job.getTargetSchema().trim().isEmpty()) {
            sql.append(job.getTargetSchema()).append(".");
        }
        
        sql.append(job.getTargetTable()).append(" (");
        
        // 添加列名
        List<String> columns = new ArrayList<>(sampleRow.keySet());
        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        
        // 添加占位符
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        
        return sql.toString();
    }
}
