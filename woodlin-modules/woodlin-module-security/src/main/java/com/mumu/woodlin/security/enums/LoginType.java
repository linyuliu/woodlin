package com.mumu.woodlin.security.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录方式枚举
 *
 * @author mumu
 * @description 定义系统支持的各种登录认证方式，支持多种登录场景
 * @since 2025-01-15
 */
@Getter
@AllArgsConstructor
public enum LoginType {
    
    /**
     * 密码登录 - 使用用户名+密码进行认证
     * 最传统和基础的登录方式
     */
    PASSWORD("password", "密码登录"),
    
    /**
     * 验证码登录 - 使用用户名+图形验证码进行认证
     * 适用于需要额外安全验证的场景
     */
    CAPTCHA("captcha", "验证码登录"),
    
    /**
     * 手机号登录 - 使用手机号+短信验证码进行认证
     * 适用于移动端和快速登录场景
     */
    MOBILE_SMS("mobile_sms", "手机号登录"),
    
    /**
     * SSO单点登录 - 使用第三方认证服务进行登录
     * 适用于企业内部统一认证场景（如OAuth2, SAML等）
     */
    SSO("sso", "单点登录"),
    
    /**
     * Passkey登录 - 使用WebAuthn/FIDO2标准的无密码认证
     * 使用生物识别或硬件密钥进行安全认证
     */
    PASSKEY("passkey", "Passkey登录"),
    
    /**
     * TOTP双因素认证 - 使用时间基准的一次性密码
     * 通常作为第二因素认证，配合密码使用（2FA）
     */
    TOTP("totp", "TOTP双因素认证");
    
    /**
     * 登录类型代码
     */
    private final String code;
    
    /**
     * 登录类型描述
     */
    private final String description;
    
    /**
     * 根据代码获取登录类型
     *
     * @param code 登录类型代码
     * @return 登录类型枚举，如果未找到返回null
     */
    public static LoginType fromCode(String code) {
        for (LoginType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
