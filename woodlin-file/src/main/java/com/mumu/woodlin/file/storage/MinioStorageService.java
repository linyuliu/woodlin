package com.mumu.woodlin.file.storage;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;

import cn.hutool.json.JSONUtil;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO存储服务实现
 * 
 * @author mumu
 * @description MinIO对象存储服务实现，支持私有化部署的对象存储
 * @since 2025-01-30
 */
@Slf4j
@Service
public class MinioStorageService implements StorageService {
    
    /**
     * 创建MinIO客户端
     *
     * @param config 存储配置
     * @return MinIO客户端
     */
    private MinioClient createClient(SysStorageConfig config) {
        return MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        try {
            MinioClient minioClient = createClient(config);
            
            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build()
            );
            
            log.info("MinIO存储上传成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
            // 返回访问URL
            String domain = config.getDomain();
            if (domain != null && !domain.isEmpty()) {
                return domain + "/" + objectKey;
            }
            return config.getEndpoint() + "/" + config.getBucketName() + "/" + objectKey;
            
        } catch (Exception e) {
            log.error("MinIO存储上传失败: objectKey={}", objectKey, e);
            throw new RuntimeException("MinIO存储上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        try {
            MinioClient minioClient = createClient(config);
            
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("MinIO存储下载失败: objectKey={}", objectKey, e);
            throw new RuntimeException("MinIO存储下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        try {
            MinioClient minioClient = createClient(config);
            
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .build()
            );
            
            log.info("MinIO存储删除成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("MinIO存储删除失败: objectKey={}", objectKey, e);
            throw new RuntimeException("MinIO存储删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        try {
            MinioClient minioClient = createClient(config);
            
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .build()
            );
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        try {
            MinioClient minioClient = createClient(config);
            
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .expiry(expirationTime, TimeUnit.SECONDS)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("MinIO生成预签名URL失败: objectKey={}", objectKey, e);
            throw new RuntimeException("MinIO生成预签名URL失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        try {
            MinioClient minioClient = createClient(config);
            
            // 生成上传预签名URL
            String uploadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .expiry(expirationTime, TimeUnit.SECONDS)
                    .build()
            );
            
            // 返回上传凭证
            com.mumu.woodlin.file.dto.UploadCredentialDTO credentialDTO = new com.mumu.woodlin.file.dto.UploadCredentialDTO(
                uploadUrl,
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("MinIO生成上传凭证失败: objectKey={}", objectKey, e);
            throw new RuntimeException("MinIO生成上传凭证失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getStorageType() {
        return StorageType.MINIO.getCode();
    }
}
