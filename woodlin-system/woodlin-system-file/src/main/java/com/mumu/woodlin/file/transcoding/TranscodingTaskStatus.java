package com.mumu.woodlin.file.transcoding;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 转码任务状态
 * 
 * @author mumu
 * @description 转码任务的状态信息
 * @since 2025-01-31
 */
@Data
@Accessors(chain = true)
public class TranscodingTaskStatus {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 任务状态（pending-等待中，processing-处理中，completed-已完成，failed-失败）
     */
    private String status;
    
    /**
     * 进度百分比（0-100）
     */
    private Integer progress;
    
    /**
     * 转码后的文件ID
     */
    private Long outputFileId;
    
    /**
     * 转码后的文件URL
     */
    private String outputFileUrl;
    
    /**
     * 错误信息（失败时）
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    
    /**
     * 耗时（毫秒）
     */
    private Long duration;
}
