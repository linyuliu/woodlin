package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson配置属性
 * 
 * @author mumu
 * @description Redisson客户端配置属性，支持单机、集群、哨兵等多种部署模式
 * @since 2025-01-01
 */
@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    /**
     * 单机服务器配置
     */
    private SingleServerConfig singleServerConfig = new SingleServerConfig();

    /**
     * 单机服务器配置
     */
    @Data
    public static class SingleServerConfig {
        
        /**
         * Redis服务器地址，格式：redis://host:port
         */
        private String address;
        
        /**
         * 数据库索引，默认为0
         */
        private Integer database = 0;
        
        /**
         * 密码，如果没有设置密码可以为空
         */
        private String password;
        
        /**
         * 连接池大小，默认64
         */
        private Integer connectionPoolSize = 64;
        
        /**
         * 最小空闲连接数，默认10
         */
        private Integer connectionMinimumIdleSize = 10;
        
        /**
         * 空闲连接超时时间（毫秒），默认10000
         */
        private Integer idleConnectionTimeout = 10000;
        
        /**
         * 连接超时时间（毫秒），默认10000
         */
        private Integer connectTimeout = 10000;
        
        /**
         * 命令执行超时时间（毫秒），默认3000
         */
        private Integer timeout = 3000;
        
        /**
         * 命令失败重试次数，默认3
         */
        private Integer retryAttempts = 3;
        
        /**
         * 命令重试间隔（毫秒），默认1500
         */
        private Integer retryInterval = 1500;
    }
}
