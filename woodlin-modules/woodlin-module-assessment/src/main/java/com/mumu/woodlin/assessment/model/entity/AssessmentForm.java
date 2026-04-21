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
 * 测评主体（量表/试卷/问卷）
 *
 * <p>对应表 sys_assessment_form。每条记录代表一个独立的测评产品，可通过 {@code assessment_type}
 * 区分量表、试卷和问卷三类场景。版本管理由 {@link AssessmentFormVersion} 承接。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_form")
@Schema(description = "测评主体（量表/试卷/问卷）")
public class AssessmentForm extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "form_id", type = IdType.ASSIGN_ID)
    @Schema(description = "测评ID")
    private Long formId;

    @TableField("form_code")
    @Schema(description = "唯一编码（业务标识，建议字母+数字）")
    private String formCode;

    @TableField("form_name")
    @Schema(description = "测评名称")
    private String formName;

    @TableField("assessment_type")
    @Schema(description = "测评类型: scale/exam/survey")
    private String assessmentType;

    @TableField("category_code")
    @Schema(description = "分类编码（可关联字典）")
    private String categoryCode;

    @TableField("description")
    @Schema(description = "简介/说明")
    private String description;

    @TableField("cover_url")
    @Schema(description = "封面图URL")
    private String coverUrl;

    @TableField("tags")
    @Schema(description = "标签，JSON数组字符串")
    private String tags;

    @TableField("current_version_id")
    @Schema(description = "当前活跃版本ID（外键 sys_assessment_form_version）")
    private Long currentVersionId;

    @TableField("status")
    @Schema(description = "启用状态: 1=启用 0=禁用")
    private Integer status;

    @TableField("sort_order")
    @Schema(description = "排序值")
    private Integer sortOrder;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
