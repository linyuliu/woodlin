package com.mumu.woodlin.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Woodlin 管理后台启动类
 * @author mumu
 * @description Woodlin管理后台系统的主启动类，整合所有功能模块
 * @since 2025-01-01
 */
@SpringBootApplication(scanBasePackages = {
        "com.mumu.woodlin.admin",           // 管理后台模块
        "com.mumu.woodlin.common",          // 通用模块
        "com.mumu.woodlin.security",        // 安全模块
        "com.mumu.woodlin.system",          // 系统管理模块
        "com.mumu.woodlin.tenant",          // 多租户模块
        "com.mumu.woodlin.file",            // 文件管理模块
        "com.mumu.woodlin.task",            // 任务调度模块
        "com.mumu.woodlin.generator",       // 代码生成模块
        "com.mumu.woodlin.sql2api"          // SQL2API模块
})
@MapperScan("com.mumu.woodlin.**.mapper")
public class WoodlinAdminApplication {
    /**
     * 主方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WoodlinAdminApplication.class, args);
    }
}
