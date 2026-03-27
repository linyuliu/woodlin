package com.mumu.woodlin.etl.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * ETL 模块自动装配。
 */
@AutoConfiguration
@Import(EtlConfiguration.class)
@ComponentScan(basePackages = {
    "com.mumu.woodlin.etl.controller",
    "com.mumu.woodlin.etl.dialect",
    "com.mumu.woodlin.etl.job",
    "com.mumu.woodlin.etl.service"
})
@MapperScan("com.mumu.woodlin.etl.mapper")
public class EtlModuleAutoConfiguration {
}
