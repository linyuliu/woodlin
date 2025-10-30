package com.mumu.woodlin.file.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * AWS S3存储服务实现
 * 
 * @author mumu
 * @description AWS S3对象存储服务实现，也兼容S3协议的其他存储服务
 * @since 2025-01-30
 */
@Slf4j
@Service
public class S3StorageService implements StorageService {
    
    /**
     * 创建S3客户端
     *
     * @param config 存储配置
     * @return S3客户端
     */
    private AmazonS3 createClient(SysStorageConfig config) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(
            config.getAccessKey(), 
            config.getSecretKey()
        );
        
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials));
        
        // 如果配置了endpoint，使用自定义endpoint
        if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
            builder.withEndpointConfiguration(
                new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(
                    config.getEndpoint(), 
                    config.getRegion()
                )
            );
        } else {
            builder.withRegion(config.getRegion());
        }
        
        return builder.build();
    }
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        try {
            AmazonS3 s3Client = createClient(config);
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            
            // 上传文件
            s3Client.putObject(config.getBucketName(), objectKey, inputStream, metadata);
            
            log.info("S3存储上传成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
            // 返回访问URL
            String domain = config.getDomain();
            if (domain != null && !domain.isEmpty()) {
                return domain + "/" + objectKey;
            }
            
            // 如果是公开访问，返回对象URL
            if ("1".equals(config.getIsPublic())) {
                return s3Client.getUrl(config.getBucketName(), objectKey).toString();
            }
            
            return config.getEndpoint() + "/" + config.getBucketName() + "/" + objectKey;
            
        } catch (Exception e) {
            log.error("S3存储上传失败: objectKey={}", objectKey, e);
            throw new RuntimeException("S3存储上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        try {
            AmazonS3 s3Client = createClient(config);
            
            return s3Client.getObject(config.getBucketName(), objectKey).getObjectContent();
            
        } catch (Exception e) {
            log.error("S3存储下载失败: objectKey={}", objectKey, e);
            throw new RuntimeException("S3存储下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        try {
            AmazonS3 s3Client = createClient(config);
            
            s3Client.deleteObject(config.getBucketName(), objectKey);
            
            log.info("S3存储删除成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("S3存储删除失败: objectKey={}", objectKey, e);
            throw new RuntimeException("S3存储删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        try {
            AmazonS3 s3Client = createClient(config);
            
            return s3Client.doesObjectExist(config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("S3存储检查文件存在失败: objectKey={}", objectKey, e);
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        try {
            AmazonS3 s3Client = createClient(config);
            
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000L);
            
            // 生成预签名URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                config.getBucketName(), 
                objectKey
            ).withExpiration(expiration);
            
            URL url = s3Client.generatePresignedUrl(request);
            
            return url.toString();
            
        } catch (Exception e) {
            log.error("S3生成预签名URL失败: objectKey={}", objectKey, e);
            throw new RuntimeException("S3生成预签名URL失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        try {
            AmazonS3 s3Client = createClient(config);
            
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000L);
            
            // 生成上传预签名URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                config.getBucketName(), 
                objectKey,
                com.amazonaws.HttpMethod.PUT
            ).withExpiration(expiration);
            
            URL uploadUrl = s3Client.generatePresignedUrl(request);
            
            // 返回上传凭证
            com.mumu.woodlin.file.dto.UploadCredentialDTO credentialDTO = new com.mumu.woodlin.file.dto.UploadCredentialDTO(
                uploadUrl.toString(),
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("S3生成上传凭证失败: objectKey={}", objectKey, e);
            throw new RuntimeException("S3生成上传凭证失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getStorageType() {
        return StorageType.S3.getCode();
    }
}
