package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度 DTO
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "维度 DTO")
public class SchemaDimensionDTO {

    @Schema(description = "维度编码")
    private String dimensionCode;

    @Schema(description = "维度名称")
    private String dimensionName;

    @Schema(description = "维度说明")
    private String dimensionDesc;

    @Schema(description = "父维度编码")
    private String parentDimensionCode;

    @Schema(description = "计分模式")
    private String scoreMode;

    @Schema(description = "自定义 DSL")
    private String scoreDsl;

    @Schema(description = "常模集ID")
    private Long normSetId;

    @Schema(description = "排序值")
    private Integer sortOrder;
}
