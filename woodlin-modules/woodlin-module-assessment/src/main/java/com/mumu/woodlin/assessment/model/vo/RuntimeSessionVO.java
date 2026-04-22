package com.mumu.woodlin.assessment.model.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 作答运行时会话信息
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "作答运行时会话信息")
public class RuntimeSessionVO {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "发布ID")
    private Long publishId;

    @Schema(description = "测评ID")
    private Long formId;

    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "会话状态")
    private String status;

    @Schema(description = "展示随机种子")
    private Long displaySeed;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "累计用时（秒）")
    private Integer elapsedSeconds;

    @Schema(description = "第几次作答")
    private Integer attemptNumber;

    @Schema(description = "当前章节编码")
    private String currentSectionCode;

    @Schema(description = "当前题目编码")
    private String currentItemCode;
}
