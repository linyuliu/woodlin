package com.mumu.woodlin.file.transcoding;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 转码选项
 * 
 * @author mumu
 * @description 转码参数配置类，包含各种转码场景的配置选项
 * @since 2025-01-31
 */
@Data
@Accessors(chain = true)
public class TranscodingOptions {
    
    // ========== 通用选项 ==========
    
    /**
     * 目标格式（如：pdf, mp4, jpg等）
     */
    private String targetFormat;
    
    /**
     * 输出质量（0-100，100为最高质量）
     */
    private Integer quality = 80;
    
    // ========== 视频转码选项 ==========
    
    /**
     * 视频编码器（如：h264, h265, vp9等）
     */
    private String videoCodec = "h264";
    
    /**
     * 视频码率（单位：kbps）
     */
    private Integer videoBitrate;
    
    /**
     * 视频宽度
     */
    private Integer videoWidth;
    
    /**
     * 视频高度
     */
    private Integer videoHeight;
    
    /**
     * 帧率（fps）
     */
    private Integer frameRate = 30;
    
    /**
     * 是否保持宽高比
     */
    private Boolean keepAspectRatio = true;
    
    // ========== 音频转码选项 ==========
    
    /**
     * 音频编码器（如：aac, mp3, opus等）
     */
    private String audioCodec = "aac";
    
    /**
     * 音频码率（单位：kbps）
     */
    private Integer audioBitrate = 128;
    
    /**
     * 采样率（如：44100, 48000等）
     */
    private Integer sampleRate = 44100;
    
    /**
     * 声道数（1=单声道，2=立体声）
     */
    private Integer channels = 2;
    
    // ========== 文档转换选项 ==========
    
    /**
     * PDF页面方向（portrait/landscape）
     */
    private String pageOrientation = "portrait";
    
    /**
     * PDF页面大小（A4, A3, Letter等）
     */
    private String pageSize = "A4";
    
    /**
     * 是否保留原始格式
     */
    private Boolean preserveFormatting = true;
    
    // ========== 图片转换选项 ==========
    
    /**
     * 图片宽度
     */
    private Integer imageWidth;
    
    /**
     * 图片高度
     */
    private Integer imageHeight;
    
    /**
     * 是否压缩
     */
    private Boolean compress = false;
    
    // ========== 高级选项 ==========
    
    /**
     * 是否异步处理
     */
    private Boolean async = false;
    
    /**
     * 处理完成后的回调URL
     */
    private String callbackUrl;
    
    /**
     * 自定义参数（JSON格式）
     */
    private String customParams;
}
