package com.mumu.woodlin.etl.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.enums.EtlJobStatus;
import com.mumu.woodlin.etl.mapper.EtlJobMapper;
import com.mumu.woodlin.etl.service.IEtlExecutionService;
import com.mumu.woodlin.etl.service.IEtlJobService;
import com.mumu.woodlin.task.service.ITaskScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int DEFAULT_RETRY_INTERVAL = 60;
    private static final String DEFAULT_CONCURRENT = "0";
    private static final String DEFAULT_JOB_GROUP = "DEFAULT";
    private static final String DEFAULT_SYNC_MODE = "FULL";
    private static final String DEFAULT_CRON_EXPRESSION = "0 0 2 * * ?";

    private final ObjectProvider<ITaskScheduleService> taskScheduleServiceProvider;
    private final IEtlExecutionService etlExecutionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createJob(EtlJob etlJob) {
        applyCreateDefaults(etlJob);

        boolean saved = this.save(etlJob);
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

        applyUpdateDefaults(etlJob, oldJob);
        boolean updated = this.updateById(etlJob);

        if (updated) {
            String oldCron = oldJob.getCronExpression();
            String newCron = etlJob.getCronExpression();
            boolean cronChanged = (oldCron == null && newCron != null)
                || (oldCron != null && !oldCron.equals(newCron));

            if (!oldJob.getStatus().equals(etlJob.getStatus()) || cronChanged) {
                deleteScheduleJob(oldJob);
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

        deleteScheduleJob(job);
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

        job.setStatus(EtlJobStatus.ENABLED.getCode());
        if (StrUtil.isBlank(job.getCronExpression())) {
            job.setCronExpression(DEFAULT_CRON_EXPRESSION);
        }
        if (StrUtil.isBlank(job.getJobGroup())) {
            job.setJobGroup(DEFAULT_JOB_GROUP);
        }

        boolean updated = this.updateById(job);
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

        job.setStatus(EtlJobStatus.DISABLED.getCode());
        boolean updated = this.updateById(job);
        if (updated) {
            deleteScheduleJob(job);
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
            etlExecutionService.execute(job);
            job.setLastExecuteTime(LocalDateTime.now());
            this.updateById(job);
            return true;
        } catch (Exception exception) {
            log.error("执行ETL任务失败: {}", jobId, exception);
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

    private void applyCreateDefaults(EtlJob etlJob) {
        if (etlJob.getStatus() == null) {
            etlJob.setStatus(EtlJobStatus.DISABLED.getCode());
        }
        if (etlJob.getBatchSize() == null) {
            etlJob.setBatchSize(DEFAULT_BATCH_SIZE);
        }
        if (etlJob.getRetryCount() == null) {
            etlJob.setRetryCount(DEFAULT_RETRY_COUNT);
        }
        if (etlJob.getRetryInterval() == null) {
            etlJob.setRetryInterval(DEFAULT_RETRY_INTERVAL);
        }
        if (StrUtil.isBlank(etlJob.getConcurrent())) {
            etlJob.setConcurrent(DEFAULT_CONCURRENT);
        }
        if (StrUtil.isBlank(etlJob.getJobGroup())) {
            etlJob.setJobGroup(DEFAULT_JOB_GROUP);
        }
        if (StrUtil.isBlank(etlJob.getSyncMode())) {
            etlJob.setSyncMode(DEFAULT_SYNC_MODE);
        }
        if (StrUtil.isBlank(etlJob.getCronExpression())) {
            etlJob.setCronExpression(DEFAULT_CRON_EXPRESSION);
        }
    }

    private void applyUpdateDefaults(EtlJob etlJob, EtlJob oldJob) {
        if (etlJob.getStatus() == null) {
            etlJob.setStatus(StrUtil.emptyToDefault(oldJob.getStatus(), EtlJobStatus.DISABLED.getCode()));
        }
        if (etlJob.getBatchSize() == null) {
            etlJob.setBatchSize(oldJob.getBatchSize() == null ? DEFAULT_BATCH_SIZE : oldJob.getBatchSize());
        }
        if (etlJob.getRetryCount() == null) {
            etlJob.setRetryCount(oldJob.getRetryCount() == null ? DEFAULT_RETRY_COUNT : oldJob.getRetryCount());
        }
        if (etlJob.getRetryInterval() == null) {
            etlJob.setRetryInterval(
                oldJob.getRetryInterval() == null ? DEFAULT_RETRY_INTERVAL : oldJob.getRetryInterval()
            );
        }
        if (StrUtil.isBlank(etlJob.getConcurrent())) {
            etlJob.setConcurrent(StrUtil.emptyToDefault(oldJob.getConcurrent(), DEFAULT_CONCURRENT));
        }
        if (StrUtil.isBlank(etlJob.getJobGroup())) {
            etlJob.setJobGroup(StrUtil.emptyToDefault(oldJob.getJobGroup(), DEFAULT_JOB_GROUP));
        }
        if (StrUtil.isBlank(etlJob.getSyncMode())) {
            etlJob.setSyncMode(StrUtil.emptyToDefault(oldJob.getSyncMode(), DEFAULT_SYNC_MODE));
        }
        if (StrUtil.isBlank(etlJob.getCronExpression())) {
            etlJob.setCronExpression(StrUtil.emptyToDefault(oldJob.getCronExpression(), DEFAULT_CRON_EXPRESSION));
        }
    }

    /**
     * 创建调度任务
     */
    private void createScheduleJob(EtlJob etlJob) {
        try {
            ITaskScheduleService taskScheduleService = taskScheduleServiceProvider.getIfAvailable();
            if (taskScheduleService == null) {
                log.warn(
                    "未检测到 ITaskScheduleService 实现，跳过本地调度创建: jobName={}",
                    etlJob.getJobName()
                );
                return;
            }
            String invokeTarget = String.format("etlJobService.executeJob(%d)", etlJob.getJobId());

            taskScheduleService.createJob(
                etlJob.getJobName(),
                etlJob.getJobGroup(),
                etlJob.getCronExpression(),
                invokeTarget
            );
            log.info("创建ETL调度任务成功: {}", etlJob.getJobName());
        } catch (Exception exception) {
            log.error("创建ETL调度任务失败: {}", etlJob.getJobName(), exception);
        }
    }

    private void deleteScheduleJob(EtlJob etlJob) {
        try {
            ITaskScheduleService taskScheduleService = taskScheduleServiceProvider.getIfAvailable();
            if (taskScheduleService == null) {
                log.warn(
                    "未检测到 ITaskScheduleService 实现，跳过本地调度删除: jobName={}",
                    etlJob.getJobName()
                );
                return;
            }
            taskScheduleService.deleteJob(etlJob.getJobName(), etlJob.getJobGroup());
        } catch (Exception exception) {
            log.error("删除ETL调度任务失败: {}", etlJob.getJobName(), exception);
        }
    }
}
