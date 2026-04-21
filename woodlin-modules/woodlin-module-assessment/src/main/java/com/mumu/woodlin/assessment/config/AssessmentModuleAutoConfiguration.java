package com.mumu.woodlin.assessment.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 测评模块自动装配
 *
 * <p>负责扫描测评模块的 Spring Bean 和 MyBatis Mapper。
 *
 * @author mumu
 * @since 2025-01-01
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.mumu.woodlin.assessment")
@MapperScan("com.mumu.woodlin.assessment.mapper")
public class AssessmentModuleAutoConfiguration {
}
