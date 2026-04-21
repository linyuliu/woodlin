package com.mumu.woodlin.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.system.filter.OpenApiSecurityFilter;
import com.mumu.woodlin.system.service.IOpenApiSecurityService;
import com.mumu.woodlin.system.service.ISysOpenAppCredentialService;
import com.mumu.woodlin.system.service.ISysOpenAppService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 开放 API 安全过滤器配置。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Configuration
public class OpenApiSecurityConfiguration {

    /**
     * 注册开放 API 安全过滤器。
     *
     * @param properties             配置
     * @param objectMapper           ObjectMapper
     * @param openApiSecurityService 运行时服务
     * @param openAppService         应用服务
     * @param credentialService      凭证服务
     * @param redissonClient         Redisson
     * @return 过滤器注册
     */
    @Bean
    public FilterRegistrationBean<OpenApiSecurityFilter> openApiSecurityFilterRegistration(
        OpenApiSecurityProperties properties,
        ObjectMapper objectMapper,
        IOpenApiSecurityService openApiSecurityService,
        ISysOpenAppService openAppService,
        ISysOpenAppCredentialService credentialService,
        RedissonClient redissonClient) {
        FilterRegistrationBean<OpenApiSecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OpenApiSecurityFilter(
            properties, objectMapper, openApiSecurityService, openAppService, credentialService, redissonClient
        ));
        registration.addUrlPatterns("/openapi/*");
        registration.setName("openApiSecurityFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }
}
