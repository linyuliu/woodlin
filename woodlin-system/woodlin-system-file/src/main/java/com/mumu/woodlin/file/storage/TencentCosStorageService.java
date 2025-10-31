package com.mumu.woodlin.file.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * 腾讯云COS存储服务实现（S3兼容模式）
 * 
 * @author mumu
 * @description 腾讯云对象存储服务(COS)实现，使用S3协议，代码简洁统一
 *              通过配置 woodlin.file.cos.use-native-sdk=false 或不配置时启用（默认）
 *              适用场景：基础文件存储操作，代码简单易维护
 * @since 2025-01-30
 */
@Service
@ConditionalOnProperty(name = "woodlin.file.cos.use-native-sdk", havingValue = "false", matchIfMissing = true)
public class TencentCosStorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.COS.getCode();
    }
}
