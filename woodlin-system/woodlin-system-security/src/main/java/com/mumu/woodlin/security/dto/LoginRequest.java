package com.mumu.woodlin.security.dto;

import com.mumu.woodlin.security.enums.LoginType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一登录请求DTO
 * 
 * @author mumu
 * @description 支持多种登录方式的统一登录请求参数
 *              根据loginType字段的值，使用不同的字段组合进行认证
 * @since 2025-01-15
 */
@Data
@Schema(description = "统一登录请求")
public class LoginRequest {
    
    /**
     * 登录方式
     * 决定使用哪种认证策略
     */
    @Schema(description = "登录方式", 
            example = "password",
            allowableValues = {"password", "captcha", "mobile_sms", "sso", "passkey", "totp"})
    private String loginType = "password";
    
    // ========== 密码登录相关字段 ==========
    
    /**
     * 用户名（用于密码登录和验证码登录）
     */
    @Schema(description = "用户名", example = "admin")
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    private String username;
    
    /**
     * 密码（用于密码登录）
     */
    @Schema(description = "密码", example = "Passw0rd")
    @Size(max = 100, message = "密码长度不能超过100个字符")
    private String password;
    
    // ========== 验证码相关字段 ==========
    
    /**
     * 图形验证码（用于验证码登录）
     */
    @Schema(description = "图形验证码")
    private String captcha;
    
    /**
     * 验证码UUID（用于验证码登录）
     */
    @Schema(description = "验证码UUID")
    private String uuid;
    
    // ========== 手机号登录相关字段 ==========
    
    /**
     * 手机号（用于手机号登录）
     */
    @Schema(description = "手机号", example = "13800138000")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String mobile;
    
    /**
     * 短信验证码（用于手机号登录）
     */
    @Schema(description = "短信验证码", example = "123456")
    @Size(max = 10, message = "短信验证码长度不能超过10个字符")
    private String smsCode;
    
    // ========== SSO登录相关字段 ==========
    
    /**
     * SSO Token（用于SSO登录）
     * 从第三方认证服务获取的令牌
     */
    @Schema(description = "SSO令牌")
    private String ssoToken;
    
    /**
     * SSO提供商（用于SSO登录）
     * 例如：oauth2, saml, cas等
     */
    @Schema(description = "SSO提供商", example = "oauth2")
    private String ssoProvider;
    
    // ========== Passkey登录相关字段 ==========
    
    /**
     * Passkey凭证ID（用于Passkey登录）
     * WebAuthn/FIDO2的凭证标识符
     */
    @Schema(description = "Passkey凭证ID")
    private String passkeyCredentialId;
    
    /**
     * Passkey认证响应（用于Passkey登录）
     * WebAuthn认证响应的JSON字符串
     */
    @Schema(description = "Passkey认证响应")
    private String passkeyAuthResponse;
    
    // ========== TOTP相关字段 ==========
    
    /**
     * TOTP验证码（用于TOTP双因素认证）
     * 6位数字的时间基准一次性密码
     */
    @Schema(description = "TOTP验证码", example = "123456")
    @Size(min = 6, max = 6, message = "TOTP验证码必须是6位数字")
    private String totpCode;
    
    /**
     * TOTP密钥（用于TOTP绑定）
     * 用户首次绑定TOTP时使用
     */
    @Schema(description = "TOTP密钥")
    private String totpSecret;
    
    // ========== 通用字段 ==========
    
    /**
     * 是否记住我
     */
    @Schema(description = "是否记住我", example = "false")
    private Boolean rememberMe = false;
    
    /**
     * 获取登录类型枚举
     * 
     * @return 登录类型枚举
     */
    public LoginType getLoginTypeEnum() {
        return LoginType.fromCode(this.loginType);
    }
}
