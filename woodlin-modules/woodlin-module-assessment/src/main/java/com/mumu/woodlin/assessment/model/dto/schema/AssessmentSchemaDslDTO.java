package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Schema DSL 传输对象
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "Schema DSL 传输对象")
public class AssessmentSchemaDslDTO {

    @Schema(description = "DSL/JSON 源码")
    private String dslSource;
}
