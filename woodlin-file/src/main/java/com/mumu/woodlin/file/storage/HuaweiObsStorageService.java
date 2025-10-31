package com.mumu.woodlin.file.storage;

import org.springframework.stereotype.Service;

import com.mumu.woodlin.file.enums.StorageType;

/**
 * 华为云OBS存储服务实现
 * 
 * @author mumu
 * @description 华为云对象存储服务(OBS)实现，兼容S3协议，使用统一的S3客户端
 *              华为云OBS提供原生SDK和S3兼容接口，这里使用S3兼容模式以保持代码统一
 * @since 2025-01-30
 */
@Service
public class HuaweiObsStorageService extends AbstractS3CompatibleStorageService {
    
    @Override
    public String getStorageType() {
        return StorageType.OBS.getCode();
    }
}
