package com.mumu.woodlin.common.interceptor;

import com.mumu.woodlin.common.config.ResponseProperties;
import com.mumu.woodlin.common.response.ResponseFieldContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.Set;

/**
 * 响应字段控制拦截器
 * 
 * @author mumu
 * @description 解析请求头中的响应字段控制指令，设置到上下文中
 * @since 2025-01-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseFieldInterceptor implements HandlerInterceptor {
    
    private final ResponseProperties responseProperties;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果未启用请求级别控制，直接返回
        if (!responseProperties.getEnableRequestControl()) {
            return true;
        }
        
        // 获取请求头
        String headerValue = request.getHeader(responseProperties.getRequestHeaderName());
        
        if (headerValue != null && !headerValue.trim().isEmpty()) {
            parseAndSetContext(headerValue);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 清除上下文，避免内存泄漏
        ResponseFieldContext.clear();
    }
    
    /**
     * 解析请求头值并设置上下文
     * 
     * 支持的格式：
     * - "code,data,message" - 只包含这些字段
     * - "-timestamp,-requestId" - 排除这些字段
     * - "code,data,-timestamp" - 包含code和data，但如果有其他字段也排除timestamp
     * 
     * @param headerValue 请求头值
     */
    private void parseAndSetContext(String headerValue) {
        try {
            Set<String> includeFields = new HashSet<>();
            Set<String> excludeFields = new HashSet<>();
            
            String[] fields = headerValue.split(",");
            for (String field : fields) {
                field = field.trim();
                if (field.startsWith("-")) {
                    // 排除字段
                    excludeFields.add(field.substring(1));
                } else if (!field.isEmpty()) {
                    // 包含字段
                    includeFields.add(field);
                }
            }
            
            if (!includeFields.isEmpty()) {
                ResponseFieldContext.setIncludeFields(includeFields);
                log.debug("设置响应包含字段: {}", includeFields);
            }
            
            if (!excludeFields.isEmpty()) {
                ResponseFieldContext.setExcludeFields(excludeFields);
                log.debug("设置响应排除字段: {}", excludeFields);
            }
            
        } catch (Exception e) {
            log.warn("解析响应字段控制头失败: {}", headerValue, e);
            // 解析失败时清除上下文，使用默认配置
            ResponseFieldContext.clear();
        }
    }
}
