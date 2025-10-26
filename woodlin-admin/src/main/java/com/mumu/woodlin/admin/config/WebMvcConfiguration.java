package com.mumu.woodlin.admin.config;

import com.mumu.woodlin.admin.interceptor.ApiEncryptionInterceptor;
import com.mumu.woodlin.admin.interceptor.PageValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 
 * @author mumu
 * @description Web层配置，包括拦截器注册
 * @since 2025-01-01
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {
    
    private final PageValidationInterceptor pageValidationInterceptor;
    
    @Autowired(required = false)
    private ApiEncryptionInterceptor apiEncryptionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册分页参数校验拦截器
        registry.addInterceptor(pageValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/druid/**",
                        // Knife4j 文档相关路径
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico",
                        // 健康检查
                        "/actuator/**"
                );
        
        // 注册API加密拦截器（如果启用）
        if (apiEncryptionInterceptor != null) {
            registry.addInterceptor(apiEncryptionInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/error",
                            "/druid/**",
                            // Knife4j 文档相关路径
                            "/doc.html",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/v3/api-docs/**",
                            "/webjars/**",
                            "/favicon.ico",
                            // 健康检查
                            "/actuator/**"
                    );
        }
    }
}