package com.mumu.woodlin.assessment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 启动作答会话入参
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "启动或续答会话入参")
public class StartSessionDTO {

    @NotNull(message = "发布ID不能为空")
    @Schema(description = "发布ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long publishId;

    @Schema(description = "匿名标识")
    private String anonymousToken;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "设备类型")
    private String deviceType;
}
