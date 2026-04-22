package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目 DTO
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "题目 DTO")
public class SchemaItemDTO {

    @Schema(description = "题目编码")
    private String itemCode;

    @Schema(description = "题型")
    private String itemType;

    @Schema(description = "题干")
    private String stem;

    @Schema(description = "题干媒体地址")
    private String stemMediaUrl;

    @Schema(description = "帮助文案")
    private String helpText;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否必答")
    private Boolean isRequired;

    @Schema(description = "是否计分")
    private Boolean isScored;

    @Schema(description = "是否锚题")
    private Boolean isAnchor;

    @Schema(description = "是否反向题")
    private Boolean isReverse;

    @Schema(description = "是否人口学题")
    private Boolean isDemographic;

    @Schema(description = "单题最高分")
    private BigDecimal maxScore;

    @Schema(description = "单题最低分")
    private BigDecimal minScore;

    @Schema(description = "单题限时")
    private Integer timeLimitSeconds;

    @Schema(description = "人口学字段名")
    private String demographicField;

    @Schema(description = "选项列表")
    private List<SchemaOptionDTO> options = new ArrayList<>();

    @Schema(description = "维度映射列表")
    private List<SchemaDimensionBindingDTO> dimensionBindings = new ArrayList<>();
}
