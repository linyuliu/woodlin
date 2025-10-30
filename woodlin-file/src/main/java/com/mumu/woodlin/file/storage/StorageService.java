package com.mumu.woodlin.file.storage;

import java.io.InputStream;

import com.mumu.woodlin.file.entity.SysStorageConfig;

/**
 * 存储服务接口
 * 
 * @author mumu
 * @description 定义对象存储服务的统一接口，支持多种存储平台
 * @since 2025-01-30
 */
public interface StorageService {
    
    /**
     * 上传文件
     *
     * @param config      存储配置
     * @param objectKey   对象键（文件路径）
     * @param inputStream 输入流
     * @param contentType 内容类型
     * @param fileSize    文件大小
     * @return 文件访问URL
     */
    String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream, 
                     String contentType, long fileSize);
    
    /**
     * 下载文件
     *
     * @param config    存储配置
     * @param objectKey 对象键（文件路径）
     * @return 文件输入流
     */
    InputStream downloadFile(SysStorageConfig config, String objectKey);
    
    /**
     * 删除文件
     *
     * @param config    存储配置
     * @param objectKey 对象键（文件路径）
     */
    void deleteFile(SysStorageConfig config, String objectKey);
    
    /**
     * 判断文件是否存在
     *
     * @param config    存储配置
     * @param objectKey 对象键（文件路径）
     * @return 是否存在
     */
    boolean fileExists(SysStorageConfig config, String objectKey);
    
    /**
     * 生成预签名URL（用于临时访问私有文件）
     *
     * @param config         存储配置
     * @param objectKey      对象键（文件路径）
     * @param expirationTime 过期时间（秒）
     * @return 预签名URL
     */
    String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime);
    
    /**
     * 生成上传凭证（用于前端直传）
     *
     * @param config         存储配置
     * @param objectKey      对象键（文件路径）
     * @param expirationTime 过期时间（秒）
     * @return 上传凭证（JSON格式）
     */
    String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime);
    
    /**
     * 获取存储类型
     *
     * @return 存储类型代码
     */
    String getStorageType();
}
