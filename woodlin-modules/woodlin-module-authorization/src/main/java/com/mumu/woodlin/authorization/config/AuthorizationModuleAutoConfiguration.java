package com.mumu.woodlin.authorization.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 授权模块自动装配。
 *
 * @author mumu
 * @since 2026-06-02
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.mumu.woodlin.authorization")
@MapperScan("com.mumu.woodlin.authorization.mapper")
public class AuthorizationModuleAutoConfiguration {
}
