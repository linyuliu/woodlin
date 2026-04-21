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
 * 维度得分结果
 *
 * <p>对应表 sys_assessment_result_dimension。拆分存储每个维度的得分明细，
 * 便于分维度报告和横向分析。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_result_dimension")
@Schema(description = "维度得分结果")
public class AssessmentResultDimension extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "记录ID")
    private Long id;

    @TableField("result_id")
    @Schema(description = "总结果ID")
    private Long resultId;

    @TableField("session_id")
    @Schema(description = "会话ID（冗余）")
    private Long sessionId;

    @TableField("dimension_id")
    @Schema(description = "维度ID")
    private Long dimensionId;

    @TableField("dimension_code")
    @Schema(description = "维度编码（冗余）")
    private String dimensionCode;

    @TableField("raw_score")
    @Schema(description = "维度原始分")
    private BigDecimal rawScore;

    @TableField("mean_score")
    @Schema(description = "维度均分（raw_score / item_count）")
    private BigDecimal meanScore;

    @TableField("standard_score")
    @Schema(description = "维度标准分（常模转换后）")
    private BigDecimal standardScore;

    @TableField("percentile")
    @Schema(description = "维度百分位")
    private BigDecimal percentile;

    @TableField("grade_label")
    @Schema(description = "维度等级标签")
    private String gradeLabel;

    @TableField("norm_segment_id")
    @Schema(description = "命中的常模分层ID")
    private Long normSegmentId;

    @TableField("item_count")
    @Schema(description = "参与计分的题数")
    private Integer itemCount;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
