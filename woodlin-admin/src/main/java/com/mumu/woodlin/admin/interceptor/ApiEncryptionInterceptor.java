package com.mumu.woodlin.admin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.mumu.woodlin.common.config.ApiEncryptionProperties;
import com.mumu.woodlin.common.util.ApiEncryptionUtil;

/**
 * API 加密拦截器
 *
 * @author mumu
 * @description 拦截需要加密的接口，对请求和响应数据进行加密/解密处理
 * @since 2025-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "woodlin.api.encryption", name = "enabled", havingValue = "true")
public class ApiEncryptionInterceptor implements HandlerInterceptor {

    private final ApiEncryptionProperties encryptionProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否需要对该接口进行加密处理
        if (!shouldEncrypt(request.getRequestURI())) {
            return true;
        }

        // 如果启用了请求加密，解密请求数据
        if (Boolean.TRUE.equals(encryptionProperties.getEncryptRequest())) {
            decryptRequest(request);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 检查是否需要对该接口进行加密处理
        if (!shouldEncrypt(request.getRequestURI())) {
            return;
        }

        // 如果启用了响应加密，加密响应数据
        if (Boolean.TRUE.equals(encryptionProperties.getEncryptResponse())) {
            encryptResponse(response);
        }
    }

    /**
     * 判断是否需要对该接口进行加密
     *
     * @param requestUri 请求URI
     * @return 是否需要加密
     */
    private boolean shouldEncrypt(String requestUri) {
        // 如果未配置任何加密规则，则不加密
        if (encryptionProperties.getIncludePatterns().isEmpty()) {
            return false;
        }

        // 检查是否在排除列表中
        for (String excludePattern : encryptionProperties.getExcludePatterns()) {
            if (pathMatcher.match(excludePattern, requestUri)) {
                log.debug("接口 {} 在加密排除列表中", requestUri);
                return false;
            }
        }

        // 检查是否在包含列表中
        for (String includePattern : encryptionProperties.getIncludePatterns()) {
            if (pathMatcher.match(includePattern, requestUri)) {
                log.debug("接口 {} 需要加密处理", requestUri);
                return true;
            }
        }

        return false;
    }

    /**
     * 解密请求数据
     *
     * @param request 请求对象
     */
    private void decryptRequest(HttpServletRequest request) throws Exception {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            log.warn("请求未被 ContentCachingRequestWrapper 包装，无法解密");
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;
        byte[] content = wrappedRequest.getContentAsByteArray();

        if (content.length == 0) {
            return;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        if (StrUtil.isBlank(body)) {
            return;
        }

        try {
            // 解析请求体，获取加密数据字段
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.readValue(body, Map.class);
            Object encryptedData = bodyMap.get(encryptionProperties.getEncryptedFieldName());

            if (encryptedData != null && encryptedData instanceof String) {
                // 解密数据
                String decryptedData = ApiEncryptionUtil.decrypt((String) encryptedData, encryptionProperties);
                log.debug("请求数据解密成功");

                // 将解密后的数据重新设置到请求体中
                // 注意：这里需要自定义 Request Wrapper 来支持修改请求体
                // 由于 HttpServletRequest 的 getInputStream 只能读取一次，需要特殊处理
            }
        } catch (Exception e) {
            log.error("请求数据解密失败", e);
            throw e;
        }
    }

    /**
     * 加密响应数据
     *
     * @param response 响应对象
     */
    private void encryptResponse(HttpServletResponse response) throws Exception {
        if (!(response instanceof ContentCachingResponseWrapper)) {
            log.warn("响应未被 ContentCachingResponseWrapper 包装，无法加密");
            return;
        }

        ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;
        byte[] content = wrappedResponse.getContentAsByteArray();

        if (content.length == 0) {
            return;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        if (StrUtil.isBlank(body)) {
            return;
        }

        try {
            // 加密响应数据
            String encryptedData = ApiEncryptionUtil.encrypt(body, encryptionProperties);
            log.debug("响应数据加密成功");

            // 如果配置了添加加密标识头
            if (Boolean.TRUE.equals(encryptionProperties.getAddEncryptionHeader())) {
                response.setHeader(encryptionProperties.getEncryptionHeaderName(), "true");
            }

            // 清空原响应内容并写入加密后的数据
            wrappedResponse.resetBuffer();
            wrappedResponse.getOutputStream().write(encryptedData.getBytes(StandardCharsets.UTF_8));
            wrappedResponse.copyBodyToResponse();
        } catch (Exception e) {
            log.error("响应数据加密失败", e);
            throw e;
        }
    }
}
