package com.mumu.woodlin.file.config;

import lombok.Data;

/**
 * S3兼容存储配置基类
 * 
 * @author mumu
 * @description 提供S3协议兼容存储的通用配置属性，适用于AWS S3、MinIO、阿里云OSS、腾讯云COS、华为云OBS等
 *              这些平台都支持S3协议，可以使用统一的配置结构
 * @since 2025-01-31
 */
@Data
public class S3CompatibleStorageProperties {
    
    /**
     * 是否启用
     */
    private boolean enabled = false;
    
    /**
     * 端点地址
     * AWS S3无需配置，其他兼容S3的服务需要配置
     */
    private String endpoint;
    
    /**
     * 访问密钥 (Access Key ID)
     */
    private String accessKey;
    
    /**
     * 密钥 (Secret Access Key)
     */
    private String secretKey;
    
    /**
     * 区域 (Region)
     * 部分服务可能不需要
     */
    private String region;
    
    /**
     * 存储桶名称 (Bucket Name)
     */
    private String bucketName;
    
    /**
     * 自定义访问域名
     * 用于CDN加速或自定义域名访问
     */
    private String domain;
    
    /**
     * 是否使用路径风格访问
     * true: http://endpoint/bucket/key
     * false: http://bucket.endpoint/key (默认)
     */
    private boolean pathStyleAccess = false;
    
    /**
     * 是否禁用分片上传
     * 某些兼容S3的服务可能不完全支持分片上传
     */
    private boolean disableChunkedEncoding = false;
    
    /**
     * 是否使用原生SDK
     * true: 使用平台原生SDK（支持高级功能）
     * false: 使用S3兼容模式（代码简洁，基础功能）
     * 默认false，使用S3兼容模式
     */
    private boolean useNativeSdk = false;
}
