package com.mumu.woodlin.common.config;

import com.mumu.woodlin.common.service.RedisCacheService;
import com.mumu.woodlin.common.service.SearchableEncryptionService;
import com.mumu.woodlin.common.util.RedisUtil;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * woodlin-common-db 自动装配。
 */
@AutoConfiguration
@EnableConfigurationProperties({
    CacheProperties.class,
    RedissonProperties.class,
    SearchableEncryptionProperties.class
})
@Import({
    RedisConfig.class,
    RedissonAutoConfiguration.class
})
public class CommonDbAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "woodlin.cache.redis-enabled", havingValue = "true", matchIfMissing = true)
    public RedisCacheService redisCacheService(RedissonClient redissonClient, CacheProperties cacheProperties) {
        return new RedisCacheService(redissonClient, cacheProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisUtil redisUtil(RedissonClient redissonClient) {
        return new RedisUtil(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "woodlin.searchable-encryption", name = "enabled", havingValue = "true")
    public SearchableEncryptionService searchableEncryptionService(SearchableEncryptionProperties properties) {
        return new SearchableEncryptionService(properties);
    }
}
