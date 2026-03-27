package com.mumu.woodlin.z3.config;

import com.mumu.woodlin.z3.service.Z3SolverService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Z3 模块自动配置.
 *
 * <p>通过 Spring Boot 自动配置机制注册 {@link Z3SolverService}。</p>
 *
 * @author mumu
 * @since 2025-10-28
 */
@AutoConfiguration
public class Z3AutoConfiguration {

    /**
     * 注册 Z3 求解服务.
     *
     * @return Z3SolverService 实例
     */
    @Bean
    public Z3SolverService z3SolverService() {
        return new Z3SolverService();
    }
}
