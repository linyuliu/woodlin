package com.mumu.woodlin.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 文件管理配置属性
 * 
 * @author mumu
 * @description 文件管理相关的配置属性，从application.yml读取
 *              注意：敏感信息（如accessKey、secretKey）应使用环境变量或加密配置，
 *              不要直接在配置文件中存储明文密钥
 * @since 2025-01-30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "woodlin.file")
public class FileProperties {
    
    /**
     * 本地存储配置
     */
    private LocalStorageProperties local = new LocalStorageProperties();
    
    /**
     * MinIO存储配置 (S3兼容)
     */
    private S3CompatibleStorageProperties minio = new S3CompatibleStorageProperties();
    
    /**
     * AWS S3存储配置
     */
    private S3CompatibleStorageProperties s3 = new S3CompatibleStorageProperties();
    
    /**
     * 阿里云OSS存储配置 (S3兼容)
     */
    private S3CompatibleStorageProperties oss = new S3CompatibleStorageProperties();
    
    /**
     * 腾讯云COS存储配置 (S3兼容)
     */
    private S3CompatibleStorageProperties cos = new S3CompatibleStorageProperties();
    
    /**
     * 华为云OBS存储配置 (S3兼容)
     */
    private S3CompatibleStorageProperties obs = new S3CompatibleStorageProperties();
    
    /**
     * 本地存储配置类
     */
    @Data
    public static class LocalStorageProperties {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 基础路径
         */
        private String basePath = "./uploads/";
        
        /**
         * 访问域名
         */
        private String domain;
    }
    
}
