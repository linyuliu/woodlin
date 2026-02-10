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
 * ETL任务实体
 * 
 * @author mumu
 * @description ETL任务配置信息，用于定义数据提取、转换、加载任务
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_job")
@Schema(description = "ETL任务")
public class EtlJob extends BaseEntity {
    
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
     * 任务描述
     */
    @TableField("job_description")
    @Schema(description = "任务描述")
    private String jobDescription;
    
    /**
     * 源数据源名称（优先匹配动态数据源，未命中时回退匹配 infra_datasource.datasource_code）
     */
    @TableField("source_datasource")
    @Schema(description = "源数据源名称/编码")
    private String sourceDatasource;
    
    /**
     * 源表名
     */
    @TableField("source_table")
    @Schema(description = "源表名")
    private String sourceTable;
    
    /**
     * 源Schema名（PostgreSQL等需要）
     */
    @TableField("source_schema")
    @Schema(description = "源Schema名")
    private String sourceSchema;
    
    /**
     * 源查询SQL（如果不为空则使用SQL，否则使用表名）
     */
    @TableField("source_query")
    @Schema(description = "源查询SQL")
    private String sourceQuery;
    
    /**
     * 目标数据源名称（优先匹配动态数据源，未命中时回退匹配 infra_datasource.datasource_code）
     */
    @TableField("target_datasource")
    @Schema(description = "目标数据源名称/编码")
    private String targetDatasource;
    
    /**
     * 目标表名
     */
    @TableField("target_table")
    @Schema(description = "目标表名")
    private String targetTable;
    
    /**
     * 目标Schema名（PostgreSQL等需要）
     */
    @TableField("target_schema")
    @Schema(description = "目标Schema名")
    private String targetSchema;
    
    /**
     * 同步模式（FULL-全量，INCREMENTAL-增量）
     */
    @TableField("sync_mode")
    @Schema(description = "同步模式", example = "FULL")
    private String syncMode;
    
    /**
     * 增量字段（用于增量同步，如时间戳字段）
     */
    @TableField("incremental_column")
    @Schema(description = "增量字段")
    private String incrementalColumn;
    
    /**
     * 字段映射配置（JSON格式，源字段->目标字段映射）
     */
    @TableField("column_mapping")
    @Schema(description = "字段映射配置")
    private String columnMapping;
    
    /**
     * 数据转换规则（JSON格式，定义数据转换逻辑）
     */
    @TableField("transform_rules")
    @Schema(description = "数据转换规则")
    private String transformRules;
    
    /**
     * 过滤条件（WHERE子句）
     */
    @TableField("filter_condition")
    @Schema(description = "过滤条件")
    private String filterCondition;
    
    /**
     * 批处理大小
     */
    @TableField("batch_size")
    @Schema(description = "批处理大小", example = "1000")
    private Integer batchSize;
    
    /**
     * cron执行表达式
     */
    @TableField("cron_expression")
    @Schema(description = "cron执行表达式")
    private String cronExpression;
    
    /**
     * 任务状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "任务状态", example = "1")
    private String status;
    
    /**
     * 是否并发执行（1-允许，0-禁止）
     */
    @TableField("concurrent")
    @Schema(description = "是否并发执行", example = "0")
    private String concurrent;
    
    /**
     * 失败重试次数
     */
    @TableField("retry_count")
    @Schema(description = "失败重试次数", example = "3")
    private Integer retryCount;
    
    /**
     * 重试间隔（秒）
     */
    @TableField("retry_interval")
    @Schema(description = "重试间隔（秒）", example = "60")
    private Integer retryInterval;
    
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
     * 上次执行状态
     */
    @TableField("last_execute_status")
    @Schema(description = "上次执行状态")
    private String lastExecuteStatus;
    
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
