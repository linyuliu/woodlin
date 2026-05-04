package com.mumu.woodlin.assessment.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测评版本分页查询条件
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Schema(description = "测评版本查询条件")
public class AssessmentFormVersionQuery {

    @Schema(description = "所属测评ID")
    private Long formId;

    @Schema(description = "版本号（模糊）")
    private String versionNo;

    @Schema(description = "版本状态: draft/compiled/published/archived")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
