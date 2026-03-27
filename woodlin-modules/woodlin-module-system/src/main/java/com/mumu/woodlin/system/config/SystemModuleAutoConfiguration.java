package com.mumu.woodlin.system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 系统模块自动装配。
 */
@AutoConfiguration
@ComponentScan(basePackages = {
    "com.mumu.woodlin.system.controller",
    "com.mumu.woodlin.system.service"
})
@MapperScan("com.mumu.woodlin.system.mapper")
public class SystemModuleAutoConfiguration {
}
