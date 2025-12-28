package com.mumu.woodlin.common.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 自动配置
 * 
 * @author mumu
 * @description 自动配置Redisson客户端，支持从Spring Redis配置或自定义Redisson配置读取
 *              优先使用自定义配置，如果未配置则从Spring Redis配置读取
 * @since 2025-01-01
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties({RedissonProperties.class, RedisProperties.class})
public class RedissonAutoConfiguration {

    private final RedissonProperties redissonProperties;
    private final RedisProperties redisProperties;

    public RedissonAutoConfiguration(RedissonProperties redissonProperties, RedisProperties redisProperties) {
        this.redissonProperties = redissonProperties;
        this.redisProperties = redisProperties;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();

        // 优先使用自定义Redisson配置，否则使用Spring Redis配置
        RedissonProperties.SingleServerConfig customConfig = redissonProperties.getSingleServerConfig();
        if (customConfig != null && StringUtils.hasText(customConfig.getAddress())) {
            // 使用自定义Redisson配置
            configureFromCustomProperties(singleServerConfig, customConfig);
        } else {
            // 使用Spring Redis配置
            configureFromSpringRedis(singleServerConfig);
        }

        return Redisson.create(config);
    }

    /**
     * 从自定义Redisson配置中读取参数
     */
    private void configureFromCustomProperties(SingleServerConfig config, RedissonProperties.SingleServerConfig customConfig) {
        config.setAddress(customConfig.getAddress());

        if (customConfig.getDatabase() != null) {
            config.setDatabase(customConfig.getDatabase());
        }
        if (StringUtils.hasText(customConfig.getPassword())) {
            config.setPassword(customConfig.getPassword());
        }
        if (customConfig.getConnectionPoolSize() != null) {
            config.setConnectionPoolSize(customConfig.getConnectionPoolSize());
        }
        if (customConfig.getConnectionMinimumIdleSize() != null) {
            config.setConnectionMinimumIdleSize(customConfig.getConnectionMinimumIdleSize());
        }
        if (customConfig.getIdleConnectionTimeout() != null) {
            config.setIdleConnectionTimeout(customConfig.getIdleConnectionTimeout());
        }
        if (customConfig.getConnectTimeout() != null) {
            config.setConnectTimeout(customConfig.getConnectTimeout());
        }
        if (customConfig.getTimeout() != null) {
            config.setTimeout(customConfig.getTimeout());
        }
        if (customConfig.getRetryAttempts() != null) {
            config.setRetryAttempts(customConfig.getRetryAttempts());
        }
        // Note: setRetryInterval is deprecated and removed - using default retry interval
    }

    /**
     * 从Spring Redis配置中读取参数
     */
    private void configureFromSpringRedis(SingleServerConfig config) {
        String host = redisProperties.getHost() != null ? redisProperties.getHost() : "localhost";
        int port = redisProperties.getPort() != 0 ? redisProperties.getPort() : 6379;
        String address = String.format("redis://%s:%d", host, port);
        config.setAddress(address);

        if (redisProperties.getDatabase() != 0) {
            config.setDatabase(redisProperties.getDatabase());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            config.setPassword(redisProperties.getPassword());
        }
        if (redisProperties.getTimeout() != null) {
            config.setTimeout((int) redisProperties.getTimeout().toMillis());
        }
        if (redisProperties.getLettuce() != null && redisProperties.getLettuce().getPool() != null) {
            RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
            if (pool.getMaxActive() > 0) {
                config.setConnectionPoolSize(pool.getMaxActive());
            }
            if (pool.getMinIdle() > 0) {
                config.setConnectionMinimumIdleSize(pool.getMinIdle());
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }
}
