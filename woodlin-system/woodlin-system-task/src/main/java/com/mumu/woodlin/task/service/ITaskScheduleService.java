package com.mumu.woodlin.task.service;

/**
 * 任务调度服务接口
 * 
 * @author mumu
 * @description 任务调度服务接口，支持SnailJob和Quartz两种实现
 * @since 2025-01-01
 */
public interface ITaskScheduleService {
    
    /**
     * 创建定时任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param cronExpression cron表达式
     * @param jobClass 任务执行类
     * @return 是否创建成功
     */
    boolean createJob(String jobName, String jobGroup, String cronExpression, String jobClass);
    
    /**
     * 暂停任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否暂停成功
     */
    boolean pauseJob(String jobName, String jobGroup);
    
    /**
     * 恢复任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否恢复成功
     */
    boolean resumeJob(String jobName, String jobGroup);
    
    /**
     * 删除任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否删除成功
     */
    boolean deleteJob(String jobName, String jobGroup);
    
    /**
     * 立即执行任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否执行成功
     */
    boolean executeJob(String jobName, String jobGroup);
    
    /**
     * 修改任务的cron表达式
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param cronExpression 新的cron表达式
     * @return 是否修改成功
     */
    boolean updateJobCron(String jobName, String jobGroup, String cronExpression);
}