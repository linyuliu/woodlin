package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;
import java.time.LocalDate;

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
 * 常模集
 *
 * <p>对应表 sys_assessment_norm_set。一个测评可以有多个常模集（不同版本、不同样本来源），
 * 通过分层（NormSegment）进一步按人口学指标细化。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_norm_set")
@Schema(description = "常模集")
public class AssessmentNormSet extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "norm_set_id", type = IdType.ASSIGN_ID)
    @Schema(description = "常模集ID")
    private Long normSetId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("norm_set_name")
    @Schema(description = "常模集名称")
    private String normSetName;

    @TableField("norm_set_code")
    @Schema(description = "常模集编码")
    private String normSetCode;

    @TableField("sample_size")
    @Schema(description = "样本量")
    private Integer sampleSize;

    @TableField("collection_start")
    @Schema(description = "样本采集开始日期")
    private LocalDate collectionStart;

    @TableField("collection_end")
    @Schema(description = "样本采集结束日期")
    private LocalDate collectionEnd;

    @TableField("source_desc")
    @Schema(description = "数据来源说明")
    private String sourceDesc;

    @TableField("applicability_desc")
    @Schema(description = "适用范围说明（如 适用于 18-60 岁成人）")
    private String applicabilityDesc;

    @TableField("is_default")
    @Schema(description = "是否为测评默认常模集")
    private Boolean isDefault;

    @TableField("status")
    @Schema(description = "状态: 1=启用 0=停用")
    private Integer status;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
