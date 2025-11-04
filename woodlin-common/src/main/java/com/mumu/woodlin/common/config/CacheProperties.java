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

    /**
     * 系统配置缓存配置
     */
    private ConfigCache config = new ConfigCache();

    /**
     * 权限缓存配置
     */
    private PermissionCache permission = new PermissionCache();

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

    @Data
    public static class ConfigCache {
        /**
         * 是否启用配置缓存
         */
        private Boolean enabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private Long expireSeconds = 7200L;

        /**
         * 缓存刷新间隔（秒）
         */
        private Long refreshIntervalSeconds = 3600L;
    }

    @Data
    public static class PermissionCache {
        /**
         * 是否启用权限缓存
         */
        private Boolean enabled = true;

        /**
         * 用户权限缓存过期时间（秒）
         */
        private Long expireSeconds = 1800L;

        /**
         * 角色权限缓存过期时间（秒）
         */
        private Long roleExpireSeconds = 3600L;
    }
}