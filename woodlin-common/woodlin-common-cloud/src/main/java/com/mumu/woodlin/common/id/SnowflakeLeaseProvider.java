package com.mumu.woodlin.common.id;

import java.util.Optional;

/**
 * Snowflake 节点租约提供者。
 */
public interface SnowflakeLeaseProvider {

    /**
     * 提供者优先级，值越小优先级越高。
     *
     * @return 优先级
     */
    int order();

    /**
     * 提供者名称。
     *
     * @return 名称
     */
    String source();

    /**
     * 尝试申请节点租约。
     *
     * @return 申请结果
     */
    Optional<SnowflakeLease> acquire();
}
