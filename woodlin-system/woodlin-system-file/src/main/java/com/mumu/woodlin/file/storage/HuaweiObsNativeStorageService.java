package com.mumu.woodlin.file.storage;

import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.dto.UploadCredentialDTO;
import com.mumu.woodlin.file.entity.SysStorageConfig;
import com.mumu.woodlin.file.enums.StorageType;
import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.TemporarySignatureRequest;

import cn.hutool.json.JSONUtil;
import com.mumu.woodlin.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * 华为云OBS原生SDK存储服务实现
 * 
 * @author mumu
 * @description 使用华为云OBS原生SDK实现，支持华为云OBS的所有高级功能
 *              通过配置 woodlin.file.obs.use-native-sdk=true 启用
 *              适用场景：需要使用华为云OBS特有功能（如数据处理、内容审核等）
 * @since 2025-01-31
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "woodlin.file.obs.use-native-sdk", havingValue = "true")
public class HuaweiObsNativeStorageService implements StorageService {
    
    /**
     * 创建OBS客户端
     *
     * @param config 存储配置
     * @return OBS客户端
     */
    private ObsClient createClient(SysStorageConfig config) {
        return new ObsClient(
            config.getAccessKey(),
            config.getSecretKey(),
            config.getEndpoint()
        );
    }
    
    @Override
    public String uploadFile(SysStorageConfig config, String objectKey, InputStream inputStream,
                            String contentType, long fileSize) {
        ObsClient obsClient = null;
        try {
            obsClient = createClient(config);
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            
            // 上传文件
            PutObjectRequest request = new PutObjectRequest(config.getBucketName(), objectKey, inputStream);
            request.setMetadata(metadata);
            
            obsClient.putObject(request);
            
            log.info("华为云OBS(原生SDK)上传成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
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
            log.error("华为云OBS(原生SDK)上传失败: objectKey={}", objectKey, e);
            throw new BusinessException("华为云OBS上传失败: " + e.getMessage(), e);
        } finally {
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (Exception e) {
                    log.error("关闭OBS客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public InputStream downloadFile(SysStorageConfig config, String objectKey) {
        ObsClient obsClient = null;
        try {
            obsClient = createClient(config);
            
            return obsClient.getObject(config.getBucketName(), objectKey).getObjectContent();
            
        } catch (Exception e) {
            log.error("华为云OBS(原生SDK)下载失败: objectKey={}", objectKey, e);
            throw new BusinessException("华为云OBS下载失败: " + e.getMessage(), e);
        } finally {
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (Exception e) {
                    log.error("关闭OBS客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public void deleteFile(SysStorageConfig config, String objectKey) {
        ObsClient obsClient = null;
        try {
            obsClient = createClient(config);
            
            obsClient.deleteObject(config.getBucketName(), objectKey);
            
            log.info("华为云OBS(原生SDK)删除成功: bucket={}, objectKey={}", config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("华为云OBS(原生SDK)删除失败: objectKey={}", objectKey, e);
            throw new BusinessException("华为云OBS删除失败: " + e.getMessage(), e);
        } finally {
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (Exception e) {
                    log.error("关闭OBS客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public boolean fileExists(SysStorageConfig config, String objectKey) {
        ObsClient obsClient = null;
        try {
            obsClient = createClient(config);
            
            return obsClient.doesObjectExist(config.getBucketName(), objectKey);
            
        } catch (Exception e) {
            log.error("华为云OBS(原生SDK)检查文件存在失败: objectKey={}", objectKey, e);
            return false;
        } finally {
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (Exception e) {
                    log.error("关闭OBS客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public String generatePresignedUrl(SysStorageConfig config, String objectKey, int expirationTime) {
        ObsClient obsClient = null;
        try {
            obsClient = createClient(config);
            
            // 生成预签名URL
            TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.GET,
                expirationTime
            );
            request.setBucketName(config.getBucketName());
            request.setObjectKey(objectKey);
            
            return obsClient.createTemporarySignature(request).getSignedUrl();
            
        } catch (Exception e) {
            log.error("华为云OBS(原生SDK)生成预签名URL失败: objectKey={}", objectKey, e);
            throw new BusinessException("华为云OBS生成预签名URL失败: " + e.getMessage(), e);
        } finally {
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (Exception e) {
                    log.error("关闭OBS客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public String generateUploadCredential(SysStorageConfig config, String objectKey, int expirationTime) {
        ObsClient obsClient = null;
        try {
            obsClient = createClient(config);
            
            // 生成上传预签名URL
            TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.PUT,
                expirationTime
            );
            request.setBucketName(config.getBucketName());
            request.setObjectKey(objectKey);
            
            String uploadUrl = obsClient.createTemporarySignature(request).getSignedUrl();
            
            // 返回上传凭证
            UploadCredentialDTO credentialDTO = new UploadCredentialDTO(
                uploadUrl,
                config.getBucketName(),
                objectKey,
                expirationTime
            );
            return JSONUtil.toJsonStr(credentialDTO);
            
        } catch (Exception e) {
            log.error("华为云OBS(原生SDK)生成上传凭证失败: objectKey={}", objectKey, e);
            throw new BusinessException("华为云OBS生成上传凭证失败: " + e.getMessage(), e);
        } finally {
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (Exception e) {
                    log.error("关闭OBS客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public String getStorageType() {
        return StorageType.OBS.getCode();
    }
}
