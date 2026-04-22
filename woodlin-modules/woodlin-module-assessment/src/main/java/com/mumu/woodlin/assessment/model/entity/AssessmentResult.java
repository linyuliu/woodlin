package com.mumu.woodlin.assessment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 测评总结果
 *
 * <p>对应表 sys_assessment_result。保存一次完整作答的汇总得分，包含原始分、
 * 加权分、标准分（常模转换后）、综合等级以及常模命中信息。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_result")
@Schema(description = "测评总结果")
public class AssessmentResult extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "result_id", type = IdType.ASSIGN_ID)
    @Schema(description = "结果ID")
    private Long resultId;

    @TableField("session_id")
    @Schema(description = "作答会话ID")
    private Long sessionId;

    @TableField("form_id")
    @Schema(description = "测评ID（冗余）")
    private Long formId;

    @TableField("publish_id")
    @Schema(description = "发布实例ID（冗余）")
    private Long publishId;

    @TableField("user_id")
    @Schema(description = "系统用户ID")
    private Long userId;

    @TableField("raw_total_score")
    @Schema(description = "原始总分")
    private BigDecimal rawTotalScore;

    @TableField("weighted_total_score")
    @Schema(description = "加权总分")
    private BigDecimal weightedTotalScore;

    @TableField("standard_score")
    @Schema(description = "标准分（常模转换后）")
    private BigDecimal standardScore;

    @TableField("norm_score_type")
    @Schema(description = "标准分类型: t_score/z_score/percentile/stanine/sten/grade_equivalent/raw_grade")
    private String normScoreType;

    @TableField("percentile")
    @Schema(description = "百分位（0-100）")
    private BigDecimal percentile;

    @TableField("grade_label")
    @Schema(description = "综合等级标签（如 低/中/高 或 优良中差）")
    private String gradeLabel;

    @TableField("norm_set_id")
    @Schema(description = "命中的常模集ID")
    private Long normSetId;

    @TableField("norm_segment_id")
    @Schema(description = "命中的常模分层ID")
    private Long normSegmentId;

    @TableField("risk_level")
    @Schema(description = "综合风险等级: none/low/medium/high/confirmed")
    private String riskLevel;

    @TableField("answered_count")
    @Schema(description = "作答题数")
    private Integer answeredCount;

    @TableField("total_item_count")
    @Schema(description = "应答总题数")
    private Integer totalItemCount;

    @TableField("report_json")
    @Schema(description = "生成的报告 JSON（LONGTEXT）")
    private String reportJson;

    @TableField("score_trace_json")
    @Schema(description = "计分审计轨迹 JSON（LONGTEXT）")
    private String scoreTraceJson;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
