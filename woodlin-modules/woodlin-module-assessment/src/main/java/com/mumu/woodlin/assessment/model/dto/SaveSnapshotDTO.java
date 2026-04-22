package com.mumu.woodlin.assessment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 保存快照入参
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "保存作答快照入参")
public class SaveSnapshotDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "当前章节编码")
    private String currentSectionCode;

    @Schema(description = "当前题目编码")
    private String currentItemCode;

    @Schema(description = "已答缓存JSON")
    private String answeredCache;

    @Schema(description = "累计用时（秒）")
    private Integer elapsedSeconds;
}
