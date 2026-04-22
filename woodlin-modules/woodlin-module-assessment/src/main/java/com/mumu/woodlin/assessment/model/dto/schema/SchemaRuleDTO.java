package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 规则 DTO
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "规则 DTO")
public class SchemaRuleDTO {

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "目标类型")
    private String targetType;

    @Schema(description = "目标编码")
    private String targetCode;

    @Schema(description = "规则源码")
    private String dslSource;

    @Schema(description = "编译结果")
    private String compiledRule;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "是否启用")
    private Boolean isActive;
}
