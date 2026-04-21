package com.mumu.woodlin.common.id;

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.lock.LockService;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NacosSnowflakeLeaseProviderTest {

    @Test
    void shouldAcquireNacosLease() throws Exception {
        LockService lockService = mock(LockService.class);
        when(lockService.remoteTryLock(any(LockInstance.class))).thenReturn(true);
        AtomicReference<Properties> capturedProperties = new AtomicReference<>();
        NacosLockServiceFactory factory = properties -> {
            capturedProperties.set(properties);
            return lockService;
        };

        NacosSnowflakeLeaseProvider provider = new NacosSnowflakeLeaseProvider(
            newProperties(),
            newEnvironment(),
            factory
        );

        Optional<SnowflakeLease> lease = provider.acquire();

        assertTrue(lease.isPresent());
        assertEquals("NACOS", lease.get().assignment().source());
        assertEquals("127.0.0.1:8848", capturedProperties.get().getProperty("serverAddr"));
    }

    @Test
    void shouldRenewAndReleaseNacosLease() throws Exception {
        LockService lockService = mock(LockService.class);
        when(lockService.remoteTryLock(any(LockInstance.class))).thenReturn(true);
        when(lockService.remoteReleaseLock(any(LockInstance.class))).thenReturn(true);

        NacosSnowflakeLeaseProvider provider = new NacosSnowflakeLeaseProvider(
            newProperties(),
            newEnvironment(),
            properties -> lockService
        );

        SnowflakeLease lease = provider.acquire().orElseThrow();

        assertTrue(lease.renew(Duration.ofSeconds(5)));
        lease.release();

        verify(lockService, times(2)).remoteTryLock(any(LockInstance.class));
        verify(lockService).remoteReleaseLock(any(LockInstance.class));
    }

    @Test
    void shouldReturnEmptyWhenServerAddrMissing() throws Exception {
        LockService lockService = mock(LockService.class);
        NacosLockServiceFactory factory = properties -> lockService;
        MockEnvironment environment = new MockEnvironment();

        NacosSnowflakeLeaseProvider provider = new NacosSnowflakeLeaseProvider(newProperties(), environment, factory);

        assertFalse(provider.acquire().isPresent());
        verify(lockService, never()).remoteTryLock(any(LockInstance.class));
    }

    @Test
    void shouldGracefullySkipUnsupportedLockFeature() throws Exception {
        LockService lockService = mock(LockService.class);
        when(lockService.remoteTryLock(any(LockInstance.class)))
            .thenThrow(new NacosRuntimeException(501, "Request Nacos server version is too low, not support lock feature."));

        NacosSnowflakeLeaseProvider provider = new NacosSnowflakeLeaseProvider(
            newProperties(),
            newEnvironment(),
            properties -> lockService
        );

        assertTrue(provider.acquire().isEmpty());
        verify(lockService).remoteTryLock(any(LockInstance.class));
    }

    private SnowflakeIdProperties newProperties() {
        SnowflakeIdProperties properties = new SnowflakeIdProperties();
        properties.setSlotCount(1);
        properties.setLeaseTtl(Duration.ofSeconds(5));
        properties.getNacos().setEnabled(true);
        return properties;
    }

    private MockEnvironment newEnvironment() {
        return new MockEnvironment()
            .withProperty("spring.profiles.active", "dev")
            .withProperty("spring.cloud.nacos.server-addr", "127.0.0.1:8848")
            .withProperty("spring.cloud.nacos.config.namespace", "mumu");
    }
}
