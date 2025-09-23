package com.mumu.woodlin.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Woodlin 管理后台启动类
 * 
 * @author mumu
 * @description Woodlin管理后台系统的主启动类，整合所有功能模块
 * @since 2025-01-01
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.mumu.woodlin")
public class WoodlinAdminApplication {
    
    /**
     * 主方法
     * 
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WoodlinAdminApplication.class, args);
        System.out.println("""
                
                ██╗    ██╗ ██████╗  ██████╗ ██████╗ ██╗     ██╗███╗   ██╗
                ██║    ██║██╔═══██╗██╔═══██╗██╔══██╗██║     ██║████╗  ██║
                ██║ █╗ ██║██║   ██║██║   ██║██║  ██║██║     ██║██╔██╗ ██║
                ██║███╗██║██║   ██║██║   ██║██║  ██║██║     ██║██║╚██╗██║
                ╚███╔███╔╝╚██████╔╝╚██████╔╝██████╔╝███████╗██║██║ ╚████║
                 ╚══╝╚══╝  ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝╚═╝╚═╝  ╚═══╝
                
                :: Woodlin 多租户中后台管理系统 ::        (v1.0.0)
                :: 作者: mumu ::                      :: 2025-01-01 ::
                """);
    }
    
}