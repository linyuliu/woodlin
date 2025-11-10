package com.mumu.woodlin.admin.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate配置
 *
 * @author mumu
 * @description 配置HTTP客户端RestTemplate，用于发起HTTP请求
 * @since 2025-01-10
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置RestTemplate Bean
     *
     * @param builder RestTemplate构建器
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
