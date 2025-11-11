package com.mumu.woodlin.common.service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.config.CacheProperties;

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

    private final RedissonClient redissonClient;
    private final CacheProperties cacheProperties;

    private static final String DICTIONARY_CACHE_PREFIX = "dict:";
    private static final String CONFIG_CACHE_PREFIX = "config:";
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
            RBucket<List<T>> bucket = redissonClient.getBucket(cacheKey);
            List<T> cachedData = bucket.get();
            if (Objects.nonNull(cachedData)) {
                log.debug("从缓存获取字典数据: {}", dictType);
                return cachedData;
            }

            // 缓存不存在，使用分布式锁防止缓存击穿
            String lockKey = CACHE_LOCK_PREFIX + dictType;
            RBucket<String> lockBucket = redissonClient.getBucket(lockKey);
            
            if (lockBucket.trySet("1", 10, TimeUnit.SECONDS)) {
                try {
                    // 再次检查缓存（双重检查）
                    cachedData = bucket.get();
                    if (Objects.nonNull(cachedData)) {
                        return cachedData;
                    }

                    // 加载数据
                    List<T> data = dataLoader.get();
                    
                    // 存入缓存
                    if (Objects.nonNull(data)) {
                        bucket.set(data, cacheProperties.getDictionary().getExpireSeconds(), TimeUnit.SECONDS);
                        log.info("字典数据已缓存: {}, 大小: {}", dictType, data.size());
                    }
                    
                    return data;
                } finally {
                    // 释放锁
                    lockBucket.delete();
                }
            } else {
                // 获取锁失败，等待一小段时间后重试获取缓存
                Thread.sleep(50);
                cachedData = bucket.get();
                if (Objects.nonNull(cachedData)) {
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
            RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
            
            if (cacheProperties.getDelayedDoubleDelete().getEnabled()) {
                // 延迟双删策略
                deleteWithDelayedDoubleDelete(cacheKey);
            } else {
                // 直接删除
                boolean deleted = bucket.delete();
                log.info("清除字典缓存: {}, 结果: {}", dictType, deleted);
            }
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
            long deleteCount = redissonClient.getKeys().deleteByPattern(DICTIONARY_CACHE_PREFIX + "*");
            log.info("清除所有字典缓存，删除数量: {}", deleteCount);
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
            if (Objects.nonNull(data) && !data.isEmpty()) {
                String cacheKey = DICTIONARY_CACHE_PREFIX + dictType;
                RBucket<List<T>> bucket = redissonClient.getBucket(cacheKey);
                bucket.set(data, cacheProperties.getDictionary().getExpireSeconds(), TimeUnit.SECONDS);
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

    /**
     * 获取配置缓存
     *
     * @param configType 配置类型
     * @param dataLoader 数据加载器，当缓存不存在时调用
     * @return 配置数据
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getConfigCache(String configType, Supplier<List<T>> dataLoader) {
        if (!cacheProperties.getConfig().getEnabled()) {
            // 缓存未启用，直接加载数据
            return dataLoader.get();
        }

        String cacheKey = CONFIG_CACHE_PREFIX + configType;
        
        try {
            // 尝试从缓存获取
            RBucket<List<T>> bucket = redissonClient.getBucket(cacheKey);
            List<T> cachedData = bucket.get();
            if (Objects.nonNull(cachedData)) {
                log.debug("从缓存获取配置数据: {}", configType);
                return cachedData;
            }

            // 缓存不存在，使用分布式锁防止缓存击穿
            String lockKey = CACHE_LOCK_PREFIX + configType;
            RBucket<String> lockBucket = redissonClient.getBucket(lockKey);
            
            if (lockBucket.trySet("1", 10, TimeUnit.SECONDS)) {
                try {
                    // 再次检查缓存（双重检查）
                    cachedData = bucket.get();
                    if (Objects.nonNull(cachedData)) {
                        return cachedData;
                    }

                    // 加载数据
                    List<T> data = dataLoader.get();
                    
                    // 存入缓存
                    if (Objects.nonNull(data)) {
                        bucket.set(data, cacheProperties.getConfig().getExpireSeconds(), TimeUnit.SECONDS);
                        log.info("配置数据已缓存: {}, 大小: {}", configType, data.size());
                    }
                    
                    return data;
                } finally {
                    // 释放锁
                    lockBucket.delete();
                }
            } else {
                // 获取锁失败，等待一小段时间后重试获取缓存
                Thread.sleep(50);
                cachedData = bucket.get();
                if (Objects.nonNull(cachedData)) {
                    return cachedData;
                }
                
                // 仍然获取不到缓存，直接加载数据（降级处理）
                log.warn("获取缓存锁失败，降级直接加载数据: {}", configType);
                return dataLoader.get();
            }
            
        } catch (Exception e) {
            log.error("获取配置缓存失败: {}", configType, e);
            // 缓存异常，降级直接加载数据
            return dataLoader.get();
        }
    }

    /**
     * 清除配置缓存
     *
     * @param configType 配置类型
     */
    public void evictConfigCache(String configType) {
        if (!cacheProperties.getConfig().getEnabled()) {
            return;
        }

        try {
            String cacheKey = CONFIG_CACHE_PREFIX + configType;
            RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
            
            if (cacheProperties.getDelayedDoubleDelete().getEnabled()) {
                // 延迟双删策略
                deleteWithDelayedDoubleDelete(cacheKey);
            } else {
                // 直接删除
                boolean deleted = bucket.delete();
                log.info("清除配置缓存: {}, 结果: {}", configType, deleted);
            }
        } catch (Exception e) {
            log.error("清除配置缓存失败: {}", configType, e);
        }
    }

    /**
     * 清除所有配置缓存
     */
    public void evictAllConfigCache() {
        if (!cacheProperties.getConfig().getEnabled()) {
            return;
        }

        try {
            long deleteCount = redissonClient.getKeys().deleteByPattern(CONFIG_CACHE_PREFIX + "*");
            log.info("清除所有配置缓存，删除数量: {}", deleteCount);
        } catch (Exception e) {
            log.error("清除所有配置缓存失败", e);
        }
    }

    /**
     * 预热配置缓存
     *
     * @param configType 配置类型
     * @param dataLoader 数据加载器
     */
    public <T> void warmupConfigCache(String configType, Supplier<List<T>> dataLoader) {
        if (!cacheProperties.getConfig().getEnabled()) {
            return;
        }

        try {
            List<T> data = dataLoader.get();
            if (Objects.nonNull(data) && !data.isEmpty()) {
                String cacheKey = CONFIG_CACHE_PREFIX + configType;
                RBucket<List<T>> bucket = redissonClient.getBucket(cacheKey);
                bucket.set(data, cacheProperties.getConfig().getExpireSeconds(), TimeUnit.SECONDS);
                log.info("预热配置缓存完成: {}, 大小: {}", configType, data.size());
            }
        } catch (Exception e) {
            log.error("预热配置缓存失败: {}", configType, e);
        }
    }

    /**
     * 延迟双删策略
     * 先删除一次，延迟后再删除一次，防止缓存与数据库不一致
     *
     * @param cacheKey 缓存键
     */
    private void deleteWithDelayedDoubleDelete(String cacheKey) {
        // 第一次删除
        RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
        boolean firstDelete = bucket.delete();
        log.debug("第一次删除缓存: {}, 结果: {}", cacheKey, firstDelete);
        
        // 异步延迟第二次删除
        long delayMillis = cacheProperties.getDelayedDoubleDelete().getDelayMillis();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMillis);
                RBucket<Object> delayBucket = redissonClient.getBucket(cacheKey);
                boolean secondDelete = delayBucket.delete();
                log.debug("第二次删除缓存: {}, 结果: {}", cacheKey, secondDelete);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("延迟双删被中断: {}", cacheKey, e);
            } catch (Exception e) {
                log.error("延迟双删失败: {}", cacheKey, e);
            }
        });
        
        log.info("已触发延迟双删: {}, 延迟: {}ms", cacheKey, delayMillis);
    }
}