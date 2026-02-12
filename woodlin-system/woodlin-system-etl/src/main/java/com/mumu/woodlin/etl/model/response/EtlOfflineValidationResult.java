package com.mumu.woodlin.etl.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线ETL配置预校验结果。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Schema(description = "离线ETL配置预校验结果")
public class EtlOfflineValidationResult {

    @Schema(description = "是否校验通过")
    private Boolean valid;

    @Schema(description = "源表是否存在")
    private Boolean sourceTableExists;

    @Schema(description = "目标表是否存在")
    private Boolean targetTableExists;

    @Schema(description = "错误信息列表")
    private List<String> errors = new ArrayList<>();

    @Schema(description = "告警信息列表")
    private List<String> warnings = new ArrayList<>();

    @Schema(description = "源表字段")
    private List<String> sourceColumns = new ArrayList<>();

    @Schema(description = "目标表字段")
    private List<String> targetColumns = new ArrayList<>();

    @Schema(description = "推荐的增量字段")
    private List<String> suggestedIncrementalColumns = new ArrayList<>();
}
