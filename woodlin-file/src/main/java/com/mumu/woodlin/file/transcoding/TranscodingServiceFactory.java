package com.mumu.woodlin.file.transcoding;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.mumu.woodlin.file.enums.TranscodingType;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 转码服务工厂
 * 
 * @author mumu
 * @description 转码服务工厂类，根据转码类型获取对应的转码服务实现
 *              使用策略模式，方便扩展新的转码实现
 * @since 2025-01-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranscodingServiceFactory {
    
    /**
     * 转码服务实例列表（Spring自动注入所有实现）
     */
    private final List<TranscodingService> transcodingServices;
    
    /**
     * 转码服务缓存
     */
    private final Map<TranscodingType, TranscodingService> serviceMap = new ConcurrentHashMap<>();
    
    /**
     * 源格式到转码服务的映射
     */
    private final Map<String, TranscodingService> formatServiceMap = new ConcurrentHashMap<>();
    
    /**
     * 初始化转码服务映射
     */
    @PostConstruct
    public void init() {
        for (TranscodingService service : transcodingServices) {
            TranscodingType type = service.getTranscodingType();
            serviceMap.put(type, service);
            log.info("注册转码服务: type={}, class={}", 
                type.getName(), service.getClass().getSimpleName());
        }
    }
    
    /**
     * 根据转码类型获取转码服务
     *
     * @param transcodingType 转码类型
     * @return 转码服务实例
     */
    public TranscodingService getService(TranscodingType transcodingType) {
        TranscodingService service = serviceMap.get(transcodingType);
        if (service == null) {
            throw new UnsupportedOperationException("不支持的转码类型: " + transcodingType);
        }
        return service;
    }
    
    /**
     * 根据源文件格式获取合适的转码服务
     *
     * @param sourceFormat 源文件格式
     * @param targetFormat 目标格式
     * @return 转码服务实例
     */
    public TranscodingService getServiceForFormat(String sourceFormat, String targetFormat) {
        // 优先从缓存获取
        String key = sourceFormat + ":" + targetFormat;
        TranscodingService cached = formatServiceMap.get(key);
        if (cached != null) {
            return cached;
        }
        
        // 查找支持该格式的服务
        for (TranscodingService service : transcodingServices) {
            if (service.supports(sourceFormat)) {
                formatServiceMap.put(key, service);
                return service;
            }
        }
        
        throw new UnsupportedOperationException(
            "不支持的文件格式转换: " + sourceFormat + " -> " + targetFormat
        );
    }
    
    /**
     * 判断是否支持该格式转换
     *
     * @param sourceFormat 源文件格式
     * @return 是否支持
     */
    public boolean supportsFormat(String sourceFormat) {
        for (TranscodingService service : transcodingServices) {
            if (service.supports(sourceFormat)) {
                return true;
            }
        }
        return false;
    }
}
