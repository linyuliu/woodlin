package com.mumu.woodlin.authorization.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 授权上下文。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@Schema(description = "授权上下文")
public class AuthorizationContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "客户端IP")
    private String ip;

    @Schema(description = "请求时间")
    private LocalDateTime time;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "开放应用ID")
    private Long appId;

    @Schema(description = "请求头")
    private Map<String, String> headers = new LinkedHashMap<>();

    @Schema(description = "额外上下文")
    private Map<String, Object> attributes = new LinkedHashMap<>();
}
