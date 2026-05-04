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
 * 测评版本
 *
 * <p>对应表 sys_assessment_form_version。每次对题库或规则做出修改后，
 * 可创建新版本；发布实例绑定到具体版本，保证历史可追溯。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_form_version")
@Schema(description = "测评版本")
public class AssessmentFormVersion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "version_id", type = IdType.ASSIGN_ID)
    @Schema(description = "版本ID")
    private Long versionId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("version_no")
    @Schema(description = "版本号（如 1.0.0）")
    private String versionNo;

    @TableField("version_tag")
    @Schema(description = "版本标签（如 初稿/正式版/修订版）")
    private String versionTag;

    @TableField("schema_id")
    @Schema(description = "关联的 Schema ID")
    private Long schemaId;

    @TableField("schema_hash")
    @Schema(description = "Schema 内容哈希（SHA-256），用于快速判断版本是否变更")
    private String schemaHash;

    @TableField("dsl_hash")
    @Schema(description = "DSL 源码哈希")
    private String dslHash;

    @TableField("status")
    @Schema(description = "版本状态: draft/compiled/published/archived")
    private String status;

    @TableField("published_at")
    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @TableField("published_by")
    @Schema(description = "发布人")
    private String publishedBy;

    @TableField("change_summary")
    @Schema(description = "变更说明")
    private String changeSummary;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
