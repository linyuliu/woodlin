package com.mumu.woodlin.etl.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.enums.EtlJobStatus;
import com.mumu.woodlin.etl.mapper.EtlJobMapper;
import com.mumu.woodlin.etl.service.IEtlExecutionService;
import com.mumu.woodlin.etl.service.IEtlJobService;
import com.mumu.woodlin.task.service.ITaskScheduleService;

/**
 * ETL任务服务实现
 * 
 * @author mumu
 * @description ETL任务管理服务实现
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlJobServiceImpl extends ServiceImpl<EtlJobMapper, EtlJob> implements IEtlJobService {
    
    private final ITaskScheduleService taskScheduleService;
    private final IEtlExecutionService etlExecutionService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createJob(EtlJob etlJob) {
        // 设置默认值
        if (etlJob.getStatus() == null) {
            etlJob.setStatus(EtlJobStatus.DISABLED.getCode());
        }
        if (etlJob.getBatchSize() == null) {
            etlJob.setBatchSize(1000);
        }
        if (etlJob.getRetryCount() == null) {
            etlJob.setRetryCount(3);
        }
        if (etlJob.getRetryInterval() == null) {
            etlJob.setRetryInterval(60);
        }
        if (etlJob.getConcurrent() == null) {
            etlJob.setConcurrent("0");
        }
        
        // 保存任务
        boolean saved = this.save(etlJob);
        
        // 如果启用状态，创建调度任务
        if (saved && EtlJobStatus.ENABLED.getCode().equals(etlJob.getStatus())) {
            createScheduleJob(etlJob);
        }
        
        return saved;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(EtlJob etlJob) {
        EtlJob oldJob = this.getById(etlJob.getJobId());
        if (oldJob == null) {
            log.error("ETL任务不存在: {}", etlJob.getJobId());
            return false;
        }
        
        // 更新任务
        boolean updated = this.updateById(etlJob);
        
        if (updated) {
            // 如果状态或cron表达式发生变化，需要更新调度
            if (!oldJob.getStatus().equals(etlJob.getStatus()) || 
                !oldJob.getCronExpression().equals(etlJob.getCronExpression())) {
                
                // 删除旧的调度任务
                taskScheduleService.deleteJob(oldJob.getJobName(), oldJob.getJobGroup());
                
                // 如果是启用状态，创建新的调度任务
                if (EtlJobStatus.ENABLED.getCode().equals(etlJob.getStatus())) {
                    createScheduleJob(etlJob);
                }
            }
        }
        
        return updated;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long jobId) {
        EtlJob job = this.getById(jobId);
        if (job == null) {
            log.error("ETL任务不存在: {}", jobId);
            return false;
        }
        
        // 删除调度任务
        taskScheduleService.deleteJob(job.getJobName(), job.getJobGroup());
        
        // 删除ETL任务
        return this.removeById(jobId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableJob(Long jobId) {
        EtlJob job = this.getById(jobId);
        if (job == null) {
            log.error("ETL任务不存在: {}", jobId);
            return false;
        }
        
        // 更新状态
        job.setStatus(EtlJobStatus.ENABLED.getCode());
        boolean updated = this.updateById(job);
        
        // 创建调度任务
        if (updated) {
            createScheduleJob(job);
        }
        
        return updated;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableJob(Long jobId) {
        EtlJob job = this.getById(jobId);
        if (job == null) {
            log.error("ETL任务不存在: {}", jobId);
            return false;
        }
        
        // 更新状态
        job.setStatus(EtlJobStatus.DISABLED.getCode());
        boolean updated = this.updateById(job);
        
        // 删除调度任务
        if (updated) {
            taskScheduleService.deleteJob(job.getJobName(), job.getJobGroup());
        }
        
        return updated;
    }
    
    @Override
    public boolean executeJob(Long jobId) {
        EtlJob job = this.getById(jobId);
        if (job == null) {
            log.error("ETL任务不存在: {}", jobId);
            return false;
        }
        
        try {
            // 异步执行ETL任务
            etlExecutionService.execute(job);
            
            // 更新上次执行时间
            job.setLastExecuteTime(LocalDateTime.now());
            this.updateById(job);
            
            return true;
        } catch (Exception e) {
            log.error("执行ETL任务失败: {}", jobId, e);
            return false;
        }
    }
    
    @Override
    public List<EtlJob> listEnabledJobs() {
        return this.list(new LambdaQueryWrapper<EtlJob>()
                .eq(EtlJob::getStatus, EtlJobStatus.ENABLED.getCode()));
    }
    
    @Override
    public List<EtlJob> listJobsByTenantId(String tenantId) {
        return this.list(new LambdaQueryWrapper<EtlJob>()
                .eq(EtlJob::getTenantId, tenantId));
    }
    
    /**
     * 创建调度任务
     */
    private void createScheduleJob(EtlJob etlJob) {
        try {
            taskScheduleService.createJob(
                etlJob.getJobName(),
                etlJob.getJobGroup(),
                etlJob.getCronExpression(),
                "com.mumu.woodlin.etl.job.EtlJobExecutor"
            );
            log.info("创建ETL调度任务成功: {}", etlJob.getJobName());
        } catch (Exception e) {
            log.error("创建ETL调度任务失败: {}", etlJob.getJobName(), e);
        }
    }
}
