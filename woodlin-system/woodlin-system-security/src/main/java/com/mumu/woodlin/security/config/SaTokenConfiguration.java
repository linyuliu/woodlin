package com.mumu.woodlin.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.mumu.woodlin.security.interceptor.UserActivityInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 安全框架配置
 *
 * @author mumu
 * @description Sa-Token安全框架的全局配置，包括拦截器设置和路由规则配置
 *              核心配置（token名称、有效期等）通过application.yml中的sa-token配置项管理
 *              Sa-Token自动从application.yml读取配置，无需手动创建SaTokenConfig Bean
 * @since 2025-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaTokenConfiguration implements WebMvcConfigurer {

    private final UserActivityInterceptor userActivityInterceptor;

    /**
     * 配置 Sa-Token 拦截器和用户活动监控拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] excludePatterns = buildExcludePatterns();
        
        log.info("Sa-Token拦截器配置: 排除路径数量={}", excludePatterns.length);

        // 注册用户活动监控拦截器
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePatterns);

        // 注册 Sa-Token 拦截器，开启注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(excludePatterns);
    }

    /**
     * 构建无需认证的路径列表
     *
     * @return 排除路径数组
     */
    private String[] buildExcludePatterns() {
        List<String> patterns = new ArrayList<>();
        
        // 认证相关路径
        patterns.addAll(Arrays.asList(
                "/auth/login",
                "/auth/logout",
                "/auth/captcha",
                "/auth/register",
                "/auth/forgot-password",
                "/auth/dev-token"
        ));
        
        // API文档路径
        patterns.addAll(Arrays.asList(
                "/doc.html",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**",
                "/favicon.ico"
        ));
        
        // 系统路径
        patterns.addAll(Arrays.asList(
                "/error",
                "/actuator/**"
        ));
        
        return patterns.toArray(new String[0]);
    }
}
