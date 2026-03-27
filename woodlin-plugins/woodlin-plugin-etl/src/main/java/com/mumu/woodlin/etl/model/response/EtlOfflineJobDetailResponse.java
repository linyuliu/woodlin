package com.mumu.woodlin.etl.model.response;

import com.mumu.woodlin.etl.model.EtlOfflineFieldRule;
import com.mumu.woodlin.etl.model.EtlOfflineRuntimeConfig;
import com.mumu.woodlin.etl.model.EtlOfflineTableMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ETL 离线任务详情。
 *
 * <p>用于前端向导直接回填基础信息、表映射、字段规则和运行配置。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务详情")
public class EtlOfflineJobDetailResponse {

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
     * 任务分组。
     */
    @Schema(description = "任务分组")
    private String jobGroup;

    /**
     * 任务描述。
     */
    @Schema(description = "任务描述")
    private String jobDescription;

    /**
     * 源数据源。
     */
    @Schema(description = "源数据源")
    private String sourceDatasource;

    /**
     * 目标数据源。
     */
    @Schema(description = "目标数据源")
    private String targetDatasource;

    /**
     * 任务状态。
     */
    @Schema(description = "任务状态")
    private String status;

    /**
     * 是否并发执行。
     */
    @Schema(description = "是否并发执行")
    private String concurrent;

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
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 运行配置。
     */
    @Schema(description = "运行配置")
    private EtlOfflineRuntimeConfig runtimeConfig;

    /**
     * 表映射。
     */
    @Schema(description = "表映射")
    private EtlOfflineTableMapping tableMapping;

    /**
     * 字段规则列表。
     */
    @Schema(description = "字段规则列表")
    private List<EtlOfflineFieldRule> fieldRules = new ArrayList<>();
}
