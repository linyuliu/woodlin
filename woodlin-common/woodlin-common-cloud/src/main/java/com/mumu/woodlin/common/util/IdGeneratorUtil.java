package com.mumu.woodlin.common.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用途：生成应用侧字符串 ID。
 * 约束：默认输出 UUIDv7，只有 UUID 策略支持 UUID 对象。
 * 行为：支持 UUIDv4、UUIDv7 和 ULID 三种生成方式。
 */
public final class IdGeneratorUtil {

    private static volatile IdGenerator defaultGenerator = UuidV7Generator.INSTANCE;
    private static volatile LongIdGenerator snowflakeGenerator = () -> {
        throw new IllegalStateException("Snowflake 生成器未初始化");
    };

    private IdGeneratorUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 用途：使用默认策略生成 ID。
     * 约束：默认策略全局共享。
     * 行为：返回当前默认生成器的输出。
     */
    public static String next() {
        return defaultGenerator.next();
    }

    /**
     * 用途：按指定策略生成 ID。
     * 约束：策略不能为空。
     * 行为：返回该策略对应格式的字符串。
     */
    public static String next(Strategy strategy) {
        return resolveGenerator(requireStrategy(strategy)).next();
    }

    /**
     * 用途：按指定策略生成 UUID 对象。
     * 约束：仅支持 UUID 策略。
     * 行为：对非 UUID 策略抛出中文异常。
     */
    public static UUID nextUuid(Strategy strategy) {
        IdGenerator generator = resolveGenerator(requireStrategy(strategy));
        if (generator instanceof UuidGenerator uuidGenerator) {
            return uuidGenerator.nextUuid();
        }
        throw new IllegalArgumentException(strategy + " 策略不支持 UUID 输出");
    }

    /**
     * 用途：按指定策略生成 long 数值。
     * 约束：仅支持数值型 ID 策略。
     * 行为：对非数值型策略抛出中文异常。
     */
    public static long nextLong(Strategy strategy) {
        IdGenerator generator = resolveGenerator(requireStrategy(strategy));
        if (generator instanceof LongIdGenerator longIdGenerator) {
            return longIdGenerator.nextLong();
        }
        throw new IllegalArgumentException(strategy + " 策略不支持 long 输出");
    }

    /**
     * 用途：切换全局默认策略。
     * 约束：策略不能为空。
     * 行为：后续 next() 调用立即生效。
     */
    public static void setDefaultStrategy(Strategy strategy) {
        defaultGenerator = resolveGenerator(requireStrategy(strategy));
    }

    /**
     * 用途：注入自定义默认生成器。
     * 约束：生成器不能为空。
     * 行为：后续 next() 调用委托给该生成器。
     */
    public static void setDefaultGenerator(IdGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("生成器不能为空");
        }
        defaultGenerator = generator;
    }

    /**
     * 用途：注册 Snowflake 生成器。
     * 约束：允许传入 null 以恢复未初始化状态。
     * 行为：供 Spring 自动配置在启动和关闭阶段挂载生成器。
     */
    public static void setSnowflakeGenerator(LongIdGenerator generator) {
        snowflakeGenerator = generator != null ? generator : () -> {
            throw new IllegalStateException("Snowflake 生成器未初始化");
        };
    }

    private static Strategy requireStrategy(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("生成策略不能为空");
        }
        return strategy;
    }

    private static IdGenerator resolveGenerator(Strategy strategy) {
        return switch (strategy) {
            case UUID_V4 -> UuidV4Generator.INSTANCE;
            case UUID_V7 -> UuidV7Generator.INSTANCE;
            case ULID -> UlidGenerator.INSTANCE;
            case SNOWFLAKE -> snowflakeGenerator;
        };
    }

    /**
     * 用途：声明可选的 ID 生成策略。
     * 约束：策略不能为空。
     * 行为：由调用方显式选择输出格式。
     */
    public enum Strategy {
        /**
         * 使用 UUIDv4。
         */
        UUID_V4,
        /**
         * 使用 UUIDv7。
         */
        UUID_V7,
        /**
         * 使用 ULID。
         */
        ULID,
        /**
         * 使用 Snowflake。
         */
        SNOWFLAKE
    }

    private enum UuidV4Generator implements UuidGenerator {
        INSTANCE;

        @Override
        public UUID nextUuid() {
            return UUID.randomUUID();
        }
    }

    /**
     * 用途：生成时间有序的 UUIDv7。
     * 约束：通过单个原子状态协调并发访问。
     * 行为：同毫秒内递增序列，序列耗尽后推进逻辑毫秒。
     */
    private enum UuidV7Generator implements UuidGenerator {
        INSTANCE;

        private static final int SEQUENCE_BITS = 12;
        private static final int UUID_TIME_SHIFT = 16;
        private static final int SEQUENCE_BOUND = 1 << SEQUENCE_BITS;
        private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1;
        private static final long UUID_TIMESTAMP_MASK = 0xFFFF_FFFF_FFFFL;
        private static final long UUID_VERSION = 0x7L;
        private static final long UUID_VARIANT = 0x8000_0000_0000_0000L;
        private static final long UUID_RANDOM_MASK = 0x3FFF_FFFF_FFFF_FFFFL;

        private final AtomicLong state = new AtomicLong(0L);

        @Override
        public UUID nextUuid() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            long nextState = nextState(System.currentTimeMillis());
            long logicalMillis = extractMillis(nextState);
            long sequence = extractSequence(nextState);

            return new UUID(
                composeMostSigBits(logicalMillis, sequence, random),
                composeLeastSigBits(random)
            );
        }

        private long nextState(long currentMillis) {
            while (true) {
                long previous = state.get();
                long next = calculateNextState(currentMillis, previous);
                if (state.compareAndSet(previous, next)) {
                    return next;
                }
            }
        }

        private long calculateNextState(long currentMillis, long previousState) {
            long previousMillis = extractMillis(previousState);
            long previousSequence = extractSequence(previousState);
            if (currentMillis > previousMillis) {
                return currentMillis << SEQUENCE_BITS;
            }

            long nextSequence = (previousSequence + 1) & SEQUENCE_MASK;
            if (nextSequence == 0L) {
                return (previousMillis + 1) << SEQUENCE_BITS;
            }
            return (previousMillis << SEQUENCE_BITS) | nextSequence;
        }

        private long extractMillis(long stateValue) {
            return stateValue >>> SEQUENCE_BITS;
        }

        private long extractSequence(long stateValue) {
            return stateValue & SEQUENCE_MASK;
        }

        private long composeMostSigBits(long logicalMillis, long sequence, ThreadLocalRandom random) {
            long mixedLowBits = (sequence ^ random.nextInt(SEQUENCE_BOUND)) & SEQUENCE_MASK;
            return ((logicalMillis & UUID_TIMESTAMP_MASK) << UUID_TIME_SHIFT)
                | (UUID_VERSION << SEQUENCE_BITS)
                | mixedLowBits;
        }

        private long composeLeastSigBits(ThreadLocalRandom random) {
            return UUID_VARIANT | (random.nextLong() & UUID_RANDOM_MASK);
        }
    }

    /**
     * 用途：生成 26 位 ULID 字符串。
     * 约束：时间部分保持毫秒级编码，随机部分使用线程本地随机源。
     * 行为：返回符合 Crockford Base32 字符集的时间有序字符串。
     */
    private enum UlidGenerator implements IdGenerator {
        INSTANCE;

        private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

        @Override
        public String next() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            char[] chars = new char[26];
            encodeTime(System.currentTimeMillis(), chars);
            encodeRandom(random.nextLong(), random.nextInt(1 << 16), chars);
            return new String(chars);
        }

        private void encodeTime(long currentMillis, char[] chars) {
            for (int index = 9; index >= 0; index--) {
                chars[index] = ENCODING[(int) (currentMillis & 0x1F)];
                currentMillis >>>= 5;
            }
        }

        private void encodeRandom(long randomHigh, int randomLow, char[] chars) {
            int b0 = (int) (randomHigh >>> 56) & 0xFF;
            int b1 = (int) (randomHigh >>> 48) & 0xFF;
            int b2 = (int) (randomHigh >>> 40) & 0xFF;
            int b3 = (int) (randomHigh >>> 32) & 0xFF;
            int b4 = (int) (randomHigh >>> 24) & 0xFF;
            int b5 = (int) (randomHigh >>> 16) & 0xFF;
            int b6 = (int) (randomHigh >>> 8) & 0xFF;
            int b7 = (int) randomHigh & 0xFF;
            int b8 = (randomLow >>> 8) & 0xFF;
            int b9 = randomLow & 0xFF;

            chars[10] = ENCODING[b0 >>> 3];
            chars[11] = ENCODING[((b0 & 0x07) << 2) | (b1 >>> 6)];
            chars[12] = ENCODING[(b1 >>> 1) & 0x1F];
            chars[13] = ENCODING[((b1 & 0x01) << 4) | (b2 >>> 4)];
            chars[14] = ENCODING[((b2 & 0x0F) << 1) | (b3 >>> 7)];
            chars[15] = ENCODING[(b3 >>> 2) & 0x1F];
            chars[16] = ENCODING[((b3 & 0x03) << 3) | (b4 >>> 5)];
            chars[17] = ENCODING[b4 & 0x1F];
            chars[18] = ENCODING[b5 >>> 3];
            chars[19] = ENCODING[((b5 & 0x07) << 2) | (b6 >>> 6)];
            chars[20] = ENCODING[(b6 >>> 1) & 0x1F];
            chars[21] = ENCODING[((b6 & 0x01) << 4) | (b7 >>> 4)];
            chars[22] = ENCODING[((b7 & 0x0F) << 1) | (b8 >>> 7)];
            chars[23] = ENCODING[(b8 >>> 2) & 0x1F];
            chars[24] = ENCODING[((b8 & 0x03) << 3) | (b9 >>> 5)];
            chars[25] = ENCODING[b9 & 0x1F];
        }
    }

    /**
     * 用途：定义统一的字符串 ID 生成入口。
     * 约束：返回值不能为空。
     * 行为：每次调用生成一个新 ID。
     */
    @FunctionalInterface
    public interface IdGenerator {
        String next();
    }

    /**
     * 用途：定义可直接输出 UUID 对象的生成器。
     * 约束：仅适用于 UUID 策略。
     * 行为：同时支持字符串输出。
     */
    @FunctionalInterface
    public interface UuidGenerator extends IdGenerator {
        UUID nextUuid();

        @Override
        default String next() {
            return nextUuid().toString();
        }
    }

    /**
     * 用途：定义可直接输出 long 数值的生成器。
     * 约束：仅适用于数值型 ID 策略。
     * 行为：同时支持字符串输出。
     */
    @FunctionalInterface
    public interface LongIdGenerator extends IdGenerator {
        long nextLong();

        @Override
        default String next() {
            return Long.toString(nextLong());
        }
    }
}
