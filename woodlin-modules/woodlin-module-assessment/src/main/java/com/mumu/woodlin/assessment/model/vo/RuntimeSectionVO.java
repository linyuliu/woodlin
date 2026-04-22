package com.mumu.woodlin.assessment.model.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 作答运行时章节
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "作答运行时章节")
public class RuntimeSectionVO {

    @Schema(description = "章节编码")
    private String sectionCode;

    @Schema(description = "章节标题")
    private String sectionTitle;

    @Schema(description = "章节说明")
    private String sectionDesc;

    @Schema(description = "展示模式")
    private String displayMode;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否必答")
    private Boolean isRequired;

    @Schema(description = "锚点编码")
    private String anchorCode;

    @Schema(description = "题目列表（按展示顺序）")
    private List<RuntimeItemVO> items;
}
