package com.mumu.woodlin.system.model;

import com.mumu.woodlin.common.openapi.enums.ApiSecurityMode;
import com.mumu.woodlin.system.entity.SysOpenApp;
import com.mumu.woodlin.system.entity.SysOpenAppCredential;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开放 API 认证结果。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Data
@Accessors(chain = true)
public class OpenApiAuthenticationResult {

    /**
     * 开放应用。
     */
    private SysOpenApp app;

    /**
     * 开放应用凭证。
     */
    private SysOpenAppCredential credential;

    /**
     * 生效安全模式。
     */
    private ApiSecurityMode securityMode;

    /**
     * 运行时租户。
     */
    private String tenantId;

    /**
     * 请求 ID。
     */
    private String requestId;

    /**
     * 主体附加属性。
     */
    private Map<String, Object> principalAttributes = new LinkedHashMap<>();
}
