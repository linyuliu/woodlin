package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;
import java.math.BigDecimal;
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
 * 等值/链接记录
 *
 * <p>对应表 sys_assessment_equating_record。保存版本间等值操作的锚题集、
 * 方法、系数及适用版本范围，用于跨版本分数可比性。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_equating_record")
@Schema(description = "等值/链接记录")
public class AssessmentEquatingRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "equating_id", type = IdType.ASSIGN_ID)
    @Schema(description = "等值记录ID")
    private Long equatingId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("source_version_id")
    @Schema(description = "源版本ID（被等值的旧版本）")
    private Long sourceVersionId;

    @TableField("target_version_id")
    @Schema(description = "目标版本ID（新版本）")
    private Long targetVersionId;

    @TableField("equating_method")
    @Schema(description = "等值方法: mean_sigma/irt_linking/equipercentile/…")
    private String equatingMethod;

    @TableField("anchor_item_codes")
    @Schema(description = "锚题编码列表（JSON 数组）")
    private String anchorItemCodes;

    @TableField("anchor_count")
    @Schema(description = "锚题数量")
    private Integer anchorCount;

    @TableField("slope")
    @Schema(description = "等值系数 A（线性等值斜率）")
    private BigDecimal slope;

    @TableField("intercept")
    @Schema(description = "等值系数 B（线性等值截距）")
    private BigDecimal intercept;

    @TableField("rmsea")
    @Schema(description = "RMSEA（拟合指数，IRT 等值时使用）")
    private BigDecimal rmsea;

    @TableField("equating_result_json")
    @Schema(description = "完整等值结果 JSON（LONGTEXT）")
    private String equatingResultJson;

    @TableField("computed_at")
    @Schema(description = "计算时间")
    private LocalDateTime computedAt;

    @TableField("computed_by")
    @Schema(description = "操作人")
    private String computedBy;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
