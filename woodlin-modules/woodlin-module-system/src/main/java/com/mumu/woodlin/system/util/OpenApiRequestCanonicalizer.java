package com.mumu.woodlin.system.util;

import com.mumu.woodlin.common.openapi.OpenApiSecurityKit;
import com.mumu.woodlin.common.openapi.model.OpenApiRequestContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * 开放 API 请求规范化工具。
 *
 * @author mumu
 * @since 2026-04-13
 */
public final class OpenApiRequestCanonicalizer {

    private OpenApiRequestCanonicalizer() {
    }

    /**
     * 构建规范签名串。
     *
     * @param request   请求
     * @param body      请求体
     * @param timestamp 时间戳
     * @param nonce     随机串
     * @param tenantId  租户
     * @param accessKey access key
     * @return 规范签名串
     */
    public static String canonicalize(HttpServletRequest request, byte[] body, String timestamp, String nonce,
                                      String tenantId, String accessKey) {
        OpenApiRequestContext context = new OpenApiRequestContext()
            .setMethod(request.getMethod())
            .setPath(request.getRequestURI())
            .setQueryParameters(copyParameters(request))
            .setBody(body)
            .setTimestamp(timestamp)
            .setNonce(nonce)
            .setTenantId(tenantId)
            .setAccessKey(accessKey);
        return OpenApiSecurityKit.buildCanonicalRequest(context);
    }

    private static Map<String, List<String>> copyParameters(HttpServletRequest request) {
        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            List<String> values = new ArrayList<String>();
            if (entry.getValue() != null) {
                Collections.addAll(values, entry.getValue());
            }
            params.put(entry.getKey(), values);
        }
        return params;
    }
}
