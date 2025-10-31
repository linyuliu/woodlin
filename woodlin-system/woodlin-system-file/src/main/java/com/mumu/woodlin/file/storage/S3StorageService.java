package com.mumu.woodlin.file.storage;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * AWS S3存储服务实现
 * 
 * @author mumu
 * @description AWS S3对象存储服务实现，使用统一的S3兼容客户端
 * @since 2025-01-30
 */
@Service
public class S3StorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.S3.getCode();
    }
}
