package com.mumu.woodlin.system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 系统模块自动装配。
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenApiSecurityProperties.class)
@ComponentScan(basePackages = "com.mumu.woodlin.system")
@MapperScan("com.mumu.woodlin.system.mapper")
public class SystemModuleAutoConfiguration {
}
