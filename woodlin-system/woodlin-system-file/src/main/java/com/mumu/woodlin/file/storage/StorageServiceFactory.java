package com.mumu.woodlin.file.storage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.mumu.woodlin.file.enums.StorageType;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 存储服务工厂
 * 
 * @author mumu
 * @description 存储服务工厂类，根据存储类型获取对应的存储服务实现
 * @since 2025-01-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageServiceFactory {
    
    /**
     * 存储服务实例列表
     */
    private final List<StorageService> storageServices;
    
    /**
     * 存储服务缓存
     */
    private final Map<String, StorageService> storageServiceMap = new ConcurrentHashMap<>();
    
    /**
     * 初始化存储服务映射
     */
    @PostConstruct
    public void init() {
        for (StorageService storageService : storageServices) {
            String storageType = storageService.getStorageType();
            storageServiceMap.put(storageType, storageService);
            log.info("注册存储服务: type={}, class={}", storageType, storageService.getClass().getSimpleName());
        }
    }
    
    /**
     * 根据存储类型获取存储服务
     *
     * @param storageType 存储类型
     * @return 存储服务实例
     */
    public StorageService getStorageService(String storageType) {
        StorageService storageService = storageServiceMap.get(storageType);
        if (storageService == null) {
            log.warn("未找到存储类型为 {} 的存储服务，使用默认本地存储", storageType);
            storageService = storageServiceMap.get(StorageType.LOCAL.getCode());
        }
        return storageService;
    }
    
    /**
     * 根据存储类型枚举获取存储服务
     *
     * @param storageType 存储类型枚举
     * @return 存储服务实例
     */
    public StorageService getStorageService(StorageType storageType) {
        return getStorageService(storageType.getCode());
    }
}
