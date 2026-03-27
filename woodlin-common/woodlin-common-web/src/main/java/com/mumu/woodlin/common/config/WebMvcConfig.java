package com.mumu.woodlin.common.config;

import com.mumu.woodlin.common.interceptor.ResponseFieldInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 
 * @author mumu
 * @description 注册响应字段控制拦截器
 * @since 2025-01-10
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final ResponseFieldInterceptor responseFieldInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册响应字段控制拦截器
        registry.addInterceptor(responseFieldInterceptor)
                .addPathPatterns("/**");
    }
}
