package com.mumu.woodlin.common.id;

import com.mumu.woodlin.common.config.SnowflakeIdProperties;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

/**
 * 手工 Snowflake 节点提供者。
 */
public class ManualSnowflakeLeaseProvider implements SnowflakeLeaseProvider {

    public static final int ORDER = 300;
    private static final String SOURCE = "MANUAL";

    private final SnowflakeIdProperties properties;

    public ManualSnowflakeLeaseProvider(SnowflakeIdProperties properties) {
        this.properties = properties;
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
        SnowflakeIdProperties.Manual manual = properties.getManual();
        if (manual == null) {
            return Optional.empty();
        }
        if (ObjectUtils.isEmpty(manual.getWorkerId()) || ObjectUtils.isEmpty(manual.getDatacenterId())) {
            return Optional.empty();
        }

        int workerId = manual.getWorkerId();
        int datacenterId = manual.getDatacenterId();
        int slot = (datacenterId << 5) | workerId;
        if (slot >= properties.getSlotCount()) {
            throw new IllegalStateException(
                "手工配置的 datacenterId 与 workerId 超出槽位范围: slot=" + slot
            );
        }

        return Optional.of(
            new FixedSnowflakeLease(new SnowflakeNodeAssignment(SOURCE, slot, workerId, datacenterId))
        );
    }
}
