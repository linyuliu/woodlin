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
        "com.mumu.woodlin.*",           // 管理后台模块

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
