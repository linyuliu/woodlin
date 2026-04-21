package com.mumu.woodlin.assessment.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发布实例分页查询条件
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Schema(description = "发布实例查询条件")
public class AssessmentPublishQuery {

    @Schema(description = "所属测评ID")
    private Long formId;

    @Schema(description = "发布名称（模糊）")
    private String publishName;

    @Schema(description = "发布编码")
    private String publishCode;

    @Schema(description = "发布状态: draft/under_review/published/paused/closed/archived")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
