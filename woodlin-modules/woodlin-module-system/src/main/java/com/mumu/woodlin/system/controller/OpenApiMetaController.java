package com.mumu.woodlin.system.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.authorization.entity.AuthOpenApiResource;
import com.mumu.woodlin.authorization.mapper.AuthorizationQueryMapper;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.util.OpenApiSecurityConstants;

/**
 * 开放接口元信息。
 *
 * @author mumu
 * @since 2026-06-03
 */
@RestController
@RequiredArgsConstructor
public class OpenApiMetaController {

    private final AuthorizationQueryMapper authorizationQueryMapper;

    /**
     * 健康检查。
     *
     * @return 响应
     */
    @GetMapping("/open/ping")
    public R<String> ping() {
        return R.ok("pong");
    }

    /**
     * 查询当前 App 已授权资源目录。
     *
     * @param request 请求
     * @return 资源目录
     */
    @GetMapping({"/open/meta/resources", "/openapi/meta/resources"})
    public R<List<AuthOpenApiResource>> authorizedResources(HttpServletRequest request) {
        Long appId = resolveAppId(request.getAttribute(OpenApiSecurityConstants.ATTR_APP_ID));
        String tenantId = (String) request.getAttribute(OpenApiSecurityConstants.ATTR_TENANT_ID);
        return R.ok(authorizationQueryMapper.selectAppAuthorizedResources(appId, tenantId));
    }

    private Long resolveAppId(Object raw) {
        if (raw instanceof Long value) {
            return value;
        }
        if (raw instanceof Number value) {
            return value.longValue();
        }
        if (raw instanceof String value && !value.isBlank()) {
            return Long.valueOf(value);
        }
        throw BusinessException.of(ResultCode.UNAUTHORIZED, "缺少开放应用身份");
    }
}
