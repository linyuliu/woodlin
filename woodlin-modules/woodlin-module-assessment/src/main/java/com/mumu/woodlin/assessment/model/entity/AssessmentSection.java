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
 * 测评章节/页面
 *
 * <p>对应表 sys_assessment_section。一个测评版本可以包含多个章节，
 * 支持分页/连续两种展示模式，以及章节级别的随机化策略。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_section")
@Schema(description = "测评章节/页面")
public class AssessmentSection extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "section_id", type = IdType.ASSIGN_ID)
    @Schema(description = "章节ID")
    private Long sectionId;

    @TableField("version_id")
    @Schema(description = "所属版本ID")
    private Long versionId;

    @TableField("form_id")
    @Schema(description = "所属测评ID（冗余，便于查询）")
    private Long formId;

    @TableField("section_code")
    @Schema(description = "章节唯一编码（版本内唯一）")
    private String sectionCode;

    @TableField("section_title")
    @Schema(description = "章节标题")
    private String sectionTitle;

    @TableField("section_desc")
    @Schema(description = "章节说明/指导语")
    private String sectionDesc;

    @TableField("display_mode")
    @Schema(description = "展示模式: paged/continuous")
    private String displayMode;

    @TableField("random_strategy")
    @Schema(description = "章节内随机化策略: none/random_items/random_options/random_both")
    private String randomStrategy;

    @TableField("sort_order")
    @Schema(description = "章节排序值")
    private Integer sortOrder;

    @TableField("is_required")
    @Schema(description = "是否必须完成本章节")
    private Boolean isRequired;

    @TableField("anchor_code")
    @Schema(description = "锚点编码（用于断点续答定位）")
    private String anchorCode;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
