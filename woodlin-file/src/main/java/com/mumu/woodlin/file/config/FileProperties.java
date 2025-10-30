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
     * MinIO存储配置
     */
    private MinioProperties minio = new MinioProperties();
    
    /**
     * AWS S3存储配置
     */
    private S3Properties s3 = new S3Properties();
    
    /**
     * 阿里云OSS存储配置
     */
    private OssProperties oss = new OssProperties();
    
    /**
     * 腾讯云COS存储配置
     */
    private CosProperties cos = new CosProperties();
    
    /**
     * 华为云OBS存储配置
     */
    private ObsProperties obs = new ObsProperties();
    
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
    
    /**
     * MinIO存储配置
     */
    @Data
    public static class MinioProperties {
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 端点地址
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 密钥
         */
        private String secretKey;
        
        /**
         * 存储桶名称
         */
        private String bucketName;
        
        /**
         * 访问域名
         */
        private String domain;
    }
    
    /**
     * AWS S3存储配置
     */
    @Data
    public static class S3Properties {
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 端点地址（可选）
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 密钥
         */
        private String secretKey;
        
        /**
         * 区域
         */
        private String region;
        
        /**
         * 存储桶名称
         */
        private String bucketName;
        
        /**
         * 访问域名
         */
        private String domain;
    }
    
    /**
     * 阿里云OSS存储配置
     */
    @Data
    public static class OssProperties {
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 端点地址
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 密钥
         */
        private String secretKey;
        
        /**
         * 存储桶名称
         */
        private String bucketName;
        
        /**
         * 访问域名
         */
        private String domain;
    }
    
    /**
     * 腾讯云COS存储配置
     */
    @Data
    public static class CosProperties {
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 密钥
         */
        private String secretKey;
        
        /**
         * 区域
         */
        private String region;
        
        /**
         * 存储桶名称
         */
        private String bucketName;
        
        /**
         * 访问域名
         */
        private String domain;
    }
    
    /**
     * 华为云OBS存储配置
     */
    @Data
    public static class ObsProperties {
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 端点地址
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 密钥
         */
        private String secretKey;
        
        /**
         * 存储桶名称
         */
        private String bucketName;
        
        /**
         * 访问域名
         */
        private String domain;
    }
}
