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
 * 发布实例
 *
 * <p>对应表 sys_assessment_publish。将一个测评版本发布为可接受作答的实例，
 * 包含访问控制、时间限制、随机化、断点续答等配置。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_publish")
@Schema(description = "发布实例")
public class AssessmentPublish extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "publish_id", type = IdType.ASSIGN_ID)
    @Schema(description = "发布ID")
    private Long publishId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("version_id")
    @Schema(description = "绑定的版本ID")
    private Long versionId;

    @TableField("publish_code")
    @Schema(description = "发布编码（可用于生成访问链接）")
    private String publishCode;

    @TableField("publish_name")
    @Schema(description = "发布名称/批次名称")
    private String publishName;

    @TableField("status")
    @Schema(description = "发布状态: draft/under_review/published/paused/closed/archived")
    private String status;

    @TableField("start_time")
    @Schema(description = "开放开始时间")
    private LocalDateTime startTime;

    @TableField("end_time")
    @Schema(description = "开放截止时间")
    private LocalDateTime endTime;

    @TableField("time_limit_minutes")
    @Schema(description = "作答总时限（分钟，0表示不限）")
    private Integer timeLimitMinutes;

    @TableField("max_attempts")
    @Schema(description = "最大允许作答次数（0表示不限）")
    private Integer maxAttempts;

    @TableField("allow_anonymous")
    @Schema(description = "是否允许匿名作答")
    private Boolean allowAnonymous;

    @TableField("allow_resume")
    @Schema(description = "是否允许断点续答")
    private Boolean allowResume;

    @TableField("random_strategy")
    @Schema(description = "全局随机化策略: none/random_items/random_options/random_both")
    private String randomStrategy;

    @TableField("access_policy")
    @Schema(description = "访问控制策略（JSON，可配置白名单角色/部门/外部TOKEN）")
    private String accessPolicy;

    @TableField("device_restriction")
    @Schema(description = "设备限制（JSON，如 pc_only/mobile_allowed/tablet_restricted）")
    private String deviceRestriction;

    @TableField("show_result_immediately")
    @Schema(description = "完成后是否立即展示结果")
    private Boolean showResultImmediately;

    @TableField("result_visibility")
    @Schema(description = "结果可见范围: self/admin/all")
    private String resultVisibility;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
