package com.mumu.woodlin.file.transcoding;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * 转码服务抽象基类
 * 
 * @author mumu
 * @description 提供转码服务的通用实现逻辑，子类只需实现具体的转码算法
 * @since 2025-01-31
 */
@Slf4j
public abstract class AbstractTranscodingService implements TranscodingService {
    
    @Override
    public String transcodeAsync(Long fileId, String sourceFormat, TranscodingOptions options) {
        // 生成任务ID
        String taskId = generateTaskId();
        
        // 创建任务状态
        TranscodingTaskStatus status = new TranscodingTaskStatus()
            .setTaskId(taskId)
            .setFileId(fileId)
            .setStatus("pending")
            .setProgress(0)
            .setCreateTime(LocalDateTime.now());
        
        // 异步提交转码任务
        submitTranscodingTask(taskId, fileId, sourceFormat, options);
        
        log.info("提交转码任务: taskId={}, fileId={}, type={}", 
            taskId, fileId, getTranscodingType().getName());
        
        return taskId;
    }
    
    @Override
    public TranscodingTaskStatus getTaskStatus(String taskId) {
        // 子类可以override这个方法实现自定义的状态查询
        // 这里提供默认实现
        return queryTaskStatus(taskId);
    }
    
    /**
     * 生成任务ID
     *
     * @return 任务ID
     */
    protected String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 提交转码任务到任务队列
     * 子类可以override以实现自定义的任务提交逻辑
     *
     * @param taskId       任务ID
     * @param fileId       文件ID
     * @param sourceFormat 源格式
     * @param options      转码选项
     */
    protected void submitTranscodingTask(String taskId, Long fileId, 
                                        String sourceFormat, TranscodingOptions options) {
        // 默认实现：使用线程池异步执行
        // 实际项目中可以使用消息队列（RabbitMQ, Kafka等）
        // 或者分布式任务调度框架（XXL-JOB等）
        log.warn("异步转码任务提交未实现，请在子类中override submitTranscodingTask方法");
    }
    
    /**
     * 查询任务状态
     * 子类应该override这个方法实现实际的状态查询逻辑
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    protected TranscodingTaskStatus queryTaskStatus(String taskId) {
        // 默认实现：返回未找到
        return new TranscodingTaskStatus()
            .setTaskId(taskId)
            .setStatus("not_found")
            .setErrorMessage("任务不存在或已过期");
    }
    
    /**
     * 验证转码选项
     *
     * @param options 转码选项
     * @throws IllegalArgumentException 如果选项无效
     */
    protected void validateOptions(TranscodingOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("转码选项不能为空");
        }
        
        if (options.getTargetFormat() == null || options.getTargetFormat().isEmpty()) {
            throw new IllegalArgumentException("目标格式不能为空");
        }
    }
}
