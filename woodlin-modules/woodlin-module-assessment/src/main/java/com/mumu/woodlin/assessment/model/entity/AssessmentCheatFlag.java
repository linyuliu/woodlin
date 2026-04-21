package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;
import java.time.LocalDateTime;

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
 * 作弊/风险标记
 *
 * <p>对应表 sys_assessment_cheat_flag。记录风控规则命中结果及人工复核结论。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_cheat_flag")
@Schema(description = "作弊/风险标记")
public class AssessmentCheatFlag extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "flag_id", type = IdType.ASSIGN_ID)
    @Schema(description = "标记ID")
    private Long flagId;

    @TableField("session_id")
    @Schema(description = "关联会话ID")
    private Long sessionId;

    @TableField("rule_code")
    @Schema(description = "触发的风控规则编码")
    private String ruleCode;

    @TableField("rule_desc")
    @Schema(description = "规则描述")
    private String ruleDesc;

    @TableField("risk_level")
    @Schema(description = "风险等级: none/low/medium/high/confirmed")
    private String riskLevel;

    @TableField("evidence")
    @Schema(description = "证据/触发详情（JSON）")
    private String evidence;

    @TableField("detected_at")
    @Schema(description = "检测时间")
    private LocalDateTime detectedAt;

    @TableField("reviewed_by")
    @Schema(description = "人工复核人")
    private String reviewedBy;

    @TableField("reviewed_at")
    @Schema(description = "人工复核时间")
    private LocalDateTime reviewedAt;

    @TableField("review_conclusion")
    @Schema(description = "复核结论: confirmed_cheat/false_positive/pending")
    private String reviewConclusion;

    @TableField("review_note")
    @Schema(description = "复核备注")
    private String reviewNote;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
