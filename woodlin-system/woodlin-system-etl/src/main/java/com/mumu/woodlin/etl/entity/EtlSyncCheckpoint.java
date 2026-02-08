package com.mumu.woodlin.etl.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * ETL 同步检查点实体。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_sync_checkpoint")
@Schema(description = "ETL同步检查点")
public class EtlSyncCheckpoint extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 检查点主键ID。
     */
    @TableId(value = "checkpoint_id", type = IdType.ASSIGN_ID)
    @Schema(description = "检查点主键ID")
    private Long checkpointId;

    /**
     * ETL任务ID。
     */
    @TableField("job_id")
    @Schema(description = "ETL任务ID")
    private Long jobId;

    /**
     * 同步模式。
     */
    @TableField("sync_mode")
    @Schema(description = "同步模式")
    private String syncMode;

    /**
     * 增量字段。
     */
    @TableField("incremental_column")
    @Schema(description = "增量字段")
    private String incrementalColumn;

    /**
     * 上次同步最大增量值。
     */
    @TableField("last_incremental_value")
    @Schema(description = "上次同步最大增量值")
    private String lastIncrementalValue;

    /**
     * 上次成功同步时间。
     */
    @TableField("last_sync_time")
    @Schema(description = "上次成功同步时间")
    private LocalDateTime lastSyncTime;

    /**
     * 上次同步源数据行数。
     */
    @TableField("source_row_count")
    @Schema(description = "上次同步源数据行数")
    private Long sourceRowCount;

    /**
     * 上次同步目标数据行数。
     */
    @TableField("target_row_count")
    @Schema(description = "上次同步目标数据行数")
    private Long targetRowCount;

    /**
     * 本次执行命中的桶数量。
     */
    @TableField("applied_bucket_count")
    @Schema(description = "本次执行命中的桶数量")
    private Integer appliedBucketCount;

    /**
     * 本次执行跳过的桶数量。
     */
    @TableField("skipped_bucket_count")
    @Schema(description = "本次执行跳过的桶数量")
    private Integer skippedBucketCount;

    /**
     * 最近一次校验状态。
     */
    @TableField("validation_status")
    @Schema(description = "最近一次校验状态")
    private String validationStatus;

    /**
     * 最近一次执行日志ID。
     */
    @TableField("last_execution_log_id")
    @Schema(description = "最近一次执行日志ID")
    private Long lastExecutionLogId;

    /**
     * 租户ID。
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 备注。
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
