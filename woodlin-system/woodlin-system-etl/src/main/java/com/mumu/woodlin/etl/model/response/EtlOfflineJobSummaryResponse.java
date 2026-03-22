package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ETL 离线任务列表摘要。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务列表摘要")
public class EtlOfflineJobSummaryResponse {

    /**
     * 任务ID。
     */
    @Schema(description = "任务ID")
    private Long jobId;

    /**
     * 任务名称。
     */
    @Schema(description = "任务名称")
    private String jobName;

    /**
     * 源数据源。
     */
    @Schema(description = "源数据源")
    private String sourceDatasource;

    /**
     * 源Schema。
     */
    @Schema(description = "源Schema")
    private String sourceSchema;

    /**
     * 源表。
     */
    @Schema(description = "源表")
    private String sourceTable;

    /**
     * 目标数据源。
     */
    @Schema(description = "目标数据源")
    private String targetDatasource;

    /**
     * 目标Schema。
     */
    @Schema(description = "目标Schema")
    private String targetSchema;

    /**
     * 目标表。
     */
    @Schema(description = "目标表")
    private String targetTable;

    /**
     * 同步模式。
     */
    @Schema(description = "同步模式")
    private String syncMode;

    /**
     * 状态。
     */
    @Schema(description = "状态")
    private String status;

    /**
     * Cron 表达式。
     */
    @Schema(description = "Cron表达式")
    private String cronExpression;

    /**
     * 最近执行时间。
     */
    @Schema(description = "最近执行时间")
    private LocalDateTime lastExecuteTime;

    /**
     * 最近执行状态。
     */
    @Schema(description = "最近执行状态")
    private String lastExecuteStatus;

    /**
     * 字段规则数量。
     */
    @Schema(description = "字段规则数量")
    private Long fieldRuleCount;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
