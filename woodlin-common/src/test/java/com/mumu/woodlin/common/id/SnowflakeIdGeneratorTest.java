package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void shouldGenerateMonotonicIds() {
        SnowflakeIdGenerator generator = newGenerator(new long[]{1000L, 1001L, 1002L, 1003L});
        generator.updateAssignment(new SnowflakeNodeAssignment("TEST", 1, 1, 0));

        long first = generator.nextId();
        long second = generator.nextId();
        long third = generator.nextId();

        assertTrue(first < second && second < third, "Snowflake ID 应保持单调递增");
    }

    @Test
    void shouldRemainUniqueInConcurrency() throws Exception {
        SnowflakeIdGenerator generator = newGenerator(() -> System.currentTimeMillis());
        generator.updateAssignment(new SnowflakeNodeAssignment("TEST", 3, 3, 0));

        int threads = 8;
        int perThread = 2000;
        Set<Long> ids = ConcurrentHashMap.newKeySet(threads * perThread);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch finishLatch = new CountDownLatch(threads);
            for (int index = 0; index < threads; index++) {
                pool.execute(() -> {
                    try {
                        startLatch.await();
                        for (int count = 0; count < perThread; count++) {
                            ids.add(generator.nextId());
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            assertTrue(finishLatch.await(30, TimeUnit.SECONDS), "并发生成超时");
        } finally {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertEquals(threads * perThread, ids.size(), "并发生成不应出现重复 ID");
    }

    @Test
    void shouldAdvanceLogicalMillisWhenSequenceOverflows() {
        SnowflakeIdGenerator generator = newGenerator(() -> 1000L);
        generator.updateAssignment(new SnowflakeNodeAssignment("TEST", 7, 7, 0));

        long beforeOverflow = 0L;
        long afterOverflow = 0L;
        for (int index = 0; index <= 4096; index++) {
            long id = generator.nextId();
            if (index == 4095) {
                beforeOverflow = id;
            }
            if (index == 4096) {
                afterOverflow = id;
            }
        }

        assertTrue(
            extractTimestamp(afterOverflow) > extractTimestamp(beforeOverflow),
            "序列溢出后应推进逻辑毫秒"
        );
    }

    @Test
    void shouldTolerateSmallClockRollback() {
        SnowflakeIdGenerator generator = newGenerator(new long[]{1000L, 998L});
        generator.updateAssignment(new SnowflakeNodeAssignment("TEST", 5, 5, 0));

        long first = generator.nextId();
        long second = generator.nextId();

        assertTrue(second > first, "小幅时钟回拨后仍应继续生成唯一 ID");
    }

    @Test
    void shouldRejectLargeClockRollback() {
        SnowflakeIdProperties properties = newProperties();
        properties.setRollbackToleranceMillis(10L);

        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(
            properties,
            new SequenceTimeSupplier(1000L, 900L)
        );
        generator.updateAssignment(new SnowflakeNodeAssignment("TEST", 9, 9, 0));
        generator.nextId();

        IllegalStateException exception = assertThrows(IllegalStateException.class, generator::nextId);
        assertTrue(exception.getMessage().contains("系统时钟回拨超过允许范围"));
    }

    private SnowflakeIdGenerator newGenerator(long[] timestamps) {
        return new SnowflakeIdGenerator(newProperties(), new SequenceTimeSupplier(timestamps));
    }

    private SnowflakeIdGenerator newGenerator(java.util.function.LongSupplier supplier) {
        return new SnowflakeIdGenerator(newProperties(), supplier);
    }

    private SnowflakeIdProperties newProperties() {
        SnowflakeIdProperties properties = new SnowflakeIdProperties();
        properties.setEpoch(0L);
        properties.setLeaseTtl(Duration.ofSeconds(3));
        properties.setRollbackToleranceMillis(5L);
        return properties;
    }

    private long extractTimestamp(long snowflakeId) {
        return snowflakeId >>> 22;
    }

    private static final class SequenceTimeSupplier implements java.util.function.LongSupplier {

        private final long[] timestamps;
        private final AtomicInteger index = new AtomicInteger();

        private SequenceTimeSupplier(long... timestamps) {
            this.timestamps = timestamps;
        }

        @Override
        public long getAsLong() {
            int currentIndex = index.getAndIncrement();
            if (currentIndex >= timestamps.length) {
                return timestamps[timestamps.length - 1];
            }
            return timestamps[currentIndex];
        }
    }
}
