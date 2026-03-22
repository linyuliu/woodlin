package com.mumu.woodlin.etl.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ETL 离线任务表映射配置。
 *
 * <p>一个表映射最终对应一条单表 ETL 任务。</p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务表映射配置")
public class EtlOfflineTableMapping {

    /**
     * 任务名称。
     */
    @Schema(description = "任务名称")
    private String jobName;

    /**
     * 源Schema。
     */
    @Schema(description = "源Schema")
    private String sourceSchema;

    /**
     * 源表。
     */
    @NotBlank(message = "源表不能为空")
    @Schema(description = "源表", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceTable;

    /**
     * 目标Schema。
     */
    @Schema(description = "目标Schema")
    private String targetSchema;

    /**
     * 目标表。
     */
    @NotBlank(message = "目标表不能为空")
    @Schema(description = "目标表", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetTable;

    /**
     * 增量字段。
     */
    @Schema(description = "增量字段")
    private String incrementalColumn;

    /**
     * 过滤条件。
     */
    @Schema(description = "过滤条件")
    private String filterCondition;

    /**
     * 字段规则列表。
     */
    @Valid
    @Schema(description = "字段规则列表")
    private List<EtlOfflineFieldRule> fieldRules = new ArrayList<>();
}
