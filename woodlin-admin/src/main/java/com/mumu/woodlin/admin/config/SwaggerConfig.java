package com.mumu.woodlin.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 文档配置
 * 
 * @author mumu
 * @description Swagger API 文档配置类，用于生成接口文档
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
                                .url("https://opensource.org/licenses/MIT")));
    }
}