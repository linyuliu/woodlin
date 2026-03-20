package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import com.mumu.woodlin.common.util.IdGeneratorUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeLeaseManagerTest {

    private SnowflakeLeaseManager manager;

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.stop();
        }
        IdGeneratorUtil.setSnowflakeGenerator(null);
    }

    @Test
    void shouldUseHigherPriorityProvider() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(newProperties());
        SnowflakeLeaseProvider redisProvider = new TestProvider(100, "REDIS", Optional.of(new FixedSnowflakeLease(
            new SnowflakeNodeAssignment("REDIS", 1, 1, 0)
        )));
        SnowflakeLeaseProvider nacosProvider = new TestProvider(200, "NACOS", Optional.of(new FixedSnowflakeLease(
            new SnowflakeNodeAssignment("NACOS", 2, 2, 0)
        )));

        manager = new SnowflakeLeaseManager(List.of(nacosProvider, redisProvider), generator, newProperties());
        manager.start();

        assertEquals("REDIS", generator.currentAssignment().source());
    }

    @Test
    void shouldFallbackWhenDynamicLeaseRenewFails() throws Exception {
        SnowflakeIdProperties properties = newProperties();
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(properties);
        TestDynamicLease redisLease = new TestDynamicLease(new SnowflakeNodeAssignment("REDIS", 3, 3, 0), false);
        AtomicInteger attempts = new AtomicInteger();
        SnowflakeLeaseProvider redisProvider = new TestProvider(100, "REDIS", () -> {
            if (attempts.getAndIncrement() == 0) {
                return Optional.of(redisLease);
            }
            return Optional.empty();
        });
        SnowflakeLeaseProvider manualProvider = new TestProvider(300, "MANUAL", Optional.of(new FixedSnowflakeLease(
            new SnowflakeNodeAssignment("MANUAL", 4, 4, 0)
        )));

        manager = new SnowflakeLeaseManager(List.of(redisProvider, manualProvider), generator, properties);
        manager.start();

        await(() -> generator.currentAssignment() != null && "MANUAL".equals(generator.currentAssignment().source()));

        assertEquals("MANUAL", generator.currentAssignment().source());
        assertTrue(redisLease.released, "旧的动态租约应被主动释放");
    }

    @Test
    void shouldMarkGeneratorUnavailableWhenReacquireFails() throws Exception {
        SnowflakeIdProperties properties = newProperties();
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(properties);
        AtomicInteger attempts = new AtomicInteger();
        SnowflakeLeaseProvider redisProvider = new TestProvider(100, "REDIS", () -> {
            if (attempts.getAndIncrement() == 0) {
                return Optional.of(new TestDynamicLease(new SnowflakeNodeAssignment("REDIS", 5, 5, 0), false));
            }
            return Optional.empty();
        });

        manager = new SnowflakeLeaseManager(List.of(redisProvider), generator, properties);
        manager.start();

        await(() -> generator.currentAssignment() == null);

        assertNull(generator.currentAssignment());
        assertThrows(IllegalStateException.class, generator::nextId);
    }

    private SnowflakeIdProperties newProperties() {
        SnowflakeIdProperties properties = new SnowflakeIdProperties();
        properties.setEpoch(0L);
        properties.setLeaseTtl(Duration.ofMillis(300));
        properties.setHeartbeatInterval(Duration.ofMillis(100));
        properties.setRollbackToleranceMillis(10L);
        return properties;
    }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + 3000L;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
        assertTrue(condition.getAsBoolean(), "等待条件超时");
    }

    private record TestProvider(int order, String source,
                                Supplier<Optional<SnowflakeLease>> supplier) implements SnowflakeLeaseProvider {

            private TestProvider(int order, String source, Optional<SnowflakeLease> lease) {
                this(order, source, () -> lease);
            }

        @Override
            public Optional<SnowflakeLease> acquire() {
                return supplier.get();
            }
        }

    private static final class TestDynamicLease implements SnowflakeLease {

        private final SnowflakeNodeAssignment assignment;
        private final boolean renewResult;
        private boolean released;

        private TestDynamicLease(SnowflakeNodeAssignment assignment, boolean renewResult) {
            this.assignment = assignment;
            this.renewResult = renewResult;
        }

        @Override
        public SnowflakeNodeAssignment assignment() {
            return assignment;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public boolean renew(Duration leaseTtl) {
            return renewResult;
        }

        @Override
        public void release() {
            released = true;
        }
    }
}
