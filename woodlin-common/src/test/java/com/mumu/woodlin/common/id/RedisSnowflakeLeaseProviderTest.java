package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisSnowflakeLeaseProviderTest {

    @Test
    void shouldAcquireRedisLease() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        @SuppressWarnings("unchecked")
        RBucket<Object> bucket = mock(RBucket.class);
        when(redissonClient.getBucket("woodlin:id:snowflake:test:slot:0")).thenReturn(bucket);
        when(bucket.trySet(anyString(), eq(5000L), eq(TimeUnit.MILLISECONDS))).thenReturn(true);

        RedisSnowflakeLeaseProvider provider = new RedisSnowflakeLeaseProvider(redissonClient, newProperties(), "test");

        Optional<SnowflakeLease> lease = provider.acquire();

        assertTrue(lease.isPresent());
        assertEquals("REDIS", lease.get().assignment().source());
        assertTrue(lease.get().isDynamic());
    }

    @Test
    void shouldReturnEmptyWhenRedisSlotsAreUnavailable() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        @SuppressWarnings("unchecked")
        RBucket<Object> firstBucket = mock(RBucket.class);
        @SuppressWarnings("unchecked")
        RBucket<Object> secondBucket = mock(RBucket.class);
        when(redissonClient.getBucket("woodlin:id:snowflake:test:slot:0")).thenReturn(firstBucket);
        when(redissonClient.getBucket("woodlin:id:snowflake:test:slot:1")).thenReturn(secondBucket);
        when(firstBucket.trySet(anyString(), eq(5000L), eq(TimeUnit.MILLISECONDS))).thenReturn(false);
        when(secondBucket.trySet(anyString(), eq(5000L), eq(TimeUnit.MILLISECONDS))).thenReturn(false);

        SnowflakeIdProperties properties = newProperties();
        properties.setSlotCount(2);
        RedisSnowflakeLeaseProvider provider = new RedisSnowflakeLeaseProvider(redissonClient, properties, "test");

        assertFalse(provider.acquire().isPresent());
    }

    @Test
    void shouldRenewAndReleaseWithOwnerCheck() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        @SuppressWarnings("unchecked")
        RBucket<Object> bucket = mock(RBucket.class);
        RScript script = mock(RScript.class);
        when(redissonClient.getBucket("woodlin:id:snowflake:test:slot:0")).thenReturn(bucket);
        when(bucket.trySet(anyString(), eq(5000L), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(redissonClient.getScript()).thenReturn(script);
        when(script.eval(
            eq(RScript.Mode.READ_WRITE),
            anyString(),
            eq(RScript.ReturnType.BOOLEAN),
            anyList(),
            any(Object[].class)
        ))
            .thenReturn(true);

        RedisSnowflakeLeaseProvider provider = new RedisSnowflakeLeaseProvider(redissonClient, newProperties(), "test");
        SnowflakeLease lease = provider.acquire().orElseThrow();
        ArgumentCaptor<Object> tokenCaptor = ArgumentCaptor.forClass(Object.class);
        verify(bucket).trySet(tokenCaptor.capture(), eq(5000L), eq(TimeUnit.MILLISECONDS));

        assertTrue(lease.renew(Duration.ofSeconds(5)));
        lease.release();

        ArgumentCaptor<String> scriptCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> valuesCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(script, times(2)).eval(
            eq(RScript.Mode.READ_WRITE),
            scriptCaptor.capture(),
            eq(RScript.ReturnType.BOOLEAN),
            anyList(),
            valuesCaptor.capture()
        );

        assertTrue(scriptCaptor.getAllValues().get(0).contains("redis.call('get', KEYS[1]) == ARGV[1]"));
        assertTrue(scriptCaptor.getAllValues().get(1).contains("redis.call('get', KEYS[1]) == ARGV[1]"));
        assertEquals(tokenCaptor.getValue(), valuesCaptor.getAllValues().get(0)[0]);
        assertEquals(tokenCaptor.getValue(), valuesCaptor.getAllValues().get(1)[0]);
    }

    private SnowflakeIdProperties newProperties() {
        SnowflakeIdProperties properties = new SnowflakeIdProperties();
        properties.setLeaseTtl(Duration.ofSeconds(5));
        properties.setSlotCount(1);
        return properties;
    }
}
