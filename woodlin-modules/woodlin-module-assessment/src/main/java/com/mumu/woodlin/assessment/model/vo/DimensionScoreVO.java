package com.mumu.woodlin.assessment.model.vo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 维度得分视图对象
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Builder
@Schema(description = "维度得分视图对象")
public class DimensionScoreVO {

    @Schema(description = "维度ID")
    private Long dimensionId;

    @Schema(description = "维度编码")
    private String dimensionCode;

    @Schema(description = "维度名称")
    private String dimensionName;

    @Schema(description = "维度原始分（计分模式聚合后）")
    private BigDecimal rawScore;

    @Schema(description = "维度均分（rawScore / itemCount）")
    private BigDecimal meanScore;

    @Schema(description = "参与计分的题数")
    private Integer itemCount;

    @Schema(description = "维度标准分（常模转换后，无常模时为 null）")
    private BigDecimal standardScore;

    @Schema(description = "维度百分位（0-100，无常模时为 null）")
    private BigDecimal percentile;

    @Schema(description = "维度等级标签（无常模时为 null）")
    private String gradeLabel;

    @Schema(description = "命中的常模分层ID（无常模时为 null）")
    private Long normSegmentId;

    @Schema(description = "常模匹配路径（用于审计，如 full_match / fallback_no_region）")
    private String normMatchPath;
}
