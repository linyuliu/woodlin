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
 * 常模分层
 *
 * <p>对应表 sys_assessment_norm_segment。按人口学指标（地区/年龄段/性别/学历等）
 * 对常模集进行细分，常模转换时先通过分层规则匹配 segment，再在 NormConversion 中查表。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_norm_segment")
@Schema(description = "常模分层")
public class AssessmentNormSegment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "segment_id", type = IdType.ASSIGN_ID)
    @Schema(description = "分层ID")
    private Long segmentId;

    @TableField("norm_set_id")
    @Schema(description = "所属常模集ID")
    private Long normSetId;

    @TableField("segment_code")
    @Schema(description = "分层编码（常模集内唯一）")
    private String segmentCode;

    @TableField("segment_name")
    @Schema(description = "分层名称（如 男性18-25岁）")
    private String segmentName;

    @TableField("gender_filter")
    @Schema(description = "性别过滤（null=不限）")
    private String genderFilter;

    @TableField("age_min")
    @Schema(description = "年龄下限（null=不限）")
    private Integer ageMin;

    @TableField("age_max")
    @Schema(description = "年龄上限（null=不限）")
    private Integer ageMax;

    @TableField("education_filter")
    @Schema(description = "学历过滤（null=不限）")
    private String educationFilter;

    @TableField("region_code_filter")
    @Schema(description = "地区编码过滤（支持前缀匹配，如 110 匹配北京所有地区）")
    private String regionCodeFilter;

    @TableField("sample_size")
    @Schema(description = "本分层样本量")
    private Integer sampleSize;

    @TableField("extra_filter")
    @Schema(description = "扩展过滤条件（JSON，承接更多人口学维度）")
    private String extraFilter;

    @TableField("sort_priority")
    @Schema(description = "匹配优先级（数字越小越先匹配，越精确的分层优先级应越高）")
    private Integer sortPriority;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
