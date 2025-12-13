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
        EtlExecutionLog log = this.getById(logId);
        if (log == null) {
            log.error("执行日志不存在: {}", logId);
            return;
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        long duration = ChronoUnit.MILLIS.between(log.getStartTime(), endTime);
        
        log.setExecutionStatus(EtlExecutionStatus.SUCCESS.getCode());
        log.setEndTime(endTime);
        log.setDuration(duration);
        log.setExtractedRows(extractedRows);
        log.setTransformedRows(transformedRows);
        log.setLoadedRows(loadedRows);
        log.setFailedRows(0L);
        log.setExecutionDetail(executionDetail);
        
        this.updateById(log);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordExecutionFailure(Long logId, Long extractedRows, Long transformedRows, 
                                       Long loadedRows, Long failedRows, String errorMessage) {
        EtlExecutionLog log = this.getById(logId);
        if (log == null) {
            log.error("执行日志不存在: {}", logId);
            return;
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        long duration = ChronoUnit.MILLIS.between(log.getStartTime(), endTime);
        
        log.setExecutionStatus(EtlExecutionStatus.FAILED.getCode());
        log.setEndTime(endTime);
        log.setDuration(duration);
        log.setExtractedRows(extractedRows);
        log.setTransformedRows(transformedRows);
        log.setLoadedRows(loadedRows);
        log.setFailedRows(failedRows);
        log.setErrorMessage(errorMessage);
        
        this.updateById(log);
    }
}
