package com.mumu.woodlin.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.task.entity.SysJobLog;
import com.mumu.woodlin.task.mapper.SysJobLogMapper;
import com.mumu.woodlin.task.service.ISysJobLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 定时任务日志服务实现
 *
 * @author yulin
 * @since 2026-06
 */
@Service
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements ISysJobLogService {

    @Override
    public PageResult<SysJobLog> queryLogPage(SysJobLog query, int pageNum, int pageSize) {
        long current = pageNum < 1 ? 1L : pageNum;
        long size = pageSize < 1 ? 10L : pageSize;
        Page<SysJobLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getJobName())) {
                wrapper.like(SysJobLog::getJobName, query.getJobName());
            }
            if (StringUtils.hasText(query.getJobGroup())) {
                wrapper.eq(SysJobLog::getJobGroup, query.getJobGroup());
            }
            if (StringUtils.hasText(query.getStatus())) {
                wrapper.eq(SysJobLog::getStatus, query.getStatus());
            }
        }
        wrapper.orderByDesc(SysJobLog::getStartTime);
        page = this.page(page, wrapper);
        return PageResult.success(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public boolean deleteLog(Long logId) {
        return this.removeById(logId);
    }

    @Override
    public boolean cleanLogs() {
        return this.remove(new LambdaQueryWrapper<>());
    }
}
