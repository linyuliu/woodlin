package com.mumu.woodlin.assessment.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 提交作答入参
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "提交作答入参")
public class SubmitAnswersDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Valid
    @Schema(description = "答案列表")
    private List<AnswerItemDTO> answers;

    @Schema(description = "累计用时（秒）")
    private Integer elapsedSeconds;
}
