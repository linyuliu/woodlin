package com.mumu.woodlin.file.enums;

import lombok.Getter;

/**
 * 转码类型枚举
 * 
 * @author mumu
 * @description 定义支持的文件转码类型
 * @since 2025-01-31
 */
@Getter
public enum TranscodingType {
    
    /**
     * 文档转PDF
     */
    DOCUMENT_TO_PDF("document_to_pdf", "文档转PDF"),
    
    /**
     * 视频转码（降低码率和分辨率）
     */
    VIDEO_TRANSCODE("video_transcode", "视频转码"),
    
    /**
     * 音频转码
     */
    AUDIO_TRANSCODE("audio_transcode", "音频转码"),
    
    /**
     * 图片转换
     */
    IMAGE_CONVERT("image_convert", "图片格式转换");
    
    /**
     * 转码类型代码
     */
    private final String code;
    
    /**
     * 转码类型名称
     */
    private final String name;
    
    TranscodingType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据代码获取转码类型
     *
     * @param code 转码类型代码
     * @return 转码类型枚举
     */
    public static TranscodingType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (TranscodingType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
