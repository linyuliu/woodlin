package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 常模转换映射
 *
 * <p>对应表 sys_assessment_norm_conversion。存储原始分到标准分/百分位/等级的映射关系，
 * 可以是精确查表或区间映射，同时记录目标分数类型。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_norm_conversion")
@Schema(description = "常模转换映射")
public class AssessmentNormConversion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "conversion_id", type = IdType.ASSIGN_ID)
    @Schema(description = "映射ID")
    private Long conversionId;

    @TableField("segment_id")
    @Schema(description = "所属分层ID")
    private Long segmentId;

    @TableField("dimension_id")
    @Schema(description = "所属维度ID（null 表示总分转换）")
    private Long dimensionId;

    @TableField("norm_score_type")
    @Schema(description = "目标标准分类型: t_score/z_score/percentile/stanine/sten/grade_equivalent/raw_grade")
    private String normScoreType;

    @TableField("raw_score_min")
    @Schema(description = "原始分区间下限（区间映射时使用）")
    private BigDecimal rawScoreMin;

    @TableField("raw_score_max")
    @Schema(description = "原始分区间上限（区间映射时使用）")
    private BigDecimal rawScoreMax;

    @TableField("raw_score_exact")
    @Schema(description = "精确原始分（精确查表时使用，与区间二选一）")
    private BigDecimal rawScoreExact;

    @TableField("standard_score")
    @Schema(description = "对应的标准分值")
    private BigDecimal standardScore;

    @TableField("percentile")
    @Schema(description = "对应的百分位（0-100）")
    private BigDecimal percentile;

    @TableField("grade_label")
    @Schema(description = "对应的等级标签")
    private String gradeLabel;

    @TableField("sort_order")
    @Schema(description = "排序值（区间映射时按 raw_score_min 排序）")
    private Integer sortOrder;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
