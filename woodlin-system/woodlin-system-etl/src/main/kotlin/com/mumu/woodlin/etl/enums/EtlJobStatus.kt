package com.mumu.woodlin.etl.enums

/**
 * ETL任务状态枚举
 *
 * 定义ETL任务的各种状态，用于控制任务的启用和禁用
 *
 * @author mumu
 * @since 2025-01-01
 */
enum class EtlJobStatus(
    /** 状态编码 */
    val code: String,
    /** 状态名称 */
    val displayName: String
) {
    /** 启用状态 - 任务将按照cron表达式自动执行 */
    ENABLED("1", "启用"),

    /** 禁用状态 - 任务不会自动执行，但可以手动触发 */
    DISABLED("0", "禁用");

    companion object {
        /**
         * 根据状态编码获取枚举值
         *
         * @param code 状态编码
         * @return 对应的枚举值，如果不存在则返回DISABLED
         */
        fun fromCode(code: String): EtlJobStatus {
            return entries.find { it.code == code } ?: DISABLED
        }
    }
}
