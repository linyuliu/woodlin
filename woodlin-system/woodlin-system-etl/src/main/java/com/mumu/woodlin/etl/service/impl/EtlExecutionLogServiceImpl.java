package com.mumu.woodlin.etl.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.etl.entity.EtlExecutionLog;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.enums.EtlExecutionStatus;
import com.mumu.woodlin.etl.mapper.EtlExecutionLogMapper;
import com.mumu.woodlin.etl.service.IEtlExecutionLogService;

/**
 * ETL执行历史服务实现
 * 
 * @author mumu
 * @description ETL执行历史管理服务实现
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlExecutionLogServiceImpl extends ServiceImpl<EtlExecutionLogMapper, EtlExecutionLog> 
        implements IEtlExecutionLogService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordExecutionStart(EtlJob job) {
        EtlExecutionLog log = new EtlExecutionLog();
        log.setJobId(job.getJobId());
        log.setJobName(job.getJobName());
        log.setExecutionStatus(EtlExecutionStatus.RUNNING.getCode());
        log.setStartTime(LocalDateTime.now());
        log.setTenantId(job.getTenantId());
        log.setExtractedRows(0L);
        log.setTransformedRows(0L);
        log.setLoadedRows(0L);
        log.setFailedRows(0L);
        
        this.save(log);
        return log.getLogId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordExecutionSuccess(Long logId, Long extractedRows, Long transformedRows, 
                                       Long loadedRows, String executionDetail) {
        EtlExecutionLog executionLog = this.getById(logId);
        if (executionLog == null) {
            log.error("执行日志不存在: {}", logId);
            return;
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        long duration = ChronoUnit.MILLIS.between(executionLog.getStartTime(), endTime);
        
        executionLog.setExecutionStatus(EtlExecutionStatus.SUCCESS.getCode());
        executionLog.setEndTime(endTime);
        executionLog.setDuration(duration);
        executionLog.setExtractedRows(extractedRows);
        executionLog.setTransformedRows(transformedRows);
        executionLog.setLoadedRows(loadedRows);
        executionLog.setFailedRows(0L);
        executionLog.setExecutionDetail(executionDetail);
        
        this.updateById(executionLog);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordExecutionFailure(Long logId, Long extractedRows, Long transformedRows, 
                                       Long loadedRows, Long failedRows, String errorMessage) {
        EtlExecutionLog executionLog = this.getById(logId);
        if (executionLog == null) {
            log.error("执行日志不存在: {}", logId);
            return;
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        long duration = ChronoUnit.MILLIS.between(executionLog.getStartTime(), endTime);
        
        executionLog.setExecutionStatus(EtlExecutionStatus.FAILED.getCode());
        executionLog.setEndTime(endTime);
        executionLog.setDuration(duration);
        executionLog.setExtractedRows(extractedRows);
        executionLog.setTransformedRows(transformedRows);
        executionLog.setLoadedRows(loadedRows);
        executionLog.setFailedRows(failedRows);
        executionLog.setErrorMessage(errorMessage);
        
        this.updateById(executionLog);
    }
}
