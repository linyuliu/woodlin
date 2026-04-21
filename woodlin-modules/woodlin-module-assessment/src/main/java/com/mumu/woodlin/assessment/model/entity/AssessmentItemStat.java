package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;
import java.math.BigDecimal;

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
 * 题目项目分析统计
 *
 * <p>对应表 sys_assessment_item_stat。保存题目级心理测量学指标，
 * 包括区分度、难度、缺失率、alpha if deleted、DIF 标记等。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_item_stat")
@Schema(description = "题目项目分析统计")
public class AssessmentItemStat extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "stat_id", type = IdType.ASSIGN_ID)
    @Schema(description = "统计记录ID")
    private Long statId;

    @TableField("item_id")
    @Schema(description = "题目ID")
    private Long itemId;

    @TableField("version_id")
    @Schema(description = "版本ID")
    private Long versionId;

    @TableField("publish_id")
    @Schema(description = "发布批次ID（null 表示汇总所有批次）")
    private Long publishId;

    @TableField("sample_size")
    @Schema(description = "统计样本量")
    private Integer sampleSize;

    @TableField("item_difficulty")
    @Schema(description = "难度系数 P（0-1，CTT）")
    private BigDecimal itemDifficulty;

    @TableField("item_discrimination")
    @Schema(description = "区分度 D（点二列相关或 rpb）")
    private BigDecimal itemDiscrimination;

    @TableField("missing_rate")
    @Schema(description = "缺失率（0-1）")
    private BigDecimal missingRate;

    @TableField("ceiling_rate")
    @Schema(description = "天花板效应比例（最高分比例）")
    private BigDecimal ceilingRate;

    @TableField("floor_rate")
    @Schema(description = "地板效应比例（最低分比例）")
    private BigDecimal floorRate;

    @TableField("alpha_if_deleted")
    @Schema(description = "删除该题后的量表 alpha 系数")
    private BigDecimal alphaIfDeleted;

    @TableField("omega_if_deleted")
    @Schema(description = "删除该题后的量表 omega 系数")
    private BigDecimal omegaIfDeleted;

    @TableField("dif_flag")
    @Schema(description = "DIF 标记（A/B/C 或 none）")
    private String difFlag;

    @TableField("dif_reference_group")
    @Schema(description = "DIF 参照组描述")
    private String difReferenceGroup;

    @TableField("stat_json")
    @Schema(description = "完整统计结果 JSON（LONGTEXT，可存 IRTparams 等扩展数据）")
    private String statJson;

    @TableField("computed_at")
    @Schema(description = "统计计算时间")
    private java.time.LocalDateTime computedAt;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
