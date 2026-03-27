package com.mumu.woodlin.datasource.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据源模块自动装配。
 */
@AutoConfiguration
@ComponentScan(basePackages = {
    "com.mumu.woodlin.datasource.controller",
    "com.mumu.woodlin.datasource.service"
})
@MapperScan("com.mumu.woodlin.datasource.mapper")
public class DatasourceModuleAutoConfiguration {
}
