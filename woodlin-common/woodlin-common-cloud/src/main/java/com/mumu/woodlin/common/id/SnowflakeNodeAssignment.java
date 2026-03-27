package com.mumu.woodlin.common.id;

/**
 * Snowflake 节点分配结果。
 *
 * @param source       节点来源
 * @param slot         槽位编号
 * @param workerId     workerId
 * @param datacenterId datacenterId
 */
public record SnowflakeNodeAssignment(String source, int slot, int workerId, int datacenterId) {
}
