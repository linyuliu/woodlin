package com.mumu.woodlin.task.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 定时任务调度日志实体
 *
 * @author yulin
 * @description 定时任务执行日志，仅追加，不参与逻辑删除
 * @since 2026-06
 */
@Data
@Accessors(chain = true)
@TableName("sys_job_log")
@Schema(description = "定时任务调度日志")
public class SysJobLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    @Schema(description = "日志ID")
    private Long logId;

    /**
     * 任务名称
     */
    @TableField("job_name")
    @Schema(description = "任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @TableField("job_group")
    @Schema(description = "任务组名")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @TableField("invoke_target")
    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    /**
     * 执行状态（0-成功，1-失败）
     */
    @TableField("status")
    @Schema(description = "执行状态")
    private String status;

    /**
     * 执行消息或错误信息
     */
    @TableField("message")
    @Schema(description = "执行消息")
    private String message;

    /**
     * 开始时间
     */
    @TableField("start_time")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("stop_time")
    @Schema(description = "结束时间")
    private LocalDateTime stopTime;

    /**
     * 耗时（毫秒）
     */
    @TableField("elapsed_time")
    @Schema(description = "耗时(毫秒)")
    private Long elapsedTime;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
