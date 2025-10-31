package com.mumu.woodlin.file.storage;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * 阿里云OSS存储服务实现
 * 
 * @author mumu
 * @description 阿里云对象存储服务(OSS)实现，兼容S3协议，使用统一的S3客户端
 *              阿里云OSS提供原生SDK和S3兼容接口，这里使用S3兼容模式以保持代码统一
 * @since 2025-01-30
 */
@Service
public class AliyunOssStorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.OSS.getCode();
    }
}
