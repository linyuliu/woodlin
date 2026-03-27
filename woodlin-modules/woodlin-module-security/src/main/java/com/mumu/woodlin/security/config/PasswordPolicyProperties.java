package com.mumu.woodlin.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 密码策略配置属性
 * 
 * @author mumu
 * @description 密码策略相关配置，包括首次登录、密码过期等策略
 * @since 2025-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "woodlin.security.password")
public class PasswordPolicyProperties {
    
    /**
     * 是否启用密码策略
     */
    private Boolean enabled = true;
    
    /**
     * 是否要求首次登录修改密码
     */
    private Boolean requireChangeOnFirstLogin = false;
    
    /**
     * 密码过期天数（0表示永不过期）
     */
    private Integer expireDays = 0;
    
    /**
     * 密码过期前提醒天数
     */
    private Integer warningDays = 7;
    
    /**
     * 最大密码错误次数（超过此次数将锁定账号）
     */
    private Integer maxErrorCount = 5;
    
    /**
     * 账号锁定时长（分钟）
     */
    private Integer lockDurationMinutes = 30;
    
    /**
     * 是否启用强密码策略
     */
    private Boolean strongPasswordRequired = false;
    
    /**
     * 最小密码长度
     */
    private Integer minLength = 6;
    
    /**
     * 最大密码长度
     */
    private Integer maxLength = 20;
    
    /**
     * 是否要求包含数字
     */
    private Boolean requireDigits = false;
    
    /**
     * 是否要求包含小写字母
     */
    private Boolean requireLowercase = false;
    
    /**
     * 是否要求包含大写字母
     */
    private Boolean requireUppercase = false;
    
    /**
     * 是否要求包含特殊字符
     */
    private Boolean requireSpecialChars = false;
    
}