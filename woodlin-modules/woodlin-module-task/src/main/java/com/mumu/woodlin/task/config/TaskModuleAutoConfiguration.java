package com.mumu.woodlin.task.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 任务模块自动装配。
 */
@AutoConfiguration
@Import(SnailJobConfig.class)
public class TaskModuleAutoConfiguration {
}
