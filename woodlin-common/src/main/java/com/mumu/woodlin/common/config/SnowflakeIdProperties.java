package com.mumu.woodlin.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Snowflake ID 配置属性。
 */
@Data
@ConfigurationProperties(prefix = "woodlin.id.snowflake")
public class SnowflakeIdProperties {

    /**
     * 2025-01-01 00:00:00 UTC。
     */
    public static final long DEFAULT_EPOCH = 1735689600000L;
    private static final String DEFAULT_NAMESPACE_PREFIX = "woodlin-";

    /**
     * 是否启用 Snowflake 能力。
     */
    private Boolean enabled = true;

    /**
     * Snowflake 起始时间戳。
     */
    private Long epoch = DEFAULT_EPOCH;

    /**
     * 动态分配槽位数量。
     */
    @Min(1)
    @Max(256)
    private Integer slotCount = 256;

    /**
     * 动态租约 TTL。
     */
    private Duration leaseTtl = Duration.ofSeconds(15);

    /**
     * 心跳续约间隔。
     */
    private Duration heartbeatInterval;

    /**
     * 时钟回拨容忍时间，单位毫秒。
     */
    @Min(0)
    private Long rollbackToleranceMillis = 5000L;

    /**
     * 节点分配命名空间。
     */
    private String namespace;

    /**
     * Redis 动态分配配置。
     */
    private Redis redis = new Redis();

    /**
     * Nacos 动态分配配置。
     */
    private Nacos nacos = new Nacos();

    /**
     * 手工节点配置。
     */
    private Manual manual = new Manual();

    public Duration resolveHeartbeatInterval() {
        if (heartbeatInterval != null && !heartbeatInterval.isNegative() && !heartbeatInterval.isZero()) {
            return heartbeatInterval;
        }
        return leaseTtl.dividedBy(3);
    }

    public String resolveNamespace(String activeProfile) {
        if (StringUtils.hasText(namespace)) {
            return namespace;
        }
        if (StringUtils.hasText(activeProfile)) {
            return DEFAULT_NAMESPACE_PREFIX + activeProfile;
        }
        return DEFAULT_NAMESPACE_PREFIX + "default";
    }

    @Data
    public static class Redis {

        /**
         * Redis 租约键前缀。
         */
        private String keyPrefix = "woodlin:id:snowflake";
    }

    @Data
    public static class Nacos {

        /**
         * 是否启用 Nacos 动态分配。
         */
        private Boolean enabled = true;

        /**
         * Nacos 锁键前缀。
         */
        private String lockPrefix = "woodlin:id:snowflake:lock";

        /**
         * Nacos 服务器地址。
         */
        private String serverAddr;

        /**
         * Nacos 命名空间。
         */
        private String namespace;

        /**
         * Nacos 用户名。
         */
        private String username;

        /**
         * Nacos 密码。
         */
        private String password;
    }

    @Data
    public static class Manual {

        /**
         * 手工指定的 workerId。
         */
        @Min(0)
        @Max(31)
        private Integer workerId;

        /**
         * 手工指定的 datacenterId。
         */
        @Min(0)
        @Max(31)
        private Integer datacenterId;
    }
}
