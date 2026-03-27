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
 * ETL 桶位校验快照实体。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_data_bucket_checksum")
@Schema(description = "ETL桶位校验快照")
public class EtlDataBucketChecksum extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 桶位校验记录ID。
     */
    @TableId(value = "bucket_checksum_id", type = IdType.ASSIGN_ID)
    @Schema(description = "桶位校验记录ID")
    private Long bucketChecksumId;

    /**
     * ETL任务ID。
     */
    @TableField("job_id")
    @Schema(description = "ETL任务ID")
    private Long jobId;

    /**
     * 执行日志ID。
     */
    @TableField("execution_log_id")
    @Schema(description = "执行日志ID")
    private Long executionLogId;

    /**
     * 桶号。
     */
    @TableField("bucket_number")
    @Schema(description = "桶号")
    private Integer bucketNumber;

    /**
     * 桶边界起始值。
     */
    @TableField("bucket_boundary_start")
    @Schema(description = "桶边界起始值")
    private String bucketBoundaryStart;

    /**
     * 桶边界结束值。
     */
    @TableField("bucket_boundary_end")
    @Schema(description = "桶边界结束值")
    private String bucketBoundaryEnd;

    /**
     * 源侧桶位行数。
     */
    @TableField("source_row_count")
    @Schema(description = "源侧桶位行数")
    private Long sourceRowCount;

    /**
     * 目标侧桶位行数。
     */
    @TableField("target_row_count")
    @Schema(description = "目标侧桶位行数")
    private Long targetRowCount;

    /**
     * 源侧桶位校验值。
     */
    @TableField("source_checksum")
    @Schema(description = "源侧桶位校验值")
    private String sourceChecksum;

    /**
     * 目标侧桶位校验值。
     */
    @TableField("target_checksum")
    @Schema(description = "目标侧桶位校验值")
    private String targetChecksum;

    /**
     * 重试次数。
     */
    @TableField("retry_count")
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 重试是否修复成功。
     */
    @TableField("retry_success")
    @Schema(description = "重试是否修复成功", example = "1")
    private String retrySuccess;

    /**
     * 最后一次重试时间。
     */
    @TableField("last_retry_time")
    @Schema(description = "最后一次重试时间")
    private LocalDateTime lastRetryTime;

    /**
     * 是否需要同步。
     */
    @TableField("needs_sync")
    @Schema(description = "是否需要同步", example = "1")
    private String needsSync;

    /**
     * 跳过原因。
     */
    @TableField("skip_reason")
    @Schema(description = "跳过原因")
    private String skipReason;

    /**
     * 校验时间。
     */
    @TableField("compared_at")
    @Schema(description = "校验时间")
    private LocalDateTime comparedAt;

    /**
     * 租户ID。
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
