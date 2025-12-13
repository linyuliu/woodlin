package com.mumu.woodlin.etl.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.mumu.woodlin.common.entity.BaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * ETL任务实体类
 *
 * 用于定义数据抽取、转换、加载（ETL）任务的配置信息。
 * 支持全量和增量两种同步模式，可配置Cron表达式进行定时调度。
 *
 * ## 主要功能
 * - 配置源和目标数据源及表信息
 * - 支持自定义SQL查询和过滤条件
 * - 支持字段映射和数据转换规则
 * - 支持批量处理和错误重试
 * - 集成任务调度系统
 *
 * @author mumu
 * @since 2025-01-01
 */
@TableName("sys_etl_job")
@Schema(description = "ETL任务配置")
data class EtlJob(
    /** 任务ID，系统自动生成 */
    @TableId(value = "job_id", type = IdType.ASSIGN_ID)
    @Schema(description = "任务ID")
    var jobId: Long? = null,

    /** 任务名称，必填，建议使用描述性名称 */
    @TableField("job_name")
    @Schema(description = "任务名称", example = "用户数据同步")
    var jobName: String? = null,

    /** 任务组名，用于任务分类管理 */
    @TableField("job_group")
    @Schema(description = "任务组名", example = "DATA_SYNC")
    var jobGroup: String? = null,

    /** 任务描述，详细说明任务用途 */
    @TableField("job_description")
    @Schema(description = "任务描述")
    var jobDescription: String? = null,

    /** 源数据源名称，对应application.yml中配置的数据源key */
    @TableField("source_datasource")
    @Schema(description = "源数据源名称", example = "source_mysql")
    var sourceDatasource: String? = null,

    /** 源表名，与sourceQuery二选一 */
    @TableField("source_table")
    @Schema(description = "源表名", example = "users")
    var sourceTable: String? = null,

    /** 源Schema名，PostgreSQL等数据库需要指定 */
    @TableField("source_schema")
    @Schema(description = "源Schema名", example = "public")
    var sourceSchema: String? = null,

    /**
     * 源查询SQL，可选
     *
     * 如果不为空则使用此SQL查询数据，否则使用sourceTable。
     * 适用于需要复杂查询、多表关联、数据转换的场景。
     */
    @TableField("source_query")
    @Schema(description = "源查询SQL")
    var sourceQuery: String? = null,

    /** 目标数据源名称，对应application.yml中配置的数据源key */
    @TableField("target_datasource")
    @Schema(description = "目标数据源名称", example = "target_mysql")
    var targetDatasource: String? = null,

    /** 目标表名，必填 */
    @TableField("target_table")
    @Schema(description = "目标表名", example = "users_backup")
    var targetTable: String? = null,

    /** 目标Schema名，PostgreSQL等数据库需要指定 */
    @TableField("target_schema")
    @Schema(description = "目标Schema名", example = "public")
    var targetSchema: String? = null,

    /**
     * 同步模式
     *
     * - FULL: 全量同步，每次同步所有数据
     * - INCREMENTAL: 增量同步，基于incrementalColumn只同步变更数据
     */
    @TableField("sync_mode")
    @Schema(description = "同步模式", example = "FULL", allowableValues = ["FULL", "INCREMENTAL"])
    var syncMode: String? = null,

    /**
     * 增量字段名
     *
     * 增量同步时必填，通常使用时间戳字段如update_time、created_at等
     */
    @TableField("incremental_column")
    @Schema(description = "增量字段", example = "update_time")
    var incrementalColumn: String? = null,

    /**
     * 字段映射配置（JSON格式）
     *
     * 定义源字段到目标字段的映射关系，例如：
     * ```json
     * {
     *   "source_user_id": "target_uid",
     *   "source_user_name": "target_name"
     * }
     * ```
     */
    @TableField("column_mapping")
    @Schema(description = "字段映射配置(JSON)")
    var columnMapping: String? = null,

    /**
     * 数据转换规则（JSON格式）
     *
     * 定义数据转换逻辑，例如数据脱敏、类型转换等
     */
    @TableField("transform_rules")
    @Schema(description = "数据转换规则(JSON)")
    var transformRules: String? = null,

    /**
     * 过滤条件（WHERE子句）
     *
     * 附加的过滤条件，不需要包含WHERE关键字
     * 例如: "status = '1' AND created_at > '2024-01-01'"
     */
    @TableField("filter_condition")
    @Schema(description = "过滤条件", example = "status = '1'")
    var filterCondition: String? = null,

    /**
     * 批处理大小
     *
     * 每批次处理的记录数，默认1000。
     * 需要根据数据量和网络情况调整，建议范围：500-10000
     */
    @TableField("batch_size")
    @Schema(description = "批处理大小", example = "1000")
    var batchSize: Int? = null,

    /**
     * Cron执行表达式
     *
     * 定义任务执行时间，例如：
     * - "0 0 2 * * ?" 每天凌晨2点
     * - "0 */30 * * * ?" 每30分钟
     */
    @TableField("cron_expression")
    @Schema(description = "cron执行表达式", example = "0 0 2 * * ?")
    var cronExpression: String? = null,

    /** 任务状态：1-启用，0-禁用 */
    @TableField("status")
    @Schema(description = "任务状态", example = "1", allowableValues = ["0", "1"])
    var status: String? = null,

    /** 是否并发执行：1-允许，0-禁止 */
    @TableField("concurrent")
    @Schema(description = "是否并发执行", example = "0", allowableValues = ["0", "1"])
    var concurrent: String? = null,

    /** 失败重试次数，默认3次 */
    @TableField("retry_count")
    @Schema(description = "失败重试次数", example = "3")
    var retryCount: Int? = null,

    /** 重试间隔（秒），默认60秒 */
    @TableField("retry_interval")
    @Schema(description = "重试间隔(秒)", example = "60")
    var retryInterval: Int? = null,

    /** 下次执行时间，由调度系统自动计算 */
    @TableField("next_execute_time")
    @Schema(description = "下次执行时间")
    var nextExecuteTime: LocalDateTime? = null,

    /** 上次执行时间，每次执行后自动更新 */
    @TableField("last_execute_time")
    @Schema(description = "上次执行时间")
    var lastExecuteTime: LocalDateTime? = null,

    /** 上次执行状态，记录最近一次执行结果 */
    @TableField("last_execute_status")
    @Schema(description = "上次执行状态")
    var lastExecuteStatus: String? = null,

    /** 租户ID，用于多租户数据隔离 */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    var tenantId: String? = null,

    /** 备注信息 */
    @TableField("remark")
    @Schema(description = "备注")
    var remark: String? = null
) : BaseEntity() {
    companion object {
        private const val serialVersionUID = 1L
    }
}
