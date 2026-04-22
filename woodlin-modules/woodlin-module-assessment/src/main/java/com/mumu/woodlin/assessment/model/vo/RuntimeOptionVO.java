package com.mumu.woodlin.assessment.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 作答运行时选项
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "作答运行时选项")
public class RuntimeOptionVO {

    @Schema(description = "选项编码")
    private String optionCode;

    @Schema(description = "展示文本")
    private String displayText;

    @Schema(description = "媒体地址")
    private String mediaUrl;

    @Schema(description = "原始值")
    private String rawValue;

    @Schema(description = "是否互斥")
    private Boolean isExclusive;

    @Schema(description = "排序值")
    private Integer sortOrder;
}
