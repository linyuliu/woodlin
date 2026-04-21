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
 * 作答会话
 *
 * <p>对应表 sys_assessment_session。代表一名受试者在某次发布中的一次作答过程。
 * 支持匿名作答（通过 anonymous_token）和有账号作答两种模式。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_session")
@Schema(description = "作答会话")
public class AssessmentSession extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id", type = IdType.ASSIGN_ID)
    @Schema(description = "会话ID")
    private Long sessionId;

    @TableField("publish_id")
    @Schema(description = "发布实例ID")
    private Long publishId;

    @TableField("form_id")
    @Schema(description = "测评ID（冗余）")
    private Long formId;

    @TableField("version_id")
    @Schema(description = "版本ID（冗余，记录作答时使用的版本）")
    private Long versionId;

    @TableField("user_id")
    @Schema(description = "系统用户ID（匿名时为 null）")
    private Long userId;

    @TableField("anonymous_token")
    @Schema(description = "匿名标识 Token（用于无账号场景下唯一标识受试者）")
    private String anonymousToken;

    @TableField("status")
    @Schema(description = "会话状态: not_started/in_progress/paused/completed/expired/abandoned/invalidated")
    private String status;

    @TableField("display_seed")
    @Schema(description = "乱序随机种子（用于还原展示顺序）")
    private Long displaySeed;

    @TableField("started_at")
    @Schema(description = "开始作答时间")
    private LocalDateTime startedAt;

    @TableField("completed_at")
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @TableField("elapsed_seconds")
    @Schema(description = "累计用时（秒）")
    private Integer elapsedSeconds;

    @TableField("client_ip")
    @Schema(description = "作答客户端IP")
    private String clientIp;

    @TableField("user_agent")
    @Schema(description = "浏览器 User-Agent")
    private String userAgent;

    @TableField("device_type")
    @Schema(description = "设备类型: pc/mobile/tablet")
    private String deviceType;

    @TableField("attempt_number")
    @Schema(description = "本次是第几次作答（从1开始）")
    private Integer attemptNumber;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
