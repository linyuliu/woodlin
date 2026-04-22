package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 题目维度映射 DTO
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "题目维度映射 DTO")
public class SchemaDimensionBindingDTO {

    @Schema(description = "维度编码")
    private String dimensionCode;

    @Schema(description = "权重")
    private BigDecimal weight;

    @Schema(description = "计分模式覆盖")
    private String scoreMode;

    @Schema(description = "反向模式覆盖")
    private String reverseMode;
}
