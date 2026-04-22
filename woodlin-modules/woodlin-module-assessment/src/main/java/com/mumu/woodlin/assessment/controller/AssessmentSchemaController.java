package com.mumu.woodlin.assessment.controller;

import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaAggregateDTO;
import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaCompileResultDTO;
import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaDslDTO;
import com.mumu.woodlin.assessment.service.IAssessmentSchemaService;
import com.mumu.woodlin.assessment.validation.ValidationReport;
import com.mumu.woodlin.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 测评 Schema 聚合接口
 *
 * @author mumu
 * @since 2026-04-22
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/assessment/schema")
@Tag(name = "测评 Schema", description = "测评结构化 Schema 聚合接口")
public class AssessmentSchemaController {

    private final IAssessmentSchemaService schemaService;

    @GetMapping("/version/{versionId}")
    @Operation(summary = "获取版本结构 Schema")
    public R<AssessmentSchemaAggregateDTO> getAggregate(
        @Parameter(description = "版本ID", required = true) @PathVariable Long versionId) {
        return R.ok(schemaService.getAggregate(versionId));
    }

    @PutMapping("/version/{versionId}")
    @Operation(summary = "保存版本结构 Schema")
    public R<AssessmentSchemaAggregateDTO> saveAggregate(
        @Parameter(description = "版本ID", required = true) @PathVariable Long versionId,
        @RequestBody AssessmentSchemaAggregateDTO aggregate) {
        return R.ok(schemaService.saveAggregate(versionId, aggregate));
    }

    @PostMapping("/version/{versionId}/compile")
    @Operation(summary = "编译版本结构 Schema")
    public R<AssessmentSchemaCompileResultDTO> compile(
        @Parameter(description = "版本ID", required = true) @PathVariable Long versionId) {
        return R.ok(schemaService.compileVersion(versionId));
    }

    @PostMapping("/version/{versionId}/validate")
    @Operation(summary = "校验版本结构 Schema")
    public R<ValidationReport> validate(
        @Parameter(description = "版本ID", required = true) @PathVariable Long versionId) {
        return R.ok(schemaService.validateVersion(versionId));
    }

    @PostMapping("/version/{versionId}/import-dsl")
    @Operation(summary = "导入专家模式 DSL/JSON")
    public R<AssessmentSchemaAggregateDTO> importDsl(
        @Parameter(description = "版本ID", required = true) @PathVariable Long versionId,
        @RequestBody AssessmentSchemaDslDTO dto) {
        return R.ok(schemaService.importDsl(versionId, dto.getDslSource()));
    }

    @GetMapping("/version/{versionId}/export-dsl")
    @Operation(summary = "导出专家模式 DSL/JSON")
    public R<AssessmentSchemaDslDTO> exportDsl(
        @Parameter(description = "版本ID", required = true) @PathVariable Long versionId) {
        return R.ok(new AssessmentSchemaDslDTO().setDslSource(schemaService.exportDsl(versionId)));
    }
}
