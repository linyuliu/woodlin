package com.mumu.woodlin.file.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.dto.UploadCredentialDTO;
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
import com.mumu.woodlin.common.exception.BusinessException;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云COS原生SDK存储服务实现
 * 
 * @author mumu
 * @description 使用腾讯云COS原生SDK实现，支持腾讯云COS的所有高级功能
 *              通过配置 woodlin.file.cos.use-native-sdk=true 启用
 *              适用场景：需要使用腾讯云COS特有功能（如数据万象、音视频处理等）
 * @since 2025-01-31
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "woodlin.file.cos.use-native-sdk", havingValue = "true")
public class TencentCosNativeStorageService implements StorageService {
    
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
            
            log.info("腾讯云COS上传成功: bucket={}, objectKey={}, size={}bytes", 
                config.getBucketName(), objectKey, fileSize);
            
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
            log.error("腾讯云COS上传失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("腾讯云COS上传失败: " + e.getMessage(), e);
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
            
            log.info("腾讯云COS下载文件: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            return cosClient.getObject(config.getBucketName(), objectKey).getObjectContent();
            
        } catch (Exception e) {
            log.error("腾讯云COS下载失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("腾讯云COS下载失败: " + e.getMessage(), e);
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
            log.error("腾讯云COS删除失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("腾讯云COS删除失败: " + e.getMessage(), e);
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
            log.warn("腾讯云COS检查文件存在失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
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
            
            log.info("腾讯云COS生成预签名URL成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            return url.toString();
            
        } catch (Exception e) {
            log.error("腾讯云COS生成预签名URL失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("腾讯云COS生成预签名URL失败: " + e.getMessage(), e);
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
            UploadCredentialDTO credentialDTO = new UploadCredentialDTO(
                uploadUrl.toString(),
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            
            log.info("腾讯云COS生成上传凭证成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("腾讯云COS生成上传凭证失败: bucket={}, objectKey={}", config.getBucketName(), objectKey, e);
            throw new BusinessException("腾讯云COS生成上传凭证失败: " + e.getMessage(), e);
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
