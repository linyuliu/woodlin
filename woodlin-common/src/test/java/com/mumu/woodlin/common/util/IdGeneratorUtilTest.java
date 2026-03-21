package com.mumu.woodlin.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorUtilTest {

    private static final Pattern ULID_PATTERN = Pattern.compile("[0-9A-HJKMNP-TV-Z]{26}");

    @AfterEach
    void resetDefaultStrategy() {
        IdGeneratorUtil.setDefaultStrategy(IdGeneratorUtil.Strategy.UUID_V7);
        IdGeneratorUtil.setSnowflakeGenerator(null);
    }

    @Test
    void shouldGenerateUuidV7ByDefault() {
        String id = IdGeneratorUtil.next();
        UUID uuid = UUID.fromString(id);

        assertAll(
            () -> assertNotNull(id),
            () -> assertEquals(7, uuid.version()),
            () -> assertEquals(2, uuid.variant())
        );
    }

    @Test
    void shouldSupportAllStrategies() {
        UUID uuidV4 = UUID.fromString(IdGeneratorUtil.next(IdGeneratorUtil.Strategy.UUID_V4));
        UUID uuidV7 = UUID.fromString(IdGeneratorUtil.next(IdGeneratorUtil.Strategy.UUID_V7));
        String ulid = IdGeneratorUtil.next(IdGeneratorUtil.Strategy.ULID);

        assertAll(
            () -> assertEquals(4, uuidV4.version()),
            () -> assertEquals(7, uuidV7.version()),
            () -> assertEquals(26, ulid.length()),
            () -> assertTrue(ULID_PATTERN.matcher(ulid).matches(), "ULID 字符集不合法")
        );
    }

    @Test
    void shouldSupportSnowflakeStrategy() {
        IdGeneratorUtil.setSnowflakeGenerator(() -> 123456789L);

        assertAll(
            () -> assertEquals("123456789", IdGeneratorUtil.next(IdGeneratorUtil.Strategy.SNOWFLAKE)),
            () -> assertEquals(123456789L, IdGeneratorUtil.nextLong(IdGeneratorUtil.Strategy.SNOWFLAKE))
        );
    }

    @Test
    void shouldGenerateUuidObjectsForUuidStrategies() {
        UUID uuidV4 = IdGeneratorUtil.nextUuid(IdGeneratorUtil.Strategy.UUID_V4);
        UUID uuidV7 = IdGeneratorUtil.nextUuid(IdGeneratorUtil.Strategy.UUID_V7);

        assertAll(
            () -> assertEquals(4, uuidV4.version()),
            () -> assertEquals(7, uuidV7.version()),
            () -> assertEquals(2, uuidV7.variant())
        );
    }

    @Test
    void shouldRejectUlidAsUuid() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.nextUuid(IdGeneratorUtil.Strategy.ULID)
        );

        assertEquals("ULID 策略不支持 UUID 输出", exception.getMessage());
    }

    @Test
    void shouldRejectUnsupportedLongStrategies() {
        IllegalArgumentException uuidException = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.nextLong(IdGeneratorUtil.Strategy.UUID_V7)
        );
        IllegalArgumentException ulidException = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.nextLong(IdGeneratorUtil.Strategy.ULID)
        );

        assertAll(
            () -> assertEquals("UUID_V7 策略不支持 long 输出", uuidException.getMessage()),
            () -> assertEquals("ULID 策略不支持 long 输出", ulidException.getMessage())
        );
    }

    @Test
    void shouldApplyDefaultStrategy() {
        IdGeneratorUtil.setDefaultStrategy(IdGeneratorUtil.Strategy.ULID);

        String id = IdGeneratorUtil.next();

        assertTrue(ULID_PATTERN.matcher(id).matches(), "默认策略切换后应输出 ULID");
    }

    @Test
    void shouldUseCustomDefaultGenerator() {
        IdGeneratorUtil.setDefaultGenerator(() -> "fixed-id");

        assertEquals("fixed-id", IdGeneratorUtil.next());
    }

    @Test
    void shouldRejectNullStrategy() {
        IllegalArgumentException nextException = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.next(null)
        );
        IllegalArgumentException nextUuidException = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.nextUuid(null)
        );
        IllegalArgumentException defaultStrategyException = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.setDefaultStrategy(null)
        );

        assertAll(
            () -> assertEquals("生成策略不能为空", nextException.getMessage()),
            () -> assertEquals("生成策略不能为空", nextUuidException.getMessage()),
            () -> assertEquals("生成策略不能为空", defaultStrategyException.getMessage())
        );
    }

    @Test
    void shouldRejectNullGenerator() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> IdGeneratorUtil.setDefaultGenerator(null)
        );

        assertEquals("生成器不能为空", exception.getMessage());
    }

    @Test
    void shouldKeepUuidV7TimestampNonDecreasing() {
        List<Long> timestamps = new ArrayList<>();

        for (int index = 0; index < 512; index++) {
            UUID uuid = IdGeneratorUtil.nextUuid(IdGeneratorUtil.Strategy.UUID_V7);
            timestamps.add(extractTimestamp(uuid));
        }

        for (int index = 1; index < timestamps.size(); index++) {
            assertTrue(
                timestamps.get(index) >= timestamps.get(index - 1),
                "UUIDv7 时间部分不应回退"
            );
        }
    }

    @Test
    void shouldGenerateValidUlid() {
        String ulid = IdGeneratorUtil.next(IdGeneratorUtil.Strategy.ULID);

        assertAll(
            () -> assertEquals(26, ulid.length()),
            () -> assertTrue(ULID_PATTERN.matcher(ulid).matches(), "ULID 字符集不合法")
        );
    }

    @Test
    void shouldGenerateUniqueIdsInConcurrency() throws Exception {
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        int perThread = 5000;
        int total = threads * perThread;

        Set<String> ids = ConcurrentHashMap.newKeySet(total);
        CountDownLatch startLatch = new CountDownLatch(1);
        boolean finished;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch finishLatch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                pool.execute(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < perThread; j++) {
                            ids.add(IdGeneratorUtil.next());
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            finished = finishLatch.await(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertTrue(finished, "并发生成超时");
        assertEquals(total, ids.size(), "当前测试规模下不应出现重复 ID");
    }

    private long extractTimestamp(UUID uuid) {
        return (uuid.getMostSignificantBits() >>> 16) & 0xFFFF_FFFF_FFFFL;
    }
}
