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
 * 断点快照
 *
 * <p>对应表 sys_assessment_session_snapshot。记录断点续答所需的完整上下文快照，
 * 包括当前进度、题序快照、已答缓存、计时信息等。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_session_snapshot")
@Schema(description = "作答断点快照")
public class AssessmentSessionSnapshot extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "snapshot_id", type = IdType.ASSIGN_ID)
    @Schema(description = "快照ID")
    private Long snapshotId;

    @TableField("session_id")
    @Schema(description = "所属会话ID")
    private Long sessionId;

    @TableField("current_section_code")
    @Schema(description = "当前所在章节编码")
    private String currentSectionCode;

    @TableField("current_item_code")
    @Schema(description = "当前作答到的题目编码")
    private String currentItemCode;

    @TableField("item_order_snapshot")
    @Schema(description = "题目展示顺序快照（JSON 数组，保存 item_code 序列）")
    private String itemOrderSnapshot;

    @TableField("option_order_snapshot")
    @Schema(description = "选项展示顺序快照（JSON Map：item_code -> [option_code, ...]）")
    private String optionOrderSnapshot;

    @TableField("answered_cache")
    @Schema(description = "已答缓存（JSON Map：item_code -> raw answer payload）")
    private String answeredCache;

    @TableField("elapsed_seconds")
    @Schema(description = "截至快照时的累计用时（秒）")
    private Integer elapsedSeconds;

    @TableField("snapshot_at")
    @Schema(description = "快照时间")
    private LocalDateTime snapshotAt;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
