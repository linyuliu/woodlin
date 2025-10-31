package com.mumu.woodlin.file.storage;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * MinIO存储服务实现
 * 
 * @author mumu
 * @description MinIO对象存储服务实现，完全兼容S3协议，使用统一的S3客户端
 *              MinIO是开源的对象存储服务，支持私有化部署
 * @since 2025-01-30
 */
@Service
public class MinioStorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.MINIO.getCode();
    }
}
