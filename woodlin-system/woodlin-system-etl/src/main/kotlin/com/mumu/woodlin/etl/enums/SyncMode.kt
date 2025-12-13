package com.mumu.woodlin.etl.enums

/**
 * 数据同步模式枚举
 *
 * 定义ETL任务的数据同步模式，支持全量和增量两种方式
 *
 * @author mumu
 * @since 2025-01-01
 */
enum class SyncMode(
    /** 模式编码 */
    val code: String,
    /** 模式名称 */
    val displayName: String
) {
    /**
     * 全量同步
     *
     * 每次执行时同步源表的所有数据，适用于：
     * - 数据量较小的表
     * - 不频繁变更的数据
     * - 需要完整快照的场景
     */
    FULL("FULL", "全量同步"),

    /**
     * 增量同步
     *
     * 基于增量字段（如时间戳）只同步变更的数据，适用于：
     * - 数据量大的表
     * - 频繁变更的数据
     * - 需要准实时同步的场景
     *
     * 注意：需要配置incrementalColumn字段
     */
    INCREMENTAL("INCREMENTAL", "增量同步");

    companion object {
        /**
         * 根据模式编码获取枚举值
         *
         * @param code 模式编码
         * @return 对应的枚举值，如果不存在则返回FULL
         */
        fun fromCode(code: String): SyncMode {
            return entries.find { it.code == code } ?: FULL
        }
    }
}
