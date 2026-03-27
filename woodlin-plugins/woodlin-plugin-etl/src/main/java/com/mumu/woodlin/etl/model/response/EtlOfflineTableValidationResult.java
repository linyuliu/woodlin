package com.mumu.woodlin.etl.model.response;

import com.mumu.woodlin.etl.model.EtlOfflineFieldRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ETL 离线任务表级校验结果。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "ETL离线任务表级校验结果")
public class EtlOfflineTableValidationResult {

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
     * 是否校验通过。
     */
    @Schema(description = "是否校验通过")
    private Boolean valid;

    /**
     * 源表是否存在。
     */
    @Schema(description = "源表是否存在")
    private Boolean sourceTableExists;

    /**
     * 目标表是否存在。
     */
    @Schema(description = "目标表是否存在")
    private Boolean targetTableExists;

    /**
     * 错误信息。
     */
    @Schema(description = "错误信息")
    private List<String> errors = new ArrayList<>();

    /**
     * 告警信息。
     */
    @Schema(description = "告警信息")
    private List<String> warnings = new ArrayList<>();

    /**
     * 源表字段。
     */
    @Schema(description = "源表字段")
    private List<String> sourceColumns = new ArrayList<>();

    /**
     * 目标表字段。
     */
    @Schema(description = "目标表字段")
    private List<String> targetColumns = new ArrayList<>();

    /**
     * 推荐的增量字段。
     */
    @Schema(description = "推荐的增量字段")
    private List<String> suggestedIncrementalColumns = new ArrayList<>();

    /**
     * 自动匹配的字段规则建议。
     */
    @Schema(description = "自动匹配的字段规则建议")
    private List<EtlOfflineFieldRule> suggestedFieldRules = new ArrayList<>();
}
