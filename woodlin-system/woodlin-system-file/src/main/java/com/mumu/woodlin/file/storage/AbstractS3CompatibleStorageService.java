package com.mumu.woodlin.file.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.file.dto.UploadCredentialDTO;
import com.mumu.woodlin.file.entity.SysStorageConfig;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * S3兼容存储服务抽象基类
 * 
 * @author mumu
 * @description 提供基于AWS S3 SDK的通用存储服务实现，适用于所有支持S3协议的存储平台
 *              包括：AWS S3、MinIO、阿里云OSS、腾讯云COS、华为云OBS等
 *              子类只需要实现getStorageType()方法即可
 * @since 2025-01-31
 */
@Slf4j
public abstract class AbstractS3CompatibleStorageService implements StorageService {
    
    /**
     * 创建S3客户端
     * 支持自定义endpoint的S3兼容服务
     *
     * @param config 存储配置
     * @return S3客户端
     */
    protected AmazonS3 createS3Client(SysStorageConfig config) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(
            config.getAccessKey(), 
            config.getSecretKey()
        );
        
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials));
        
        // 配置客户端选项
        ClientConfiguration clientConfig = new ClientConfiguration();
        // 某些S3兼容服务不支持分片编码
        clientConfig.setSignerOverride("AWSS3V4SignerType");
        builder.withClientConfiguration(clientConfig);
        
        // 如果配置了endpoint，使用自定义endpoint（适用于MinIO、阿里云OSS等）
        if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    config.getEndpoint(), 
                    config.getRegion() != null ? config.getRegion() : "us-east-1"
                )
            );
            // S3兼容服务通常使用路径风格访问
            builder.withPathStyleAccessEnabled(true);
        } else {
            // AWS S3使用标准区域配置
            builder.withRegion(config.getRegion() != null ? config.getRegion() : "us-east-1");
        }
        
        return builder.build();
    }
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        try {
            AmazonS3 s3Client = createS3Client(config);
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            
            // 上传文件
            s3Client.putObject(config.getBucketName(), objectKey, inputStream, metadata);
            
            log.info("{}存储上传成功: bucket={}, objectKey={}", 
                getStorageType(), config.getBucketName(), objectKey);
            
            // 返回访问URL
            String domain = config.getDomain();
            if (domain != null && !domain.isEmpty()) {
                return domain + "/" + objectKey;
            }
            
            // 如果是公开访问，返回对象URL
            if (CommonConstant.STATUS_ENABLE.equals(config.getIsPublic())) {
                return s3Client.getUrl(config.getBucketName(), objectKey).toString();
            }
            
            // 返回endpoint URL
            if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
                return config.getEndpoint() + "/" + config.getBucketName() + "/" + objectKey;
            }
            
            return s3Client.getUrl(config.getBucketName(), objectKey).toString();
            
        } catch (Exception e) {
            log.error("{}存储上传失败: objectKey={}", getStorageType(), objectKey, e);
            throw new BusinessException(getStorageType() + "存储上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        try {
            AmazonS3 s3Client = createS3Client(config);
            return s3Client.getObject(config.getBucketName(), objectKey).getObjectContent();
            
        } catch (Exception e) {
            log.error("{}存储下载失败: objectKey={}", getStorageType(), objectKey, e);
            throw new BusinessException(getStorageType() + "存储下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        try {
            AmazonS3 s3Client = createS3Client(config);
            s3Client.deleteObject(config.getBucketName(), objectKey);
            
            log.info("{}存储删除成功: bucket={}, objectKey={}", 
                getStorageType(), config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("{}存储删除失败: objectKey={}", getStorageType(), objectKey, e);
            throw new BusinessException(getStorageType() + "存储删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        try {
            AmazonS3 s3Client = createS3Client(config);
            return s3Client.doesObjectExist(config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("{}存储检查文件存在失败: objectKey={}", getStorageType(), objectKey, e);
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        try {
            AmazonS3 s3Client = createS3Client(config);
            
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
            log.error("{}生成预签名URL失败: objectKey={}", getStorageType(), objectKey, e);
            throw new BusinessException(getStorageType() + "生成预签名URL失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        try {
            AmazonS3 s3Client = createS3Client(config);
            
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
            UploadCredentialDTO credentialDTO = new UploadCredentialDTO(
                uploadUrl.toString(),
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("{}生成上传凭证失败: objectKey={}", getStorageType(), objectKey, e);
            throw new BusinessException(getStorageType() + "生成上传凭证失败: " + e.getMessage(), e);
        }
    }
}
