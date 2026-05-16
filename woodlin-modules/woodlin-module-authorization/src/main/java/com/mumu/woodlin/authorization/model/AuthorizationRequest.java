package com.mumu.woodlin.authorization.model;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 授权决策请求。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@Schema(description = "授权决策请求")
public class AuthorizationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull(message = "授权主体不能为空")
    @Schema(description = "授权主体")
    private AuthorizationSubject subject;

    @NotBlank(message = "授权动作不能为空")
    @Schema(description = "授权动作")
    private String action;

    @Valid
    @NotNull(message = "授权资源不能为空")
    @Schema(description = "授权资源")
    private AuthorizationResource resource;

    @Valid
    @Schema(description = "授权上下文")
    private AuthorizationContext context = new AuthorizationContext();
}
