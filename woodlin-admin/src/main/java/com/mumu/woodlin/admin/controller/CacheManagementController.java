package com.mumu.woodlin.admin.controller;

import com.mumu.woodlin.common.config.CacheProperties;
import com.mumu.woodlin.common.service.RedisCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存管理控制器
 *
 * @author mumu
 * @description 缓存管理相关接口，包括缓存配置、清理等
 * @since 2025-01-01
 */
@Tag(name = "缓存管理", description = "Redis缓存管理接口")
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "woodlin.cache.redis-enabled", havingValue = "true")
public class CacheManagementController {

    private final RedisCacheService redisCacheService;

    /**
     * 获取缓存配置信息
     */
    @Operation(summary = "获取缓存配置信息", description = "获取当前的Redis缓存配置信息")
    @GetMapping("/config")
    public CacheProperties getCacheConfig() {
        return redisCacheService.getCacheConfig();
    }

    /**
     * 清除指定字典缓存
     */
    @Operation(summary = "清除字典缓存", description = "清除指定类型的字典缓存")
    @DeleteMapping("/dictionary/{dictType}")
    public CacheOperationResult evictDictionaryCache(@PathVariable String dictType) {
        redisCacheService.evictDictionaryCache(dictType);
        return new CacheOperationResult(true, "字典缓存清除成功: " + dictType);
    }

    /**
     * 清除所有字典缓存
     */
    @Operation(summary = "清除所有字典缓存", description = "清除系统中所有的字典缓存")
    @DeleteMapping("/dictionary/all")
    public CacheOperationResult evictAllDictionaryCache() {
        redisCacheService.evictAllDictionaryCache();
        return new CacheOperationResult(true, "所有字典缓存清除成功");
    }

    /**
     * 预热字典缓存（示例）
     */
    @Operation(summary = "预热字典缓存", description = "预热指定类型的字典缓存")
    @PostMapping("/dictionary/{dictType}/warmup")
    public CacheOperationResult warmupDictionaryCache(@PathVariable String dictType) {
        // 这里只是示例，实际使用时需要根据具体的字典数据源进行加载
        redisCacheService.warmupDictionaryCache(dictType, () -> {
            // 模拟字典数据加载
            return java.util.Arrays.asList("示例数据1", "示例数据2");
        });
        return new CacheOperationResult(true, "字典缓存预热成功: " + dictType);
    }

    /**
     * 缓存操作结果响应类
     */
    @Data
    public static class CacheOperationResult {
        private boolean success;
        private String message;
        private long timestamp;

        public CacheOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
