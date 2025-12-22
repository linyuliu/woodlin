package com.mumu.woodlin.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
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
@ConditionalOnProperty(prefix = "woodlin.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CorsConfig implements WebMvcConfigurer {
    
    private final CorsProperties corsProperties;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("正在配置CORS跨域设置...");
        
        CorsConfiguration config = new CorsConfiguration();
        
        // 配置允许的源
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedOrigins())) {
            // 如果配置了 allowedOrigins
            if (corsProperties.getAllowedOrigins().contains("*")) {
                // 使用通配符时，不能同时设置 allowCredentials 为 true
                config.addAllowedOrigin("*");
                config.setAllowCredentials(false);
                log.info("CORS配置: 允许所有源 (*)");
            } else {
                // 配置具体的域名
                corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);
                config.setAllowCredentials(corsProperties.getAllowCredentials());
                log.info("CORS配置: 允许的源 = {}", corsProperties.getAllowedOrigins());
            }
        } else if (!CollectionUtils.isEmpty(corsProperties.getAllowedOriginPatterns())) {
            // 如果配置了 allowedOriginPatterns，可以与 allowCredentials 一起使用
            corsProperties.getAllowedOriginPatterns().forEach(config::addAllowedOriginPattern);
            config.setAllowCredentials(corsProperties.getAllowCredentials());
            log.info("CORS配置: 允许的源模式 = {}", corsProperties.getAllowedOriginPatterns());
        } else {
            // 默认配置
            log.warn("CORS配置: 未配置允许的源，将不允许任何跨域请求");
            return;
        }
        
        // 配置允许的HTTP方法
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedMethods())) {
            corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
            log.info("CORS配置: 允许的HTTP方法 = {}", corsProperties.getAllowedMethods());
        }
        
        // 配置允许的请求头
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedHeaders())) {
            corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);
        }
        
        // 配置暴露的响应头
        if (!CollectionUtils.isEmpty(corsProperties.getExposedHeaders())) {
            corsProperties.getExposedHeaders().forEach(config::addExposedHeader);
        }
        
        // 配置预检请求的缓存时间
        if (corsProperties.getMaxAge() != null) {
            config.setMaxAge(corsProperties.getMaxAge());
        }
        
        // 应用CORS配置
        registry.addMapping(corsProperties.getPathPattern())
                .combine(config);
        
        log.info("CORS配置: 路径模式 = {}, 预检缓存时间 = {}秒", 
                corsProperties.getPathPattern(), corsProperties.getMaxAge());
        log.info("CORS跨域配置完成");
    }
}
