package com.mumu.woodlin.sql2api.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * SQL2API 模块自动装配。
 */
@AutoConfiguration
@Import(Sql2ApiConfiguration.class)
@ComponentScan(basePackages = {
    "com.mumu.woodlin.sql2api.controller",
    "com.mumu.woodlin.sql2api.service"
})
@MapperScan("com.mumu.woodlin.sql2api.mapper")
public class Sql2ApiModuleAutoConfiguration {
}
