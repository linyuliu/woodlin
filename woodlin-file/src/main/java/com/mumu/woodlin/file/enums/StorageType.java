package com.mumu.woodlin.file.enums;

import lombok.Getter;

/**
 * 存储类型枚举
 * 
 * @author mumu
 * @description 定义支持的对象存储平台类型
 * @since 2025-01-30
 */
@Getter
public enum StorageType {
    
    /**
     * 本地存储
     */
    LOCAL("local", "本地存储"),
    
    /**
     * MinIO对象存储
     */
    MINIO("minio", "MinIO对象存储"),
    
    /**
     * AWS S3对象存储
     */
    S3("s3", "AWS S3对象存储"),
    
    /**
     * 阿里云OSS
     */
    OSS("oss", "阿里云OSS"),
    
    /**
     * 腾讯云COS
     */
    COS("cos", "腾讯云COS"),
    
    /**
     * 华为云OBS
     */
    OBS("obs", "华为云OBS");
    
    /**
     * 存储类型代码
     */
    private final String code;
    
    /**
     * 存储类型名称
     */
    private final String name;
    
    StorageType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据代码获取存储类型
     *
     * @param code 存储类型代码
     * @return 存储类型枚举
     */
    public static StorageType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return LOCAL;
        }
        for (StorageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return LOCAL;
    }
}
