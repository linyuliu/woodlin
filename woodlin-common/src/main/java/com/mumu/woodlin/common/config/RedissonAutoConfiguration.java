package com.mumu.woodlin.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 自动配置
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonAutoConfiguration {

    private final RedissonProperties properties;

    public RedissonAutoConfiguration(RedissonProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        RedissonProperties.SingleServerConfig serverConfig = properties.getSingleServerConfig();
        if (serverConfig == null || !StringUtils.hasText(serverConfig.getAddress())) {
            throw new IllegalStateException("Redisson address must be configured");
        }

        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(serverConfig.getAddress());

        if (serverConfig.getDatabase() != null) {
            singleServerConfig.setDatabase(serverConfig.getDatabase());
        }
        if (StringUtils.hasText(serverConfig.getPassword())) {
            singleServerConfig.setPassword(serverConfig.getPassword());
        }
        if (serverConfig.getConnectionPoolSize() != null) {
            singleServerConfig.setConnectionPoolSize(serverConfig.getConnectionPoolSize());
        }
        if (serverConfig.getConnectionMinimumIdleSize() != null) {
            singleServerConfig.setConnectionMinimumIdleSize(serverConfig.getConnectionMinimumIdleSize());
        }
        if (serverConfig.getIdleConnectionTimeout() != null) {
            singleServerConfig.setIdleConnectionTimeout(serverConfig.getIdleConnectionTimeout());
        }
        if (serverConfig.getConnectTimeout() != null) {
            singleServerConfig.setConnectTimeout(serverConfig.getConnectTimeout());
        }
        if (serverConfig.getTimeout() != null) {
            singleServerConfig.setTimeout(serverConfig.getTimeout());
        }
        if (serverConfig.getRetryAttempts() != null) {
            singleServerConfig.setRetryAttempts(serverConfig.getRetryAttempts());
        }
        if (serverConfig.getRetryInterval() != null) {
            singleServerConfig.setRetryInterval(serverConfig.getRetryInterval());
        }

        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }
}
