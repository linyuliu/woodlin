package com.mumu.woodlin.file.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * 阿里云OSS存储服务实现（S3兼容模式）
 * 
 * @author mumu
 * @description 阿里云对象存储服务(OSS)实现，使用S3协议，代码简洁统一
 *              通过配置 woodlin.file.oss.use-native-sdk=false 或不配置时启用（默认）
 *              适用场景：基础文件存储操作，代码简单易维护
 * @since 2025-01-30
 */
@Service
@ConditionalOnProperty(name = "woodlin.file.oss.use-native-sdk", havingValue = "false", matchIfMissing = true)
public class AliyunOssStorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.OSS.getCode();
    }
}
