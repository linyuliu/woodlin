package com.mumu.woodlin.assessment.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 提交单题答案入参
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "提交单题答案入参")
public class AnswerItemDTO {

    @NotBlank(message = "题目编码不能为空")
    @Schema(description = "题目编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemCode;

    @Schema(description = "原始答案JSON")
    private String rawAnswer;

    @Schema(description = "选中的选项编码列表")
    private List<String> selectedOptionCodes;

    @Schema(description = "文本答案")
    private String textAnswer;

    @Schema(description = "本题用时（秒）")
    private Integer timeSpentSeconds;

    @Schema(description = "是否跳过")
    private Boolean isSkipped;

    @Schema(description = "展示顺序")
    private Integer displayOrder;
}
