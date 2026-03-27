package com.mumu.woodlin.file.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 文件模块自动装配。
 */
@AutoConfiguration
@Import(FileProperties.class)
@ComponentScan(basePackages = {
    "com.mumu.woodlin.file.controller",
    "com.mumu.woodlin.file.service",
    "com.mumu.woodlin.file.storage",
    "com.mumu.woodlin.file.transcoding"
})
@MapperScan("com.mumu.woodlin.file.mapper")
public class FileModuleAutoConfiguration {
}
