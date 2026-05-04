package com.mumu.woodlin.assessment.model.entity;

import java.io.Serial;

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
 * 测评 Schema
 *
 * <p>对应表 sys_assessment_schema。同时保存三种形态：
 * <ul>
 *   <li>{@code canonical_schema}：后台可视化编辑的 JSON 规范 Schema（主存）</li>
 *   <li>{@code dsl_source}：DSL 源码（专家模式/导入导出用）</li>
 *   <li>{@code compiled_schema}：运行时执行用的编译后 Schema</li>
 * </ul>
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_assessment_schema")
@Schema(description = "测评 Schema（结构定义）")
public class AssessmentSchema extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "schema_id", type = IdType.ASSIGN_ID)
    @Schema(description = "Schema ID")
    private Long schemaId;

    @TableField("form_id")
    @Schema(description = "所属测评ID")
    private Long formId;

    @TableField("version_id")
    @Schema(description = "所属版本ID")
    private Long versionId;

    @TableField("status")
    @Schema(description = "Schema 状态: draft/compiled/published/archived")
    private String status;

    @TableField("canonical_schema")
    @Schema(description = "规范 JSON Schema（后台配置主存，LONGTEXT）")
    private String canonicalSchema;

    @TableField("dsl_source")
    @Schema(description = "DSL 源码（Kotlin DSL，LONGTEXT）")
    private String dslSource;

    @TableField("compiled_schema")
    @Schema(description = "编译后运行时 Schema（LONGTEXT）")
    private String compiledSchema;

    @TableField("compile_error")
    @Schema(description = "最近一次编译错误信息")
    private String compileError;

    @TableField("schema_hash")
    @Schema(description = "canonical_schema 内容哈希")
    private String schemaHash;

    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
}
