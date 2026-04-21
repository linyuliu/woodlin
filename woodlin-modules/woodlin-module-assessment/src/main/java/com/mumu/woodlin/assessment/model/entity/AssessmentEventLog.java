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
 * 作答事件日志
 *
 * <p>对应表 sys_assessment_event_log。记录作答过程中的行为事件流，
 * 用于防作弊分析和用户行为研究。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_event_log")
@Schema(description = "作答事件日志（行为埋点）")
public class AssessmentEventLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "event_id", type = IdType.ASSIGN_ID)
    @Schema(description = "事件ID")
    private Long eventId;

    @TableField("session_id")
    @Schema(description = "所属会话ID")
    private Long sessionId;

    @TableField("event_type")
    @Schema(description = "事件类型: focus/blur/paste/copy_blocked/fullscreen_exit/"
            + "visibility_change/network_retry/tab_switch/right_click_blocked/…")
    private String eventType;

    @TableField("item_code")
    @Schema(description = "事件发生时当前题目编码（可为 null）")
    private String itemCode;

    @TableField("event_payload")
    @Schema(description = "事件附加数据（JSON）")
    private String eventPayload;

    @TableField("occurred_at")
    @Schema(description = "事件发生时间（毫秒精度）")
    private LocalDateTime occurredAt;

    @TableField("elapsed_seconds")
    @Schema(description = "事件发生时的累计用时（秒）")
    private Integer elapsedSeconds;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
