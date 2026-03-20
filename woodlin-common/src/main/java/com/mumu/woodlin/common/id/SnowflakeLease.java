package com.mumu.woodlin.common.id;

import java.time.Duration;

/**
 * Snowflake 节点租约。
 */
public interface SnowflakeLease {

    /**
     * 返回当前租约对应的节点分配。
     *
     * @return 节点分配
     */
    SnowflakeNodeAssignment assignment();

    /**
     * 是否为动态租约。
     *
     * @return 动态租约返回 true
     */
    default boolean isDynamic() {
        return false;
    }

    /**
     * 续约当前节点租约。
     *
     * @param leaseTtl 目标 TTL
     * @return 续约是否成功
     */
    default boolean renew(Duration leaseTtl) {
        return true;
    }

    /**
     * 主动释放节点租约。
     */
    default void release() {
    }
}
