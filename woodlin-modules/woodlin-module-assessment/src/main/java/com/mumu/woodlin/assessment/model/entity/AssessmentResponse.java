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
 * 逐题作答记录
 *
 * <p>对应表 sys_assessment_response。每题每次作答对应一条记录，
 * 同时保存逻辑编码和展示顺序，以便事后区分乱序影响。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_response")
@Schema(description = "逐题作答记录")
public class AssessmentResponse extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "response_id", type = IdType.ASSIGN_ID)
    @Schema(description = "作答记录ID")
    private Long responseId;

    @TableField("session_id")
    @Schema(description = "所属会话ID")
    private Long sessionId;

    @TableField("form_id")
    @Schema(description = "测评ID（冗余）")
    private Long formId;

    @TableField("item_id")
    @Schema(description = "题目ID（逻辑ID）")
    private Long itemId;

    @TableField("item_code")
    @Schema(description = "题目逻辑编码")
    private String itemCode;

    @TableField("display_order")
    @Schema(description = "该题在本次作答中的展示序号（乱序时与逻辑顺序不同）")
    private Integer displayOrder;

    @TableField("raw_answer")
    @Schema(description = "原始作答 payload（JSON，MEDIUMTEXT）")
    private String rawAnswer;

    @TableField("selected_option_codes")
    @Schema(description = "选中的选项逻辑编码（JSON 数组）")
    private String selectedOptionCodes;

    @TableField("selected_option_display_orders")
    @Schema(description = "选中选项的展示序号（JSON 数组，记录选项的实际展示位置）")
    private String selectedOptionDisplayOrders;

    @TableField("text_answer")
    @Schema(description = "文本答案（简答/填空题，MEDIUMTEXT）")
    private String textAnswer;

    @TableField("answered_at")
    @Schema(description = "本题作答时间")
    private LocalDateTime answeredAt;

    @TableField("time_spent_seconds")
    @Schema(description = "本题用时（秒）")
    private Integer timeSpentSeconds;

    @TableField("is_skipped")
    @Schema(description = "是否跳过/未作答")
    private Boolean isSkipped;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
