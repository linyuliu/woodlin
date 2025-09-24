package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 缓存配置属性
 *
 * @author mumu
 * @description 缓存相关的配置属性，包括Redis二级缓存开关等
 * @since 2025-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.cache")
public class CacheProperties {

    /**
     * 是否启用Redis二级缓存
     */
    private Boolean redisEnabled = true;

    /**
     * 字典缓存配置
     */
    private DictionaryCache dictionary = new DictionaryCache();

    @Data
    public static class DictionaryCache {
        /**
         * 是否启用字典缓存
         */
        private Boolean enabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private Long expireSeconds = 3600L;

        /**
         * 缓存刷新间隔（秒）
         */
        private Long refreshIntervalSeconds = 1800L;
    }
}