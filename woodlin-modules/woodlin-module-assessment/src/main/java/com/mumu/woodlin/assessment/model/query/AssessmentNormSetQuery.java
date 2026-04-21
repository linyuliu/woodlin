package com.mumu.woodlin.assessment.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 常模集分页查询条件
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Schema(description = "常模集查询条件")
public class AssessmentNormSetQuery {

    @Schema(description = "所属测评ID")
    private Long formId;

    @Schema(description = "常模集名称（模糊）")
    private String normSetName;

    @Schema(description = "常模集编码")
    private String normSetCode;

    @Schema(description = "状态: 1=启用 0=停用")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
