package com.mumu.woodlin.etl.entity;

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
 * ETL执行历史实体
 * 
 * @author mumu
 * @description ETL任务执行历史记录
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_execution_log")
@Schema(description = "ETL执行历史")
public class EtlExecutionLog extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 执行记录ID
     */
    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    @Schema(description = "执行记录ID")
    private Long logId;
    
    /**
     * 任务ID
     */
    @TableField("job_id")
    @Schema(description = "任务ID")
    private Long jobId;
    
    /**
     * 任务名称
     */
    @TableField("job_name")
    @Schema(description = "任务名称")
    private String jobName;
    
    /**
     * 执行状态（RUNNING-运行中，SUCCESS-成功，FAILED-失败，PARTIAL_SUCCESS-部分成功）
     */
    @TableField("execution_status")
    @Schema(description = "执行状态")
    private String executionStatus;
    
    /**
     * 开始时间
     */
    @TableField("start_time")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @TableField("end_time")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（毫秒）
     */
    @TableField("duration")
    @Schema(description = "执行耗时（毫秒）")
    private Long duration;
    
    /**
     * 提取记录数
     */
    @TableField("extracted_rows")
    @Schema(description = "提取记录数")
    private Long extractedRows;
    
    /**
     * 转换记录数
     */
    @TableField("transformed_rows")
    @Schema(description = "转换记录数")
    private Long transformedRows;
    
    /**
     * 加载记录数
     */
    @TableField("loaded_rows")
    @Schema(description = "加载记录数")
    private Long loadedRows;
    
    /**
     * 失败记录数
     */
    @TableField("failed_rows")
    @Schema(description = "失败记录数")
    private Long failedRows;
    
    /**
     * 错误信息
     */
    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;
    
    /**
     * 执行详情（JSON格式）
     */
    @TableField("execution_detail")
    @Schema(description = "执行详情")
    private String executionDetail;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
