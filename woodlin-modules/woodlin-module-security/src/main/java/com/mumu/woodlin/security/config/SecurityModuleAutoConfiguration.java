package com.mumu.woodlin.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 安全模块自动装配。
 */
@AutoConfiguration
@Import({
    SaTokenConfiguration.class,
    PasswordPolicyProperties.class,
    ActivityMonitoringProperties.class
})
@ComponentScan(basePackages = {
    "com.mumu.woodlin.security.controller",
    "com.mumu.woodlin.security.handler",
    "com.mumu.woodlin.security.interceptor",
    "com.mumu.woodlin.security.service"
})
public class SecurityModuleAutoConfiguration {
}
