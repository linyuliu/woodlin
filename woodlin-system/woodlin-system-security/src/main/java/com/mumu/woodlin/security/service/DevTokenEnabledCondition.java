package com.mumu.woodlin.security.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * 开发令牌启用条件注解
 * 
 * @author mumu
 * @description 用于条件化地启用开发令牌相关功能
 * @since 2025-01-07
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    prefix = "woodlin.security.dev-token",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
public @interface DevTokenEnabledCondition {
}
