package com.mumu.woodlin.common.config;

import com.alibaba.nacos.api.lock.LockService;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.mumu.woodlin.common.id.*;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NacosSnowflakeConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(NacosSnowflakeConfiguration.class)
        .withBean(SnowflakeIdProperties.class, this::newProperties)
        .withPropertyValues(
            "spring.profiles.active=dev",
            "spring.cloud.nacos.server-addr=127.0.0.1:8848",
            "woodlin.id.snowflake.enabled=true"
        );

    @Test
    void shouldRegisterNacosProviderWhenExplicitlyEnabled() {
        contextRunner
            .withPropertyValues("woodlin.id.snowflake.nacos.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(NacosLockServiceFactory.class);
                assertThat(context).hasSingleBean(NacosSnowflakeLeaseProvider.class);
            });
    }

    @Test
    void shouldSkipNacosProviderByDefault() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(NacosLockServiceFactory.class);
                assertThat(context).doesNotHaveBean(NacosSnowflakeLeaseProvider.class);
            });
    }

    @Test
    void shouldSkipNacosProviderWhenDisabled() {
        contextRunner
            .withPropertyValues("woodlin.id.snowflake.nacos.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(NacosLockServiceFactory.class);
                assertThat(context).doesNotHaveBean(NacosSnowflakeLeaseProvider.class);
            });
    }

    @Test
    void shouldPreferRedisLeaseWhenRedisAvailable() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        @SuppressWarnings("unchecked")
        RBucket<String> bucket = mock(RBucket.class);
        RScript script = mock(RScript.class);
        LockService lockService = mock(LockService.class);
        NacosLockServiceFactory lockServiceFactory = mock(NacosLockServiceFactory.class);

        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.trySet(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(redissonClient.getScript()).thenReturn(script);
        when(script.eval(
            eq(RScript.Mode.READ_WRITE),
            anyString(),
            eq(RScript.ReturnType.BOOLEAN),
            anyList(),
            any(),
            any()
        )).thenReturn(Boolean.TRUE);
        when(lockServiceFactory.create(any())).thenReturn(lockService);

        contextRunner
            .withPropertyValues("woodlin.id.snowflake.nacos.enabled=true")
            .withBean(
                "mockNacosLockServiceFactory",
                NacosLockServiceFactory.class,
                () -> lockServiceFactory,
                beanDefinition -> beanDefinition.setPrimary(true)
            )
            .withBean(SnowflakeIdGenerator.class, () -> new SnowflakeIdGenerator(newProperties()))
            .withBean(
                RedisSnowflakeLeaseProvider.class,
                () -> new RedisSnowflakeLeaseProvider(redissonClient, newProperties(), "woodlin-dev")
            )
            .run(context -> {
                SnowflakeIdGenerator generator = context.getBean(SnowflakeIdGenerator.class);
                SnowflakeLeaseManager leaseManager = new SnowflakeLeaseManager(
                    List.of(
                        context.getBean(RedisSnowflakeLeaseProvider.class),
                        context.getBean(NacosSnowflakeLeaseProvider.class)
                    ),
                    generator,
                    context.getBean(SnowflakeIdProperties.class)
                );

                leaseManager.start();
                try {
                    assertThat(generator.currentAssignment().source()).isEqualTo("REDIS");
                } finally {
                    leaseManager.stop();
                }
            });

        verify(lockServiceFactory, never()).create(any());
        verify(lockService, never()).remoteTryLock(any(LockInstance.class));
    }

    private SnowflakeIdProperties newProperties() {
        SnowflakeIdProperties properties = new SnowflakeIdProperties();
        properties.setSlotCount(1);
        properties.setLeaseTtl(Duration.ofSeconds(5));
        properties.setHeartbeatInterval(Duration.ofSeconds(5));
        return properties;
    }
}
