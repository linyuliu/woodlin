package com.mumu.woodlin.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Druid 数据源配置
 * 
 * @author mumu
 * @description Druid连接池配置，主要通过application.yml进行配置
 * @since 2025-01-01
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.dynamic.datasource.master.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
public class DruidConfiguration {
    
    // Druid 配置主要通过 application.yml 文件进行
    // 这里可以添加自定义的 Druid 监控和过滤器配置
    
}