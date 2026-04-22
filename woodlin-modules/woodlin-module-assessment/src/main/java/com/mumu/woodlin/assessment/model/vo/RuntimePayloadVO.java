package com.mumu.woodlin.assessment.model.vo;

import com.mumu.woodlin.assessment.model.dto.AnswerItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 作答运行时完整载荷
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "作答运行时完整载荷")
public class RuntimePayloadVO {

    @Schema(description = "发布信息")
    private RuntimePublishVO publish;

    @Schema(description = "会话信息")
    private RuntimeSessionVO session;

    @Schema(description = "章节列表")
    private List<RuntimeSectionVO> sections;

    @Schema(description = "总题数")
    private int totalItems;

    @Schema(description = "断点续答答案快照")
    private Map<String, AnswerItemDTO> answerSnapshot;
}
