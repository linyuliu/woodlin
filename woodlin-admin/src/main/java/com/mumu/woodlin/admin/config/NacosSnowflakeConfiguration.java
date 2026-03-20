package com.mumu.woodlin.admin.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.lock.LockService;
import com.mumu.woodlin.admin.id.NacosLockServiceFactory;
import com.mumu.woodlin.admin.id.NacosSnowflakeLeaseProvider;
import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Nacos Snowflake 自动配置。
 */
@Configuration
@ConditionalOnClass({NacosFactory.class, LockService.class})
@ConditionalOnProperty(prefix = "woodlin.id.snowflake", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NacosSnowflakeConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "woodlin.id.snowflake.nacos",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public NacosLockServiceFactory nacosLockServiceFactory() {
        return NacosFactory::createLockService;
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "woodlin.id.snowflake.nacos",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public NacosSnowflakeLeaseProvider nacosSnowflakeLeaseProvider(
        SnowflakeIdProperties properties,
        Environment environment,
        NacosLockServiceFactory lockServiceFactory
    ) {
        return new NacosSnowflakeLeaseProvider(properties, environment, lockServiceFactory);
    }
}
