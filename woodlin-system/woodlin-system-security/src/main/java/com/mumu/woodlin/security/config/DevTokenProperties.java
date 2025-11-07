package com.mumu.woodlin.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 开发环境令牌配置属性
 * 
 * @author mumu
 * @description 用于开发调试的便捷令牌配置，仅在开发和测试环境有效
 * @since 2025-01-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.security.dev-token")
public class DevTokenProperties {
    
    /**
     * 是否启用开发令牌功能
     * 默认：false（仅在dev和test环境建议启用）
     */
    private Boolean enabled = false;
    
    /**
     * 开发令牌的默认用户名
     * 默认使用系统管理员账号
     */
    private String username = "admin";
    
    /**
     * 是否在应用启动时自动生成令牌
     */
    private Boolean autoGenerateOnStartup = true;
    
    /**
     * 开发令牌的有效期（秒）
     * -1 表示永不过期，0 表示使用系统默认配置
     */
    private Long timeout = 0L;
    
    /**
     * 是否在控制台输出令牌信息
     */
    private Boolean printToConsole = true;
    
    /**
     * 令牌提示信息的显示格式
     */
    private String displayFormat = "banner";
    
}
