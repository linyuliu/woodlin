package com.mumu.woodlin.authorization.model;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 授权主体。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@Schema(description = "授权主体")
public class AuthorizationSubject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主体类型，例如 user/app")
    private String type;

    @Schema(description = "主体ID")
    private String id;
}
