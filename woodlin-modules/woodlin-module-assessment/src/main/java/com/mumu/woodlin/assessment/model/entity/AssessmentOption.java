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
 * 选项
 *
 * <p>对应表 sys_assessment_option。单选/多选/评分等题型的候选选项，
 * 同时承载正向分值和反向分值，支持互斥选项标记。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_option")
@Schema(description = "选项")
public class AssessmentOption extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "option_id", type = IdType.ASSIGN_ID)
    @Schema(description = "选项ID")
    private Long optionId;

    @TableField("item_id")
    @Schema(description = "所属题目ID")
    private Long itemId;

    @TableField("option_code")
    @Schema(description = "选项逻辑编码（题目内唯一，如 A/B/C 或 1/2/3）")
    private String optionCode;

    @TableField("display_text")
    @Schema(description = "展示文本（支持富文本/Markdown）")
    private String displayText;

    @TableField("media_url")
    @Schema(description = "选项附属媒体URL（图片/音频）")
    private String mediaUrl;

    @TableField("raw_value")
    @Schema(description = "原始字符串值（用于收集/导出/规则匹配）")
    private String rawValue;

    @TableField("score_value")
    @Schema(description = "正向得分值")
    private BigDecimal scoreValue;

    @TableField("score_reverse_value")
    @Schema(description = "反向得分值（TABLE 模式反向计分时使用）")
    private BigDecimal scoreReverseValue;

    @TableField("is_exclusive")
    @Schema(description = "是否互斥（选中此项则取消其他选项，如'以上都不是'）")
    private Boolean isExclusive;

    @TableField("is_correct")
    @Schema(description = "是否正确答案（用于试卷客观题）")
    private Boolean isCorrect;

    @TableField("sort_order")
    @Schema(description = "选项排序值")
    private Integer sortOrder;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
