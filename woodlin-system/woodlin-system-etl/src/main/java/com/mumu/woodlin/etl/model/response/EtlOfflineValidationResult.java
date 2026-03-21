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

    /**
     * 是否校验通过。
     */
    @Schema(description = "是否校验通过")
    private Boolean valid;

    /**
     * 全局错误信息。
     */
    @Schema(description = "全局错误信息")
    private List<String> errors = new ArrayList<>();

    /**
     * 全局告警信息。
     */
    @Schema(description = "全局告警信息")
    private List<String> warnings = new ArrayList<>();

    /**
     * 表级校验结果。
     */
    @Schema(description = "表级校验结果")
    private List<EtlOfflineTableValidationResult> tableResults = new ArrayList<>();
}
