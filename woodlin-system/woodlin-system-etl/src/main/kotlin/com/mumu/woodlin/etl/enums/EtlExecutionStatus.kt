package com.mumu.woodlin.etl.enums

/**
 * ETL执行状态枚举
 *
 * 定义ETL任务执行过程中的各种状态，用于跟踪任务执行情况
 *
 * @author mumu
 * @since 2025-01-01
 */
enum class EtlExecutionStatus(
    /** 状态编码 */
    val code: String,
    /** 状态名称 */
    val displayName: String
) {
    /** 运行中 - 任务正在执行数据同步 */
    RUNNING("RUNNING", "运行中"),

    /** 成功 - 任务成功完成所有数据同步 */
    SUCCESS("SUCCESS", "成功"),

    /** 失败 - 任务执行过程中发生错误 */
    FAILED("FAILED", "失败"),

    /** 部分成功 - 部分数据同步成功，部分失败 */
    PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功");

    companion object {
        /**
         * 根据状态编码获取枚举值
         *
         * @param code 状态编码
         * @return 对应的枚举值，如果不存在则返回FAILED
         */
        fun fromCode(code: String): EtlExecutionStatus {
            return entries.find { it.code == code } ?: FAILED
        }
    }
}
