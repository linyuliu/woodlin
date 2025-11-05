package com.mumu.woodlin.file.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.mumu.woodlin.file.dto.UploadCredentialDTO;
import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;
import com.mumu.woodlin.common.exception.BusinessException;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云OSS原生SDK存储服务实现
 * 
 * @author mumu
 * @description 使用阿里云OSS原生SDK实现，支持阿里云OSS的所有高级功能
 *              通过配置 woodlin.file.oss.use-native-sdk=true 启用
 *              适用场景：需要使用阿里云OSS特有功能（如图片处理、视频截帧等）
 * @since 2025-01-31
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "woodlin.file.oss.use-native-sdk", havingValue = "true")
public class AliyunOssNativeStorageService implements StorageService {
    
    /**
     * 创建OSS客户端
     *
     * @param config 存储配置
     * @return OSS客户端
     */
    private OSS createClient(SysStorageConfig config) {
        return new OSSClientBuilder().build(
            config.getEndpoint(),
            config.getAccessKey(),
            config.getSecretKey()
        );
    }
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        OSS ossClient = null;
        try {
            ossClient = createClient(config);
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            
            // 上传文件
            ossClient.putObject(config.getBucketName(), objectKey, inputStream, metadata);
            
            log.info("阿里云OSS(原生SDK)上传成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
            // 返回访问URL
            String domain = config.getDomain();
            if (domain != null && !domain.isEmpty()) {
                return domain + "/" + objectKey;
            }
            
            // 如果是公开访问，返回公开URL
            if ("1".equals(config.getIsPublic())) {
                return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + objectKey;
            }
            
            return config.getEndpoint() + "/" + objectKey;
            
        } catch (Exception e) {
            log.error("阿里云OSS上传失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("阿里云OSS上传失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = createClient(config);
            
            return ossClient.getObject(config.getBucketName(), objectKey).getObjectContent();
            
        } catch (Exception e) {
            log.error("阿里云OSS(原生SDK)下载失败: objectKey={}", objectKey, e);
            throw new BusinessException("阿里云OSS下载失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = createClient(config);
            
            ossClient.deleteObject(config.getBucketName(), objectKey);
            
            log.info("阿里云OSS删除成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("阿里云OSS删除失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("阿里云OSS删除失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = createClient(config);
            
            return ossClient.doesObjectExist(config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("阿里云OSS(原生SDK)检查文件存在失败: objectKey={}", objectKey, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        OSS ossClient = null;
        try {
            ossClient = createClient(config);
            
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000L);
            
            // 生成预签名URL
            URL url = ossClient.generatePresignedUrl(config.getBucketName(), objectKey, expiration);
            
            return url.toString();
            
        } catch (Exception e) {
            log.error("阿里云OSS(原生SDK)生成预签名URL失败: objectKey={}", objectKey, e);
            throw new BusinessException("阿里云OSS生成预签名URL失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        OSS ossClient = null;
        try {
            ossClient = createClient(config);
            
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000L);
            
            // 生成上传预签名URL
            URL uploadUrl = ossClient.generatePresignedUrl(
                config.getBucketName(), 
                objectKey, 
                expiration,
                com.aliyun.oss.HttpMethod.PUT
            );
            
            // 返回上传凭证
            UploadCredentialDTO credentialDTO = new UploadCredentialDTO(
                uploadUrl.toString(),
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            log.info("阿里云OSS生成上传凭证成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("阿里云OSS生成上传凭证失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("阿里云OSS生成上传凭证失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public String getStorageType() {
        return StorageType.OSS.getCode();
    }
}
