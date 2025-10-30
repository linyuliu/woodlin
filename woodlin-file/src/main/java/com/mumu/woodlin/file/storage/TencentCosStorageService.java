package com.mumu.woodlin.file.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云COS存储服务实现
 * 
 * @author mumu
 * @description 腾讯云对象存储服务(COS)实现
 * @since 2025-01-30
 */
@Slf4j
@Service
public class TencentCosStorageService implements StorageService {
    
    /**
     * 创建COS客户端
     *
     * @param config 存储配置
     * @return COS客户端
     */
    private COSClient createClient(SysStorageConfig config) {
        COSCredentials credentials = new BasicCOSCredentials(
            config.getAccessKey(),
            config.getSecretKey()
        );
        
        Region region = new Region(config.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        
        return new COSClient(credentials, clientConfig);
    }
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        COSClient cosClient = null;
        try {
            cosClient = createClient(config);
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            
            // 上传文件
            cosClient.putObject(config.getBucketName(), objectKey, inputStream, metadata);
            
            log.info("腾讯云COS上传成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
            // 返回访问URL
            String domain = config.getDomain();
            if (domain != null && !domain.isEmpty()) {
                return domain + "/" + objectKey;
            }
            
            // 如果是公开访问，返回公开URL
            if ("1".equals(config.getIsPublic())) {
                return "https://" + config.getBucketName() + ".cos." + config.getRegion() + ".myqcloud.com/" + objectKey;
            }
            
            return config.getEndpoint() + "/" + objectKey;
            
        } catch (Exception e) {
            log.error("腾讯云COS上传失败: objectKey={}", objectKey, e);
            throw new RuntimeException("腾讯云COS上传失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        COSClient cosClient = null;
        try {
            cosClient = createClient(config);
            
            return cosClient.getObject(config.getBucketName(), objectKey).getObjectContent();
            
        } catch (Exception e) {
            log.error("腾讯云COS下载失败: objectKey={}", objectKey, e);
            throw new RuntimeException("腾讯云COS下载失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        COSClient cosClient = null;
        try {
            cosClient = createClient(config);
            
            cosClient.deleteObject(config.getBucketName(), objectKey);
            
            log.info("腾讯云COS删除成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("腾讯云COS删除失败: objectKey={}", objectKey, e);
            throw new RuntimeException("腾讯云COS删除失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        COSClient cosClient = null;
        try {
            cosClient = createClient(config);
            
            return cosClient.doesObjectExist(config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("腾讯云COS检查文件存在失败: objectKey={}", objectKey, e);
            return false;
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        COSClient cosClient = null;
        try {
            cosClient = createClient(config);
            
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000L);
            
            // 生成预签名URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                config.getBucketName(),
                objectKey,
                HttpMethodName.GET
            );
            request.setExpiration(expiration);
            
            URL url = cosClient.generatePresignedUrl(request);
            
            return url.toString();
            
        } catch (Exception e) {
            log.error("腾讯云COS生成预签名URL失败: objectKey={}", objectKey, e);
            throw new RuntimeException("腾讯云COS生成预签名URL失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        COSClient cosClient = null;
        try {
            cosClient = createClient(config);
            
            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000L);
            
            // 生成上传预签名URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                config.getBucketName(),
                objectKey,
                HttpMethodName.PUT
            );
            request.setExpiration(expiration);
            
            URL uploadUrl = cosClient.generatePresignedUrl(request);
            
            // 返回上传凭证
            com.mumu.woodlin.file.dto.UploadCredentialDTO credentialDTO = new com.mumu.woodlin.file.dto.UploadCredentialDTO(
                uploadUrl.toString(),
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("腾讯云COS生成上传凭证失败: objectKey={}", objectKey, e);
            throw new RuntimeException("腾讯云COS生成上传凭证失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
    
    @Override
    public String getStorageType() {
        return StorageType.COS.getCode();
    }
}
