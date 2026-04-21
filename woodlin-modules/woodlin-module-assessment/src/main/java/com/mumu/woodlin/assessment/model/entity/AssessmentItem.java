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
 * 题目/条目
 *
 * <p>对应表 sys_assessment_item。是题库的核心单元，包含题目的逻辑编码、
 * 题型、题干、帮助文本以及一系列控制标志位。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_item")
@Schema(description = "题目/条目")
public class AssessmentItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "item_id", type = IdType.ASSIGN_ID)
    @Schema(description = "题目ID")
    private Long itemId;

    @TableField("version_id")
    @Schema(description = "所属版本ID")
    private Long versionId;

    @TableField("section_id")
    @Schema(description = "所属章节ID")
    private Long sectionId;

    @TableField("form_id")
    @Schema(description = "所属测评ID（冗余）")
    private Long formId;

    @TableField("item_code")
    @Schema(description = "逻辑题号（版本内唯一，如 Q01、P_003）")
    private String itemCode;

    @TableField("item_type")
    @Schema(description = "题型: single_choice/multiple_choice/matrix_single/matrix_multiple/rating/short_text/…")
    private String itemType;

    @TableField("stem")
    @Schema(description = "题干（支持富文本/Markdown，MEDIUMTEXT）")
    private String stem;

    @TableField("stem_media_url")
    @Schema(description = "题干附属媒体URL（图片/音频/视频）")
    private String stemMediaUrl;

    @TableField("help_text")
    @Schema(description = "帮助提示文本")
    private String helpText;

    @TableField("sort_order")
    @Schema(description = "章节内排序值")
    private Integer sortOrder;

    @TableField("is_required")
    @Schema(description = "是否必答")
    private Boolean isRequired;

    @TableField("is_scored")
    @Schema(description = "是否计分题（false 则跳过计分逻辑）")
    private Boolean isScored;

    @TableField("is_anchor")
    @Schema(description = "是否锚题（用于跨版本/跨批次等值）")
    private Boolean isAnchor;

    @TableField("is_reverse")
    @Schema(description = "是否反向题（反向计分标志，具体模式由维度映射决定）")
    private Boolean isReverse;

    @TableField("is_demographic")
    @Schema(description = "是否人口学信息题（收集受试者背景，通常不计入总分）")
    private Boolean isDemographic;

    @TableField("max_score")
    @Schema(description = "单题最高分（用于公式反向计分等）")
    private BigDecimal maxScore;

    @TableField("min_score")
    @Schema(description = "单题最低分")
    private BigDecimal minScore;

    @TableField("time_limit_seconds")
    @Schema(description = "单题作答时限（秒，0表示不限）")
    private Integer timeLimitSeconds;

    @TableField("demographic_field")
    @Schema(description = "人口学字段名（如 gender/age/region_code，当 is_demographic=true 时填写）")
    private String demographicField;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
