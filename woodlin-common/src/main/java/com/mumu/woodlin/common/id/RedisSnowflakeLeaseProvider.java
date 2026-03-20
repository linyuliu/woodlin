package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的动态 Snowflake 节点提供者。
 */
@Slf4j
public class RedisSnowflakeLeaseProvider implements SnowflakeLeaseProvider {

    public static final int ORDER = 100;
    private static final String SOURCE = "REDIS";
    private static final String RENEW_SCRIPT = """
        if redis.call('get', KEYS[1]) == ARGV[1] then
            redis.call('pexpire', KEYS[1], ARGV[2])
            return 1
        end
        return 0
        """;
    private static final String RELEASE_SCRIPT = """
        if redis.call('get', KEYS[1]) == ARGV[1] then
            redis.call('del', KEYS[1])
            return 1
        end
        return 0
        """;

    private final RedissonClient redissonClient;
    private final SnowflakeIdProperties properties;
    private final String namespace;

    public RedisSnowflakeLeaseProvider(
        RedissonClient redissonClient,
        SnowflakeIdProperties properties,
        String namespace
    ) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.namespace = namespace;
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public Optional<SnowflakeLease> acquire() {
        long leaseMillis = properties.getLeaseTtl().toMillis();
        String ownerToken = UUID.randomUUID().toString();
        for (int slot = 0; slot < properties.getSlotCount(); slot++) {
            String key = buildLeaseKey(slot);
            RBucket<String> bucket = redissonClient.getBucket(key);
            if (bucket.trySet(ownerToken, leaseMillis, TimeUnit.MILLISECONDS)) {
                SnowflakeNodeAssignment assignment =
                    new SnowflakeNodeAssignment(SOURCE, slot, slot & 31, (slot >>> 5) & 31);
                return Optional.of(new RedisLease(assignment, key, ownerToken));
            }
        }
        return Optional.empty();
    }

    private String buildLeaseKey(int slot) {
        return properties.getRedis().getKeyPrefix() + ":" + namespace + ":slot:" + slot;
    }

    private boolean renew(String key, String ownerToken, Duration leaseTtl) {
        return Boolean.TRUE.equals(
            redissonClient.getScript().eval(
                RScript.Mode.READ_WRITE,
                RENEW_SCRIPT,
                RScript.ReturnType.BOOLEAN,
                List.of(key),
                ownerToken,
                Long.toString(leaseTtl.toMillis())
            )
        );
    }

    private void release(String key, String ownerToken) {
        redissonClient.getScript().eval(
            RScript.Mode.READ_WRITE,
            RELEASE_SCRIPT,
            RScript.ReturnType.BOOLEAN,
            List.of(key),
            ownerToken
        );
    }

    private final class RedisLease implements SnowflakeLease {

        private final SnowflakeNodeAssignment assignment;
        private final String key;
        private final String ownerToken;

        private RedisLease(SnowflakeNodeAssignment assignment, String key, String ownerToken) {
            this.assignment = assignment;
            this.key = key;
            this.ownerToken = ownerToken;
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
            boolean renewed = RedisSnowflakeLeaseProvider.this.renew(key, ownerToken, leaseTtl);
            if (!renewed) {
                log.warn("Redis Snowflake 租约续约失败: key={}", key);
            }
            return renewed;
        }

        @Override
        public void release() {
            RedisSnowflakeLeaseProvider.this.release(key, ownerToken);
        }
    }
}
