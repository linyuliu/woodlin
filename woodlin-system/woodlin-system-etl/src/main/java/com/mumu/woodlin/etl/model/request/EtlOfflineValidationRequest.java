package com.mumu.woodlin.etl.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 离线ETL配置预校验请求。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线ETL配置预校验请求")
public class EtlOfflineValidationRequest {

    @NotBlank(message = "源数据源不能为空")
    @Schema(description = "源数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceDatasource;

    @Schema(description = "源Schema")
    private String sourceSchema;

    @NotBlank(message = "源表不能为空")
    @Schema(description = "源表", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceTable;

    @NotBlank(message = "目标数据源不能为空")
    @Schema(description = "目标数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetDatasource;

    @Schema(description = "目标Schema")
    private String targetSchema;

    @NotBlank(message = "目标表不能为空")
    @Schema(description = "目标表", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetTable;

    @Schema(description = "同步模式：FULL/INCREMENTAL", example = "FULL")
    private String syncMode;

    @Schema(description = "增量字段")
    private String incrementalColumn;
}
