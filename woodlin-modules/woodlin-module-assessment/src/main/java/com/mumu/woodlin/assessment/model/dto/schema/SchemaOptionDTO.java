package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 选项 DTO
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "选项 DTO")
public class SchemaOptionDTO {

    @Schema(description = "选项编码")
    private String optionCode;

    @Schema(description = "展示文本")
    private String displayText;

    @Schema(description = "媒体地址")
    private String mediaUrl;

    @Schema(description = "原始值")
    private String rawValue;

    @Schema(description = "正向分值")
    private BigDecimal scoreValue;

    @Schema(description = "反向分值")
    private BigDecimal scoreReverseValue;

    @Schema(description = "是否互斥")
    private Boolean isExclusive;

    @Schema(description = "是否正确答案")
    private Boolean isCorrect;

    @Schema(description = "排序值")
    private Integer sortOrder;
}
