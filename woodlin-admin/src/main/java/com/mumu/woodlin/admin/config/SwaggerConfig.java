package com.mumu.woodlin.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 * 
 * @author mumu
 * @description Knife4j API 文档配置类，用于生成接口文档
 * @since 2025-01-01
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * 创建 OpenAPI 配置
     * 
     * @return OpenAPI 配置
     */
    @Bean
    public OpenAPI woodlinOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Woodlin 多租户中后台管理系统 API")
                        .description("高质量多租户中后台管理系统框架 - 注重设计与代码细节")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("mumu")
                                .email("mumu@woodlin.com")
                                .url("https://github.com/linyuliu/woodlin"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("Authorization", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("请在此处输入 Token，格式为：Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }

    /**
     * 系统管理模块API分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1-系统管理")
                .pathsToMatch("/system/**")
                .build();
    }

    /**
     * 租户管理模块API分组
     */
    @Bean
    public GroupedOpenApi tenantApi() {
        return GroupedOpenApi.builder()
                .group("2-租户管理")
                .pathsToMatch("/tenant/**")
                .build();
    }

    /**
     * 文件管理模块API分组
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("3-文件管理")
                .pathsToMatch("/file/**")
                .build();
    }

    /**
     * 任务调度模块API分组
     */
    @Bean
    public GroupedOpenApi taskApi() {
        return GroupedOpenApi.builder()
                .group("4-任务调度")
                .pathsToMatch("/task/**")
                .build();
    }

    /**
     * 代码生成模块API分组
     */
    @Bean
    public GroupedOpenApi generatorApi() {
        return GroupedOpenApi.builder()
                .group("5-代码生成")
                .pathsToMatch("/generator/**")
                .build();
    }

    /**
     * SQL2API模块API分组
     */
    @Bean
    public GroupedOpenApi sql2apiApi() {
        return GroupedOpenApi.builder()
                .group("6-SQL2API")
                .pathsToMatch("/sql2api/**")
                .build();
    }

    /**
     * 认证授权模块API分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("7-认证授权")
                .pathsToMatch("/auth/**", "/login/**", "/logout/**")
                .build();
    }

    /**
     * 所有API分组
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0-全部接口")
                .pathsToMatch("/**")
                .pathsToExclude("/error", "/actuator/**")
                .build();
    }
}