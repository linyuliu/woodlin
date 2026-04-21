package com.mumu.woodlin.assessment.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测评主体分页查询条件
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Schema(description = "测评主体查询条件")
public class AssessmentFormQuery {

    @Schema(description = "测评名称（模糊）")
    private String formName;

    @Schema(description = "唯一编码（精确）")
    private String formCode;

    @Schema(description = "测评类型: scale/exam/survey")
    private String assessmentType;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "启用状态: 1=启用 0=禁用")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
