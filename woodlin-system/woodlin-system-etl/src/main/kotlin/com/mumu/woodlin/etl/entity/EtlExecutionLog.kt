package com.mumu.woodlin.etl.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.mumu.woodlin.common.entity.BaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * ETL执行历史实体类
 *
 * 记录每次ETL任务执行的详细信息，包括执行状态、数据统计、错误信息等。
 * 用于任务监控、性能分析和问题排查。
 *
 * ## 记录内容
 * - 执行时间和耗时统计
 * - 数据提取、转换、加载的记录数
 * - 执行状态和错误信息
 * - 详细的执行过程数据
 *
 * @author mumu
 * @since 2025-01-01
 */
@TableName("sys_etl_execution_log")
@Schema(description = "ETL执行历史")
data class EtlExecutionLog(
    /** 执行记录ID，系统自动生成 */
    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    @Schema(description = "执行记录ID")
    var logId: Long? = null,

    /** 关联的任务ID */
    @TableField("job_id")
    @Schema(description = "任务ID")
    var jobId: Long? = null,

    /** 任务名称快照，避免任务删除后无法追溯 */
    @TableField("job_name")
    @Schema(description = "任务名称")
    var jobName: String? = null,

    /**
     * 执行状态
     *
     * - RUNNING: 任务正在执行
     * - SUCCESS: 任务成功完成
     * - FAILED: 任务执行失败
     * - PARTIAL_SUCCESS: 部分成功（部分数据同步成功）
     */
    @TableField("execution_status")
    @Schema(description = "执行状态", allowableValues = ["RUNNING", "SUCCESS", "FAILED", "PARTIAL_SUCCESS"])
    var executionStatus: String? = null,

    /** 任务开始执行时间 */
    @TableField("start_time")
    @Schema(description = "开始时间")
    var startTime: LocalDateTime? = null,

    /** 任务结束时间 */
    @TableField("end_time")
    @Schema(description = "结束时间")
    var endTime: LocalDateTime? = null,

    /**
     * 执行耗时（毫秒）
     *
     * 从开始到结束的总时长，用于性能分析和优化
     */
    @TableField("duration")
    @Schema(description = "执行耗时(毫秒)")
    var duration: Long? = null,

    /**
     * 提取记录数
     *
     * 从源数据库成功提取的记录数
     */
    @TableField("extracted_rows")
    @Schema(description = "提取记录数")
    var extractedRows: Long? = null,

    /**
     * 转换记录数
     *
     * 经过数据转换处理的记录数
     */
    @TableField("transformed_rows")
    @Schema(description = "转换记录数")
    var transformedRows: Long? = null,

    /**
     * 加载记录数
     *
     * 成功写入目标数据库的记录数
     */
    @TableField("loaded_rows")
    @Schema(description = "加载记录数")
    var loadedRows: Long? = null,

    /**
     * 失败记录数
     *
     * 处理失败的记录数，通常等于 extractedRows - loadedRows
     */
    @TableField("failed_rows")
    @Schema(description = "失败记录数")
    var failedRows: Long? = null,

    /**
     * 错误信息
     *
     * 任务失败时的错误描述和异常堆栈信息
     */
    @TableField("error_message")
    @Schema(description = "错误信息")
    var errorMessage: String? = null,

    /**
     * 执行详情（JSON格式）
     *
     * 记录详细的执行过程信息，例如：
     * - 每个批次的处理情况
     * - 数据采样信息
     * - 性能指标
     */
    @TableField("execution_detail")
    @Schema(description = "执行详情(JSON)")
    var executionDetail: String? = null,

    /** 租户ID，用于多租户数据隔离 */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    var tenantId: String? = null
) : BaseEntity() {
    companion object {
        private const val serialVersionUID = 1L
    }
}
