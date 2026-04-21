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
 * 维度/因子/分量表
 *
 * <p>对应表 sys_assessment_dimension。一个测评可以包含多个维度，
 * 维度可以嵌套（通过 parent_dimension_id），支持二阶因子结构。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_dimension")
@Schema(description = "维度/因子/分量表")
public class AssessmentDimension extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "dimension_id", type = IdType.ASSIGN_ID)
    @Schema(description = "维度ID")
    private Long dimensionId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("version_id")
    @Schema(description = "所属版本ID（版本化维度结构）")
    private Long versionId;

    @TableField("parent_dimension_id")
    @Schema(description = "父维度ID（为 null 则为顶层维度）")
    private Long parentDimensionId;

    @TableField("dimension_code")
    @Schema(description = "维度编码（版本内唯一）")
    private String dimensionCode;

    @TableField("dimension_name")
    @Schema(description = "维度名称")
    private String dimensionName;

    @TableField("dimension_desc")
    @Schema(description = "维度说明")
    private String dimensionDesc;

    @TableField("score_mode")
    @Schema(description = "计分模式: sum/mean/max/min/weighted_sum/custom_dsl")
    private String scoreMode;

    @TableField("score_dsl")
    @Schema(description = "自定义计分 DSL（当 score_mode=custom_dsl 时使用）")
    private String scoreDsl;

    @TableField("norm_set_id")
    @Schema(description = "默认关联的常模集ID")
    private Long normSetId;

    @TableField("sort_order")
    @Schema(description = "排序值")
    private Integer sortOrder;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
