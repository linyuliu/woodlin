package com.mumu.woodlin.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置
 * 
 * @author mumu
 * @description CORS跨域资源共享配置，支持不同环境的灵活配置
 *              - dev/test环境：使用通配符*，方便开发调试
 *              - prod环境：配置具体域名，保证安全性
 * @since 2025-01-10
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = "woodlin.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CorsConfig implements WebMvcConfigurer {
    
    private final CorsProperties corsProperties;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("正在配置CORS跨域设置...");
        
        // 配置允许的源
        boolean hasAllowedOrigins = !CollectionUtils.isEmpty(corsProperties.getAllowedOrigins());
        boolean hasAllowedOriginPatterns = !CollectionUtils.isEmpty(corsProperties.getAllowedOriginPatterns());
        
        if (!hasAllowedOrigins && !hasAllowedOriginPatterns) {
            log.warn("CORS配置: 未配置允许的源，将不允许任何跨域请求");
            return;
        }
        
        // 创建 CORS 注册
        CorsRegistration registration = registry.addMapping(corsProperties.getPathPattern());
        
        // 配置允许的源
        if (hasAllowedOrigins) {
            if (corsProperties.getAllowedOrigins().contains("*")) {
                // 使用通配符时，不能同时设置 allowCredentials 为 true
                registration.allowedOrigins("*");
                registration.allowCredentials(false);
                log.info("CORS配置: 允许所有源 (*)");
            } else {
                // 配置具体的域名
                registration.allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]));
                registration.allowCredentials(corsProperties.getAllowCredentials());
                log.info("CORS配置: 允许的源 = {}", corsProperties.getAllowedOrigins());
            }
        } else if (hasAllowedOriginPatterns) {
            // 如果配置了 allowedOriginPatterns，可以与 allowCredentials 一起使用
            registration.allowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(new String[0]));
            registration.allowCredentials(corsProperties.getAllowCredentials());
            log.info("CORS配置: 允许的源模式 = {}", corsProperties.getAllowedOriginPatterns());
        }
        
        // 配置允许的HTTP方法
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedMethods())) {
            registration.allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]));
            log.info("CORS配置: 允许的HTTP方法 = {}", corsProperties.getAllowedMethods());
        }
        
        // 配置允许的请求头
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedHeaders())) {
            registration.allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]));
        }
        
        // 配置暴露的响应头
        if (!CollectionUtils.isEmpty(corsProperties.getExposedHeaders())) {
            registration.exposedHeaders(corsProperties.getExposedHeaders().toArray(new String[0]));
        }
        
        // 配置预检请求的缓存时间
        if (corsProperties.getMaxAge() != null) {
            registration.maxAge(corsProperties.getMaxAge());
        }
        
        log.info("CORS配置: 路径模式 = {}, 预检缓存时间 = {}秒", 
                corsProperties.getPathPattern(), corsProperties.getMaxAge());
        log.info("CORS跨域配置完成");
    }
}
