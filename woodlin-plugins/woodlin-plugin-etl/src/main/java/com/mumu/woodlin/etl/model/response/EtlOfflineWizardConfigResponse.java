package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线ETL向导初始化配置。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线ETL向导初始化配置")
public class EtlOfflineWizardConfigResponse {

    @Schema(description = "数据源类型分组")
    private List<EtlDatasourceTypeGroup> datasourceTypeGroups;

    @Schema(description = "默认同步模式", example = "FULL")
    private String defaultSyncMode;

    @Schema(description = "默认批处理大小", example = "1000")
    private Integer defaultBatchSize;

    @Schema(description = "默认重试次数", example = "3")
    private Integer defaultRetryCount;

    @Schema(description = "默认重试间隔秒", example = "60")
    private Integer defaultRetryInterval;

    @Schema(description = "默认任务分组", example = "OFFLINE_SYNC")
    private String defaultJobGroup;

    @Schema(description = "默认结构同步模式", example = "AUTO_ADD_COLUMNS")
    private String defaultSchemaSyncMode;

    @Schema(description = "支持的同步模式")
    private List<String> supportedSyncModes = new ArrayList<>();

    @Schema(description = "支持的数据校验模式")
    private List<String> supportedValidationModes = new ArrayList<>();

    @Schema(description = "支持的结构同步模式")
    private List<String> supportedSchemaSyncModes = new ArrayList<>();

    @Schema(description = "支持的字段动作")
    private List<String> supportedFieldActions = new ArrayList<>();

    @Schema(description = "支持的空值策略")
    private List<String> supportedEmptyValuePolicies = new ArrayList<>();
}
