package com.mumu.woodlin.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.task.entity.SysJobLog;

/**
 * 定时任务日志服务接口
 *
 * @author yulin
 * @since 2026-06
 */
public interface ISysJobLogService extends IService<SysJobLog> {

    /**
     * 分页查询日志
     */
    PageResult<SysJobLog> queryLogPage(SysJobLog query, int pageNum, int pageSize);

    /**
     * 删除单条日志
     */
    boolean deleteLog(Long logId);

    /**
     * 清空全部日志
     */
    boolean cleanLogs();
}
