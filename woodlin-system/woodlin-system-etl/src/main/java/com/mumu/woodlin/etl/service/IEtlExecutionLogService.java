package com.mumu.woodlin.etl.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.etl.entity.EtlExecutionLog;
import com.mumu.woodlin.etl.entity.EtlJob;

/**
 * ETL执行历史服务接口
 * 
 * @author mumu
 * @description ETL执行历史管理服务接口
 * @since 2025-01-01
 */
public interface IEtlExecutionLogService extends IService<EtlExecutionLog> {
    
    /**
     * 记录执行开始
     * 
     * @param job ETL任务
     * @return 执行日志ID
     */
    Long recordExecutionStart(EtlJob job);
    
    /**
     * 记录执行成功
     * 
     * @param logId 执行日志ID
     * @param extractedRows 提取记录数
     * @param transformedRows 转换记录数
     * @param loadedRows 加载记录数
     * @param executionDetail 执行详情
     */
    void recordExecutionSuccess(Long logId, Long extractedRows, Long transformedRows, 
                                Long loadedRows, String executionDetail);
    
    /**
     * 记录执行失败
     * 
     * @param logId 执行日志ID
     * @param extractedRows 提取记录数
     * @param transformedRows 转换记录数
     * @param loadedRows 加载记录数
     * @param failedRows 失败记录数
     * @param errorMessage 错误信息
     */
    void recordExecutionFailure(Long logId, Long extractedRows, Long transformedRows, 
                                Long loadedRows, Long failedRows, String errorMessage);
}
