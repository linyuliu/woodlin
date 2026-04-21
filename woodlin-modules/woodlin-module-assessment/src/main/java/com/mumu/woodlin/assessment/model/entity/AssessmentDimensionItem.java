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
 * 题目-维度映射
 *
 * <p>对应表 sys_assessment_dimension_item。记录某题目归属于哪个/哪些维度，
 * 以及在该维度内的计分权重和反向模式。同一题目可以属于多个维度。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_dimension_item")
@Schema(description = "题目-维度映射")
public class AssessmentDimensionItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "映射ID")
    private Long id;

    @TableField("dimension_id")
    @Schema(description = "维度ID")
    private Long dimensionId;

    @TableField("item_id")
    @Schema(description = "题目ID")
    private Long itemId;

    @TableField("version_id")
    @Schema(description = "版本ID（冗余，便于版本内查询）")
    private Long versionId;

    @TableField("weight")
    @Schema(description = "计分权重（默认 1.0）")
    private BigDecimal weight;

    @TableField("score_mode")
    @Schema(description = "该条目在本维度的计分模式（覆盖维度默认值）: sum/mean/…")
    private String scoreMode;

    @TableField("reverse_mode")
    @Schema(description = "反向计分模式: none/formula/table")
    private String reverseMode;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
