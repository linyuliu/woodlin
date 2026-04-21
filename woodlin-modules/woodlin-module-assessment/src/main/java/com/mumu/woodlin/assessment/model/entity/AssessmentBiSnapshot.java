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
 * BI 预聚合快照（占位）
 *
 * <p>对应表 sys_assessment_bi_snapshot。为 BI 大屏提供预聚合统计快照，
 * 具体聚合维度和指标由二期 ETL 模块填充，当前仅定义元数据骨架。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_bi_snapshot")
@Schema(description = "BI 预聚合快照")
public class AssessmentBiSnapshot extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "snapshot_id", type = IdType.ASSIGN_ID)
    @Schema(description = "快照ID")
    private Long snapshotId;

    @TableField("form_id")
    @Schema(description = "测评ID")
    private Long formId;

    @TableField("publish_id")
    @Schema(description = "发布批次ID（null 表示全批次汇总）")
    private Long publishId;

    @TableField("snapshot_type")
    @Schema(description = "快照类型: daily/weekly/publish_close/manual")
    private String snapshotType;

    @TableField("snapshot_date")
    @Schema(description = "快照日期（日级别截止时间）")
    private LocalDateTime snapshotDate;

    @TableField("total_sessions")
    @Schema(description = "总会话数")
    private Long totalSessions;

    @TableField("completed_sessions")
    @Schema(description = "已完成会话数")
    private Long completedSessions;

    @TableField("avg_score")
    @Schema(description = "平均总分")
    private java.math.BigDecimal avgScore;

    @TableField("metrics_json")
    @Schema(description = "扩展指标 JSON（LONGTEXT，由 ETL 模块填充）")
    private String metricsJson;

    @TableField("dimension_stats_json")
    @Schema(description = "分维度统计 JSON（LONGTEXT）")
    private String dimensionStatsJson;

    @TableField("demographic_stats_json")
    @Schema(description = "人口学分层统计 JSON（LONGTEXT）")
    private String demographicStatsJson;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
