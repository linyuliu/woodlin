package com.mumu.woodlin.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mumu.woodlin.security.interceptor.UserActivityInterceptor;

/**
 * Sa-Token 安全框架配置
 *
 * @author mumu
 * @description Sa-Token安全框架的全局配置，包括拦截器设置和路由规则配置
 *              Sa-Token配置通过application.yml中的sa-token配置项管理
 * @since 2025-01-01
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfiguration implements WebMvcConfigurer {

    private final UserActivityInterceptor userActivityInterceptor;

    /**
     * API 文档相关路径（无需认证和监控）
     */
    private static final String[] DOC_PATHS = {
        "/doc.html",            // Knife4j 接口文档
        "/swagger-ui.html",     // Swagger UI HTML
        "/swagger-ui/**",       // Swagger UI 资源
        "/swagger-resources/**",// Swagger 资源
        "/v3/api-docs/**",      // OpenAPI 文档
        "/webjars/**",          // 静态资源
        "/favicon.ico"          // 网站图标
    };

    /**
     * 认证相关路径（无需认证和监控）
     */
    private static final String[] AUTH_PATHS = {
        "/auth/login",
        "/auth/logout",
        "/auth/captcha",
        "/auth/register",
        "/auth/forgot-password",
        "/auth/dev-token",
        "/admin/**"
    };

    /**
     * 系统路径（无需认证和监控）
     */
    private static final String[] SYSTEM_PATHS = {
        "/error",
        "/actuator/**"
    };



    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 构建排除路径列表
        String[] excludePatterns = buildExcludePatterns();

        // 注册用户活动监控拦截器
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePatterns);

        // 注册 Sa-Token 拦截器，定义详细的鉴权规则
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 指定一条 match 规则
            SaRouter
                    .match("/**") // 拦截的 path 列表，可以写多个
                    .notMatch(excludePatterns) // 排除不需要鉴权的路径
                    .check(r -> StpUtil.checkLogin()); // 执行鉴权操作
        })).addPathPatterns("/**");
    }

    /**
     * 构建排除路径列表
     *
     * @return 排除路径数组
     */
    private String[] buildExcludePatterns() {
        int totalLength = AUTH_PATHS.length + DOC_PATHS.length + SYSTEM_PATHS.length;
        String[] excludePatterns = new String[totalLength];

        int index = 0;
        System.arraycopy(AUTH_PATHS, 0, excludePatterns, index, AUTH_PATHS.length);
        index += AUTH_PATHS.length;

        System.arraycopy(DOC_PATHS, 0, excludePatterns, index, DOC_PATHS.length);
        index += DOC_PATHS.length;

        System.arraycopy(SYSTEM_PATHS, 0, excludePatterns, index, SYSTEM_PATHS.length);

        return excludePatterns;
    }
}
