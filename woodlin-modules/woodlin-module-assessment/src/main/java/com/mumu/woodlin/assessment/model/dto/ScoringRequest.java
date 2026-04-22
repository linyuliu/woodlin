package com.mumu.woodlin.assessment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 计分请求
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Schema(description = "计分请求")
public class ScoringRequest {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "作答会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "是否强制重新计分（true=覆盖已有结果）", defaultValue = "false")
    private boolean forceRescore = false;
}
