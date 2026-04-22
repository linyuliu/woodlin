package com.mumu.woodlin.assessment.model.dto.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节 DTO
 *
 * @author mumu
 * @since 2026-04-22
 */
@Data
@Accessors(chain = true)
@Schema(description = "章节 DTO")
public class SchemaSectionDTO {

    @Schema(description = "章节编码")
    private String sectionCode;

    @Schema(description = "章节标题")
    private String sectionTitle;

    @Schema(description = "章节说明")
    private String sectionDesc;

    @Schema(description = "展示模式")
    private String displayMode;

    @Schema(description = "随机策略")
    private String randomStrategy;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否必做章节")
    private Boolean isRequired;

    @Schema(description = "锚点编码")
    private String anchorCode;

    @Schema(description = "题目列表")
    private List<SchemaItemDTO> items = new ArrayList<>();
}
