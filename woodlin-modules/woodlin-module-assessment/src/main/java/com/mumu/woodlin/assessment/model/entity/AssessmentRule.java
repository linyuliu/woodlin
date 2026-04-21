package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;

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
 * 规则定义
 *
 * <p>对应表 sys_assessment_rule。统一存储所有规则类型（显示/跳转/校验/计分/常模匹配/报告），
 * 通过 {@code rule_type} 区分，并同时保存 DSL 源码和编译结果。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_rule")
@Schema(description = "规则定义")
public class AssessmentRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "rule_id", type = IdType.ASSIGN_ID)
    @Schema(description = "规则ID")
    private Long ruleId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("version_id")
    @Schema(description = "所属版本ID")
    private Long versionId;

    @TableField("rule_code")
    @Schema(description = "规则编码（版本内唯一）")
    private String ruleCode;

    @TableField("rule_name")
    @Schema(description = "规则名称")
    private String ruleName;

    @TableField("rule_type")
    @Schema(description = "规则类型: display/branch/validation/score/norm_match/report/eligibility/terminate")
    private String ruleType;

    @TableField("target_type")
    @Schema(description = "作用目标类型: item/section/dimension/form")
    private String targetType;

    @TableField("target_code")
    @Schema(description = "作用目标编码（item_code / section_code / dimension_code）")
    private String targetCode;

    @TableField("dsl_source")
    @Schema(description = "规则 DSL 源码（MEDIUMTEXT）")
    private String dslSource;

    @TableField("compiled_rule")
    @Schema(description = "编译后的规则表示（JSON，MEDIUMTEXT）")
    private String compiledRule;

    @TableField("priority")
    @Schema(description = "执行优先级（数字越小优先级越高）")
    private Integer priority;

    @TableField("is_active")
    @Schema(description = "是否启用")
    private Boolean isActive;

    @TableField("compile_error")
    @Schema(description = "最近一次编译错误")
    private String compileError;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
