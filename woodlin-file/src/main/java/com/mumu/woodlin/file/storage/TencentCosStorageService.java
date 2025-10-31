package com.mumu.woodlin.file.storage;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * 腾讯云COS存储服务实现
 * 
 * @author mumu
 * @description 腾讯云对象存储服务(COS)实现，兼容S3协议，使用统一的S3客户端
 *              腾讯云COS提供原生SDK和S3兼容接口，这里使用S3兼容模式以保持代码统一
 * @since 2025-01-30
 */
@Service
public class TencentCosStorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.COS.getCode();
    }
}
