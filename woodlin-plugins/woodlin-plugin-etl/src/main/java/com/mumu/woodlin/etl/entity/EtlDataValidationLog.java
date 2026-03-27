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
 * ETL 数据一致性校验日志实体。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_data_validation_log")
@Schema(description = "ETL数据一致性校验日志")
public class EtlDataValidationLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 校验日志ID。
     */
    @TableId(value = "validation_log_id", type = IdType.ASSIGN_ID)
    @Schema(description = "校验日志ID")
    private Long validationLogId;

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
     * 校验类型。
     */
    @TableField("validation_type")
    @Schema(description = "校验类型")
    private String validationType;

    /**
     * 源侧总行数。
     */
    @TableField("source_row_count")
    @Schema(description = "源侧总行数")
    private Long sourceRowCount;

    /**
     * 目标侧总行数。
     */
    @TableField("target_row_count")
    @Schema(description = "目标侧总行数")
    private Long targetRowCount;

    /**
     * 源侧校验摘要。
     */
    @TableField("source_checksum")
    @Schema(description = "源侧校验摘要")
    private String sourceChecksum;

    /**
     * 目标侧校验摘要。
     */
    @TableField("target_checksum")
    @Schema(description = "目标侧校验摘要")
    private String targetChecksum;

    /**
     * 校验桶数量。
     */
    @TableField("bucket_count")
    @Schema(description = "校验桶数量")
    private Integer bucketCount;

    /**
     * 差异桶数量。
     */
    @TableField("mismatch_bucket_count")
    @Schema(description = "差异桶数量")
    private Integer mismatchBucketCount;

    /**
     * 校验状态。
     */
    @TableField("validation_status")
    @Schema(description = "校验状态")
    private String validationStatus;

    /**
     * 校验信息。
     */
    @TableField("validation_message")
    @Schema(description = "校验信息")
    private String validationMessage;

    /**
     * 校验时间。
     */
    @TableField("validated_at")
    @Schema(description = "校验时间")
    private LocalDateTime validatedAt;

    /**
     * 租户ID。
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
