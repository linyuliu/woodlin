package com.mumu.woodlin.file.transcoding;

import java.io.InputStream;

import com.mumu.woodlin.file.enums.TranscodingType;

/**
 * 转码服务接口
 * 
 * @author mumu
 * @description 定义文件转码服务的统一接口，支持多种转码策略
 *              采用策略模式，便于扩展新的转码类型
 * @since 2025-01-31
 */
public interface TranscodingService {
    
    /**
     * 执行转码
     *
     * @param inputStream  输入文件流
     * @param sourceFormat 源文件格式（如：docx, mp4等）
     * @param options      转码选项（如视频码率、分辨率等）
     * @return 转码后的文件流
     */
    InputStream transcode(InputStream inputStream, String sourceFormat, TranscodingOptions options);
    
    /**
     * 异步执行转码
     *
     * @param fileId       文件ID
     * @param sourceFormat 源文件格式
     * @param options      转码选项
     * @return 转码任务ID
     */
    String transcodeAsync(Long fileId, String sourceFormat, TranscodingOptions options);
    
    /**
     * 获取转码任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    TranscodingTaskStatus getTaskStatus(String taskId);
    
    /**
     * 获取支持的转码类型
     *
     * @return 转码类型
     */
    TranscodingType getTranscodingType();
    
    /**
     * 判断是否支持该格式
     *
     * @param sourceFormat 源文件格式
     * @return 是否支持
     */
    boolean supports(String sourceFormat);
}
