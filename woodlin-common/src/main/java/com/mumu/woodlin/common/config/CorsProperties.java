package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 跨域配置属性
 * 
 * @author mumu
 * @description CORS跨域资源共享配置属性，支持不同环境的灵活配置
 * @since 2025-01-10
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.cors")
public class CorsProperties {
    
    /**
     * 是否启用CORS跨域
     */
    private Boolean enabled = true;
    
    /**
     * 允许的源（Origin），支持通配符 *
     * dev和test环境建议使用 ["*"]
     * prod环境建议配置具体域名，如 ["https://example.com", "https://www.example.com"]
     */
    private List<String> allowedOrigins;
    
    /**
     * 允许的源模式（Origin Pattern），支持通配符
     * 例如：["https://*.example.com"]
     */
    private List<String> allowedOriginPatterns;
    
    /**
     * 允许的HTTP方法
     * 默认：GET, POST, PUT, DELETE, OPTIONS, PATCH
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    
    /**
     * 允许的请求头
     * 默认：*（所有请求头）
     */
    private List<String> allowedHeaders = List.of("*");
    
    /**
     * 暴露的响应头
     * 默认：Authorization, Content-Type
     */
    private List<String> exposedHeaders = List.of("Authorization", "Content-Type");
    
    /**
     * 是否允许携带凭证（cookies）
     * 注意：当allowedOrigins包含 * 时，此项必须为false
     */
    private Boolean allowCredentials = true;
    
    /**
     * 预检请求的缓存时间（秒）
     * 默认：3600秒（1小时）
     */
    private Long maxAge = 3600L;
    
    /**
     * CORS配置应用的路径模式
     * 默认：/**（所有路径）
     */
    private String pathPattern = "/**";
}
