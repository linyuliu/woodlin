package com.mumu.woodlin.etl.model.request;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 离线ETL任务创建/更新请求。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线ETL任务创建请求")
public class EtlOfflineJobCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jobName;

    @Schema(description = "任务分组", example = "OFFLINE_SYNC")
    private String jobGroup;

    @Schema(description = "任务描述")
    private String jobDescription;

    @NotBlank(message = "源数据源不能为空")
    @Schema(description = "源数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceDatasource;

    @Schema(description = "源Schema")
    private String sourceSchema;

    @Schema(description = "源表")
    private String sourceTable;

    @Schema(description = "源查询SQL")
    private String sourceQuery;

    @NotBlank(message = "目标数据源不能为空")
    @Schema(description = "目标数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetDatasource;

    @Schema(description = "目标Schema")
    private String targetSchema;

    @NotBlank(message = "目标表不能为空")
    @Schema(description = "目标表", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetTable;

    @NotBlank(message = "同步模式不能为空")
    @Schema(description = "同步模式：FULL/INCREMENTAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String syncMode;

    @Schema(description = "增量字段")
    private String incrementalColumn;

    @Schema(description = "过滤条件")
    private String filterCondition;

    @Schema(description = "字段映射JSON")
    private String columnMapping;

    @Schema(description = "转换规则JSON")
    private String transformRules;

    @Schema(description = "Cron表达式（为空则使用默认值）")
    private String cronExpression;

    @Min(value = 100, message = "批处理大小不能小于100")
    @Max(value = 200000, message = "批处理大小不能大于200000")
    @Schema(description = "批处理大小", example = "1000")
    private Integer batchSize;

    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 20, message = "重试次数不能大于20")
    @Schema(description = "失败重试次数", example = "3")
    private Integer retryCount;

    @Min(value = 1, message = "重试间隔不能小于1秒")
    @Max(value = 3600, message = "重试间隔不能大于3600秒")
    @Schema(description = "重试间隔（秒）", example = "60")
    private Integer retryInterval;

    @NotNull(message = "自动启动标志不能为空")
    @Schema(description = "是否自动启动（true=启用任务）", example = "false")
    private Boolean autoStart = Boolean.FALSE;

    @NotNull(message = "并发配置不能为空")
    @Schema(description = "是否允许并发执行", example = "false")
    private Boolean allowConcurrent = Boolean.FALSE;

    @Schema(description = "备注")
    private String remark;

    /**
     * 校验源端输入。
     *
     * @return 是否合法
     */
    public boolean hasSourceInput() {
        return StrUtil.isNotBlank(sourceTable) || StrUtil.isNotBlank(sourceQuery);
    }
}
