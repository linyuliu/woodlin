package com.mumu.woodlin.etl.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * ETL 离线任务运行配置。
 *
 * <p>该对象承载任务级调度与执行策略，不直接表达表级字段映射信息。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务运行配置")
public class EtlOfflineRuntimeConfig {

    /**
     * 任务类型。
     */
    @Schema(description = "任务类型", example = "INCREMENTAL_SYNC")
    private String taskType;

    /**
     * 任务规格。
     */
    @Schema(description = "任务规格", example = "BALANCED")
    private String profile;

    /**
     * 同步模式。
     */
    @Schema(description = "同步模式", example = "FULL")
    private String syncMode;

    /**
     * 是否全量初始化。
     */
    @Schema(description = "是否全量初始化")
    private Boolean fullInit;

    /**
     * 是否同步 DDL。
     */
    @Schema(description = "是否同步DDL")
    private Boolean syncDdl;

    /**
     * 数据校验模式。
     */
    @Schema(description = "数据校验模式", example = "NONE")
    private String validationMode;

    /**
     * 全量前是否清空目标表。
     */
    @Schema(description = "全量前是否清空目标表")
    private Boolean truncateTarget;

    /**
     * 是否重建目标表。
     */
    @Schema(description = "是否重建目标表")
    private Boolean recreateTarget;

    /**
     * 结构同步模式。
     */
    @Schema(description = "结构同步模式", example = "AUTO_ADD_COLUMNS")
    private String schemaSyncMode;

    /**
     * 是否启用参数模板。
     */
    @Schema(description = "是否启用参数模板")
    private Boolean useParamTemplate;

    /**
     * 批处理大小。
     */
    @Min(value = 100, message = "批处理大小不能小于100")
    @Max(value = 200000, message = "批处理大小不能大于200000")
    @Schema(description = "批处理大小", example = "1000")
    private Integer batchSize;

    /**
     * 失败重试次数。
     */
    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 20, message = "重试次数不能大于20")
    @Schema(description = "失败重试次数", example = "3")
    private Integer retryCount;

    /**
     * 重试间隔（秒）。
     */
    @Min(value = 1, message = "重试间隔不能小于1秒")
    @Max(value = 3600, message = "重试间隔不能大于3600秒")
    @Schema(description = "重试间隔（秒）", example = "60")
    private Integer retryInterval;

    /**
     * 是否允许并发执行。
     */
    @Schema(description = "是否允许并发执行")
    private Boolean allowConcurrent;

    /**
     * 是否自动启动。
     */
    @Schema(description = "是否自动启动")
    private Boolean autoStart;

    /**
     * Cron 表达式。
     */
    @Schema(description = "Cron表达式")
    private String cronExpression;
}
