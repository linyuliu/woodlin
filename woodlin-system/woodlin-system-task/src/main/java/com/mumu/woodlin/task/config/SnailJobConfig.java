package com.mumu.woodlin.task.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * SnailJob 任务调度配置
 * 
 * @author mumu
 * @description SnailJob分布式任务调度配置
 * @since 2025-01-01
 */
@Configuration
@ConditionalOnProperty(name = "snail-job.enabled", havingValue = "true", matchIfMissing = true)
public class SnailJobConfig {
    
    // SnailJob 配置将通过 application.yml 进行配置
    // 具体配置项包括:
    // - 服务端地址
    // - 命名空间
    // - 组名称
    // - 等其他配置项
}