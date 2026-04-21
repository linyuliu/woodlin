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
 * 人口学档案快照
 *
 * <p>对应表 sys_assessment_demographic_profile。在作答完成时采集并固化人口学信息，
 * 地区字段建议关联 sys_region.region_code 以便进行地区常模分层。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_demographic_profile")
@Schema(description = "人口学档案快照（作答时采集）")
public class AssessmentDemographicProfile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "profile_id", type = IdType.ASSIGN_ID)
    @Schema(description = "档案ID")
    private Long profileId;

    @TableField("session_id")
    @Schema(description = "关联会话ID")
    private Long sessionId;

    @TableField("user_id")
    @Schema(description = "系统用户ID（匿名时为 null）")
    private Long userId;

    @TableField("gender")
    @Schema(description = "性别（对应 sys_dict_data 中的性别字典值）")
    private String gender;

    @TableField("birth_year")
    @Schema(description = "出生年份")
    private Integer birthYear;

    @TableField("age")
    @Schema(description = "年龄（采集时计算或手工填写）")
    private Integer age;

    @TableField("age_group")
    @Schema(description = "年龄段编码（如 18-25）")
    private String ageGroup;

    @TableField("education_level")
    @Schema(description = "学历（对应 sys_dict_data 中的学历字典值）")
    private String educationLevel;

    @TableField("occupation")
    @Schema(description = "职业（对应字典值）")
    private String occupation;

    @TableField("region_code")
    @Schema(description = "地区编码（关联 sys_region.region_code）")
    private String regionCode;

    @TableField("province_code")
    @Schema(description = "省级编码（冗余，便于地区常模分层）")
    private String provinceCode;

    @TableField("marital_status")
    @Schema(description = "婚姻状况（对应字典值）")
    private String maritalStatus;

    @TableField("ethnicity")
    @Schema(description = "民族（对应字典值）")
    private String ethnicity;

    @TableField("extra_fields")
    @Schema(description = "扩展人口学字段（JSON Map，存储测评自定义人口学题的作答）")
    private String extraFields;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    @TableField("norm_weight")
    @Schema(description = "常模分层权重（用于加权统计，默认 1.0）")
    private BigDecimal normWeight;
}
