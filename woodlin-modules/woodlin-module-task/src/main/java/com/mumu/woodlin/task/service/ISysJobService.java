package com.mumu.woodlin.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.task.entity.SysJob;

/**
 * 定时任务服务接口
 *
 * @author yulin
 * @since 2026-06
 */
public interface ISysJobService extends IService<SysJob> {

    /**
     * 分页查询任务
     */
    PageResult<SysJob> queryJobPage(SysJob query, int pageNum, int pageSize);

    /**
     * 新增任务
     */
    boolean createJob(SysJob job);

    /**
     * 修改任务
     */
    boolean updateJob(SysJob job);

    /**
     * 删除任务
     */
    boolean deleteJob(Long jobId);

    /**
     * 修改任务状态
     */
    boolean changeJobStatus(Long jobId, String status);

    /**
     * 立即执行一次
     */
    boolean runJobOnce(Long jobId);
}
