package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 测评结构化 Schema 聚合对象
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "测评结构化 Schema 聚合对象")
public class AssessmentSchemaAggregateDTO {

    @Schema(description = "Schema ID")
    private Long schemaId;

    @Schema(description = "测评ID")
    private Long formId;

    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "结构状态")
    private String status;

    @Schema(description = "测评类型: scale/exam/survey")
    private String assessmentType;

    @Schema(description = "全局随机策略")
    private String randomStrategy;

    @Schema(description = "结构说明")
    private String description;

    @Schema(description = "上下文字段预留位")
    private Map<String, Object> contextFields;

    @Schema(description = "章节列表")
    private List<SchemaSectionDTO> sections = new ArrayList<>();

    @Schema(description = "维度列表")
    private List<SchemaDimensionDTO> dimensions = new ArrayList<>();

    @Schema(description = "规则列表")
    private List<SchemaRuleDTO> rules = new ArrayList<>();

    @Schema(description = "专家模式 DSL/JSON 源码")
    private String dslSource;

    @Schema(description = "最近一次编译结果")
    private String compiledSchema;

    @Schema(description = "最近一次编译错误")
    private String compileError;

    @Schema(description = "canonical_schema 哈希")
    private String schemaHash;

    @Schema(description = "dsl_source 哈希")
    private String dslHash;
}
