package com.mumu.woodlin.common.config;

import com.mumu.woodlin.common.id.*;
import com.mumu.woodlin.common.util.IdGeneratorUtil;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * Snowflake ID 自动配置。
 */
@Configuration
@EnableConfigurationProperties(SnowflakeIdProperties.class)
@ConditionalOnProperty(prefix = "woodlin.id.snowflake", name = "enabled", havingValue = "true")
public class SnowflakeIdConfiguration {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeIdProperties properties) {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(properties);
        IdGeneratorUtil.setSnowflakeGenerator(generator);
        return generator;
    }

    @Bean
    public ManualSnowflakeLeaseProvider manualSnowflakeLeaseProvider(SnowflakeIdProperties properties) {
        return new ManualSnowflakeLeaseProvider(properties);
    }

    @Bean
    public NetworkSnowflakeLeaseProvider networkSnowflakeLeaseProvider(SnowflakeIdProperties properties) {
        return new NetworkSnowflakeLeaseProvider(properties);
    }

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public RedisSnowflakeLeaseProvider redisSnowflakeLeaseProvider(
        RedissonClient redissonClient,
        SnowflakeIdProperties properties,
        Environment environment
    ) {
        return new RedisSnowflakeLeaseProvider(redissonClient, properties, resolveNamespace(properties, environment));
    }

    @Bean
    public SnowflakeLeaseManager snowflakeLeaseManager(
        List<SnowflakeLeaseProvider> providers,
        SnowflakeIdGenerator generator,
        SnowflakeIdProperties properties
    ) {
        return new SnowflakeLeaseManager(providers, generator, properties);
    }

    private String resolveNamespace(SnowflakeIdProperties properties, Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : null;
        return properties.resolveNamespace(activeProfile);
    }

}
