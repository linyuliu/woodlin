package com.mumu.woodlin.authorization.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 授权资源。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@Schema(description = "授权资源")
public class AuthorizationResource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "资源类型，例如 api/menu/data")
    private String type;

    @Schema(description = "资源ID或路径")
    private String id;

    @Schema(description = "资源属性")
    private Map<String, Object> attributes = new LinkedHashMap<>();
}
