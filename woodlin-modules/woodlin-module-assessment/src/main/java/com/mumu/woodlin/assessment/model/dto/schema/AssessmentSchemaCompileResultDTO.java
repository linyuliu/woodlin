package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Schema 编译结果
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "Schema 编译结果")
public class AssessmentSchemaCompileResultDTO {

    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "Schema ID")
    private Long schemaId;

    @Schema(description = "编译后状态")
    private String status;

    @Schema(description = "schema_hash")
    private String schemaHash;

    @Schema(description = "dsl_hash")
    private String dslHash;

    @Schema(description = "编译后结构")
    private String compiledSchema;

    @Schema(description = "编译错误")
    private String compileError;
}
