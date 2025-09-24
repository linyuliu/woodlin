package com.mumu.woodlin.common.service;

import com.mumu.woodlin.common.config.CacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis二级缓存服务
 *
 * @author mumu
 * @description 提供基于Redis的二级缓存功能，支持字典缓存等
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "woodlin.cache.redis-enabled", havingValue = "true", matchIfMissing = true)
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;

    private static final String DICTIONARY_CACHE_PREFIX = "dict:";
    private static final String CACHE_LOCK_PREFIX = "cache_lock:";

    /**
     * 获取字典缓存
     *
     * @param dictType 字典类型
     * @param dataLoader 数据加载器，当缓存不存在时调用
     * @return 字典数据
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getDictionaryCache(String dictType, Supplier<List<T>> dataLoader) {
        if (!cacheProperties.getDictionary().getEnabled()) {
            // 缓存未启用，直接加载数据
            return dataLoader.get();
        }

        String cacheKey = DICTIONARY_CACHE_PREFIX + dictType;
        
        try {
            // 尝试从缓存获取
            List<T> cachedData = (List<T>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                log.debug("从缓存获取字典数据: {}", dictType);
                return cachedData;
            }

            // 缓存不存在，使用分布式锁防止缓存击穿
            String lockKey = CACHE_LOCK_PREFIX + dictType;
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    // 再次检查缓存（双重检查）
                    cachedData = (List<T>) redisTemplate.opsForValue().get(cacheKey);
                    if (cachedData != null) {
                        return cachedData;
                    }

                    // 加载数据
                    List<T> data = dataLoader.get();
                    
                    // 存入缓存
                    if (data != null) {
                        redisTemplate.opsForValue().set(cacheKey, data, 
                            cacheProperties.getDictionary().getExpireSeconds(), TimeUnit.SECONDS);
                        log.info("字典数据已缓存: {}, 大小: {}", dictType, data.size());
                    }
                    
                    return data;
                } finally {
                    // 释放锁
                    redisTemplate.delete(lockKey);
                }
            } else {
                // 获取锁失败，等待一小段时间后重试获取缓存
                Thread.sleep(50);
                cachedData = (List<T>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedData != null) {
                    return cachedData;
                }
                
                // 仍然获取不到缓存，直接加载数据（降级处理）
                log.warn("获取缓存锁失败，降级直接加载数据: {}", dictType);
                return dataLoader.get();
            }
            
        } catch (Exception e) {
            log.error("获取字典缓存失败: {}", dictType, e);
            // 缓存异常，降级直接加载数据
            return dataLoader.get();
        }
    }

    /**
     * 清除字典缓存
     *
     * @param dictType 字典类型
     */
    public void evictDictionaryCache(String dictType) {
        if (!cacheProperties.getDictionary().getEnabled()) {
            return;
        }

        try {
            String cacheKey = DICTIONARY_CACHE_PREFIX + dictType;
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.info("清除字典缓存: {}, 结果: {}", dictType, deleted);
        } catch (Exception e) {
            log.error("清除字典缓存失败: {}", dictType, e);
        }
    }

    /**
     * 清除所有字典缓存
     */
    public void evictAllDictionaryCache() {
        if (!cacheProperties.getDictionary().getEnabled()) {
            return;
        }

        try {
            redisTemplate.delete(redisTemplate.keys(DICTIONARY_CACHE_PREFIX + "*"));
            log.info("清除所有字典缓存");
        } catch (Exception e) {
            log.error("清除所有字典缓存失败", e);
        }
    }

    /**
     * 预热字典缓存
     *
     * @param dictType 字典类型
     * @param dataLoader 数据加载器
     */
    public <T> void warmupDictionaryCache(String dictType, Supplier<List<T>> dataLoader) {
        if (!cacheProperties.getDictionary().getEnabled()) {
            return;
        }

        try {
            List<T> data = dataLoader.get();
            if (data != null && !data.isEmpty()) {
                String cacheKey = DICTIONARY_CACHE_PREFIX + dictType;
                redisTemplate.opsForValue().set(cacheKey, data, 
                    cacheProperties.getDictionary().getExpireSeconds(), TimeUnit.SECONDS);
                log.info("预热字典缓存完成: {}, 大小: {}", dictType, data.size());
            }
        } catch (Exception e) {
            log.error("预热字典缓存失败: {}", dictType, e);
        }
    }

    /**
     * 获取缓存配置
     *
     * @return 缓存配置
     */
    public CacheProperties getCacheConfig() {
        return cacheProperties;
    }
}