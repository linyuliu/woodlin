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
 * 锚题定义
 *
 * <p>对应表 sys_assessment_anchor_item。锚题是跨版本等值、跨地区等值、
 * 跨批次可比性分析的核心工具，通过 anchor_code 在多版本间建立对应关系。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_anchor_item")
@Schema(description = "锚题定义")
public class AssessmentAnchorItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "anchor_id", type = IdType.ASSIGN_ID)
    @Schema(description = "锚题记录ID")
    private Long anchorId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("anchor_code")
    @Schema(description = "锚题逻辑编码（跨版本共享）")
    private String anchorCode;

    @TableField("item_id")
    @Schema(description = "当前版本对应的题目ID")
    private Long itemId;

    @TableField("version_id")
    @Schema(description = "版本ID")
    private Long versionId;

    @TableField("anchor_type")
    @Schema(description = "锚题类型: common（常规锚）/ equating（等值锚）/ calibration（校准锚）")
    private String anchorType;

    @TableField("note")
    @Schema(description = "备注")
    private String note;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
