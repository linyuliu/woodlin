package com.mumu.woodlin.assessment.model.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 作答运行时题目
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "作答运行时题目")
public class RuntimeItemVO {

    @Schema(description = "题目编码")
    private String itemCode;

    @Schema(description = "题目类型")
    private String itemType;

    @Schema(description = "题干")
    private String stem;

    @Schema(description = "题干媒体地址")
    private String stemMediaUrl;

    @Schema(description = "帮助文本")
    private String helpText;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否必答")
    private Boolean isRequired;

    @Schema(description = "是否计分题")
    private Boolean isScored;

    @Schema(description = "是否锚题")
    private Boolean isAnchor;

    @Schema(description = "是否反向题")
    private Boolean isReverse;

    @Schema(description = "是否人口学题")
    private Boolean isDemographic;

    @Schema(description = "单题时限（秒）")
    private Integer timeLimitSeconds;

    @Schema(description = "人口学字段")
    private String demographicField;

    @Schema(description = "选项列表（按展示顺序）")
    private List<RuntimeOptionVO> options;
}
