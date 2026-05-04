package com.mumu.woodlin.task.service.impl;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.task.entity.SysJob;
import com.mumu.woodlin.task.entity.SysJobLog;
import com.mumu.woodlin.task.mapper.SysJobMapper;
import com.mumu.woodlin.task.service.ISysJobLogService;
import com.mumu.woodlin.task.service.ISysJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 定时任务服务实现
 *
 * @author yulin
 * @description 定时任务的数据库操作实现，Quartz 调度操作可选
 * @since 2026-06
 */
@Slf4j
@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements ISysJobService {

    @Autowired(required = false)
    private Scheduler quartzScheduler;

    @Autowired
    private ISysJobLogService jobLogService;

    @Override
    public PageResult<SysJob> queryJobPage(SysJob query, int pageNum, int pageSize) {
        long current = pageNum < 1 ? 1L : pageNum;
        long size = pageSize < 1 ? 10L : pageSize;
        Page<SysJob> page = new Page<>(current, size);
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getJobName())) {
                wrapper.like(SysJob::getJobName, query.getJobName());
            }
            if (StringUtils.hasText(query.getJobGroup())) {
                wrapper.eq(SysJob::getJobGroup, query.getJobGroup());
            }
            if (StringUtils.hasText(query.getStatus())) {
                wrapper.eq(SysJob::getStatus, query.getStatus());
            }
        }
        wrapper.orderByDesc(SysJob::getCreateTime);
        page = this.page(page, wrapper);
        return PageResult.success(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public boolean createJob(SysJob job) {
        boolean saved = this.save(job);
        if (saved && quartzScheduler != null) {
            log.info("Quartz scheduler available, but real scheduling is not yet implemented for job [{}]", job.getJobName());
        }
        return saved;
    }

    @Override
    public boolean updateJob(SysJob job) {
        return this.updateById(job);
    }

    @Override
    public boolean deleteJob(Long jobId) {
        return this.removeById(jobId);
    }

    @Override
    public boolean changeJobStatus(Long jobId, String status) {
        SysJob job = this.getById(jobId);
        if (job == null) {
            return false;
        }
        job.setStatus(status);
        return this.updateById(job);
    }

    @Override
    public boolean runJobOnce(Long jobId) {
        SysJob job = this.getById(jobId);
        if (job == null) {
            return false;
        }
        LocalDateTime start = LocalDateTime.now();
        SysJobLog logEntry = new SysJobLog()
                .setJobName(job.getJobName())
                .setJobGroup(job.getJobGroup())
                .setInvokeTarget(job.getInvokeTarget())
                .setStatus("0")
                .setMessage("手动触发执行(占位实现)")
                .setStartTime(start)
                .setStopTime(start)
                .setElapsedTime(0L)
                .setTenantId(job.getTenantId());
        jobLogService.save(logEntry);
        job.setLastExecuteTime(start);
        this.updateById(job);
        return true;
    }
}
