package com.mumu.woodlin.assessment.model.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 作答运行时发布信息
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "作答运行时发布信息")
public class RuntimePublishVO {

    @Schema(description = "发布ID")
    private Long publishId;

    @Schema(description = "测评ID")
    private Long formId;

    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "发布编码")
    private String publishCode;

    @Schema(description = "发布名称")
    private String publishName;

    @Schema(description = "发布状态")
    private String status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "总时限（分钟）")
    private Integer timeLimitMinutes;

    @Schema(description = "最大作答次数")
    private Integer maxAttempts;

    @Schema(description = "是否允许匿名作答")
    private Boolean allowAnonymous;

    @Schema(description = "是否允许断点续答")
    private Boolean allowResume;

    @Schema(description = "随机化策略")
    private String randomStrategy;

    @Schema(description = "是否立即展示结果")
    private Boolean showResultImmediately;
}
