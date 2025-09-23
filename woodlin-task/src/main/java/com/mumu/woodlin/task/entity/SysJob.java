package com.mumu.woodlin.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 定时任务实体
 * 
 * @author mumu
 * @description 定时任务信息实体类，用于任务调度管理
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job")
@Schema(description = "定时任务")
public class SysJob extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 任务ID
     */
    @TableId(value = "job_id", type = IdType.ASSIGN_ID)
    @Schema(description = "任务ID")
    private Long jobId;
    
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
     * cron执行表达式
     */
    @TableField("cron_expression")
    @Schema(description = "cron执行表达式")
    private String cronExpression;
    
    /**
     * 计划执行错误策略（1-立即执行，2-执行一次，3-放弃执行）
     */
    @TableField("misfire_policy")
    @Schema(description = "计划执行错误策略")
    private String misfirePolicy;
    
    /**
     * 是否并发执行（1-允许，0-禁止）
     */
    @TableField("concurrent")
    @Schema(description = "是否并发执行")
    private String concurrent;
    
    /**
     * 任务状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "任务状态", example = "1")
    private String status;
    
    /**
     * 下次执行时间
     */
    @TableField("next_execute_time")
    @Schema(description = "下次执行时间")
    private LocalDateTime nextExecuteTime;
    
    /**
     * 上次执行时间
     */
    @TableField("last_execute_time")
    @Schema(description = "上次执行时间")
    private LocalDateTime lastExecuteTime;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}