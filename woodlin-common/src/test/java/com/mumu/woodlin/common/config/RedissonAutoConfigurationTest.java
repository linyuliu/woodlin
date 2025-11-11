package com.mumu.woodlin.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;

class RedissonAutoConfigurationTest {

    @Test
    void shouldThrowExceptionWhenAddressMissing() {
        RedissonProperties properties = new RedissonProperties();
        properties.getSingleServerConfig().setAddress(null);

        RedissonAutoConfiguration configuration = new RedissonAutoConfiguration(properties);

        assertThatThrownBy(configuration::redissonClient)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Redisson address must be configured");
    }

    @Test
    void shouldCreateRedissonClientWithValidConfig() {
        RedissonProperties properties = new RedissonProperties();
        properties.getSingleServerConfig().setAddress("redis://127.0.0.1:6379");

        RedissonAutoConfiguration configuration = new RedissonAutoConfiguration(properties);
        RedissonClient redissonClient = mock(RedissonClient.class);

        try (MockedStatic<Redisson> mockedRedisson = mockStatic(Redisson.class)) {
            mockedRedisson.when(() -> Redisson.create(any(Config.class))).thenReturn(redissonClient);

            RedissonClient client = configuration.redissonClient();
            assertThat(client).isSameAs(redissonClient);
        }
    }

    @Test
    void shouldCreateRedissonConnectionFactory() {
        RedissonClient client = mock(RedissonClient.class);
        RedissonAutoConfiguration configuration = new RedissonAutoConfiguration(new RedissonProperties());

        RedissonConnectionFactory connectionFactory = configuration.redissonConnectionFactory(client);
        assertThat(connectionFactory).isNotNull();
    }
}
