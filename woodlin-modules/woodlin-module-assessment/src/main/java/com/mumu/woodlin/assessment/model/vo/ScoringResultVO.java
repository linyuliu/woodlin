package com.mumu.woodlin.assessment.model.vo;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 计分结果视图对象
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Builder
@Schema(description = "计分结果视图对象")
public class ScoringResultVO {

    @Schema(description = "结果ID（持久化后的主键）")
    private Long resultId;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "原始总分（所有计分题有效分之和）")
    private BigDecimal rawTotalScore;

    @Schema(description = "加权总分（维度加权后汇总）")
    private BigDecimal weightedTotalScore;

    @Schema(description = "总分标准分（常模转换后，无常模时为 null）")
    private BigDecimal standardScore;

    @Schema(description = "总分百分位（0-100，无常模时为 null）")
    private BigDecimal percentile;

    @Schema(description = "综合等级标签（无常模时为 null）")
    private String gradeLabel;

    @Schema(description = "命中的常模集ID（无常模时为 null）")
    private Long normSetId;

    @Schema(description = "命中的常模分层ID（无常模时为 null）")
    private Long normSegmentId;

    @Schema(description = "常模匹配路径（总分级别，用于审计）")
    private String normMatchPath;

    @Schema(description = "作答题数（含已跳过题目）")
    private Integer answeredCount;

    @Schema(description = "应答总题数（计分题数量）")
    private Integer totalItemCount;

    @Schema(description = "各维度得分明细")
    private List<DimensionScoreVO> dimensionScores;
}
