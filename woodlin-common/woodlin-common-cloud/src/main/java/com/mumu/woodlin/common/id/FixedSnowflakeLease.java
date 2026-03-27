package com.mumu.woodlin.common.id;

/**
 * 固定节点租约。
 */
public record FixedSnowflakeLease(SnowflakeNodeAssignment assignment) implements SnowflakeLease {

}
