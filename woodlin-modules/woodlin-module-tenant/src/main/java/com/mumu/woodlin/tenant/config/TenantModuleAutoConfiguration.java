package com.mumu.woodlin.tenant.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 租户模块自动装配。
 */
@AutoConfiguration
@ComponentScan(basePackages = {
    "com.mumu.woodlin.tenant.controller",
    "com.mumu.woodlin.tenant.service"
})
@MapperScan("com.mumu.woodlin.tenant.mapper")
public class TenantModuleAutoConfiguration {
}
