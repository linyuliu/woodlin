package com.mumu.woodlin.task.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 任务模块自动装配。
 */
@AutoConfiguration
@Import(SnailJobConfig.class)
@ComponentScan(basePackages = {
    "com.mumu.woodlin.task.controller",
    "com.mumu.woodlin.task.service"
})
@MapperScan("com.mumu.woodlin.task.mapper")
public class TaskModuleAutoConfiguration {
}
