package com.mumu.woodlin.admin.config;

import com.mumu.woodlin.admin.interceptor.PageValidationInterceptor;
import lombok.RequiredArgsConstructor;
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
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册分页参数校验拦截器
        registry.addInterceptor(pageValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/druid/**",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/actuator/**"
                );
    }
}