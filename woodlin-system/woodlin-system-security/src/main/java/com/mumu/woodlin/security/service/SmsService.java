package com.mumu.woodlin.security.service;

/**
 * 短信服务接口
 *
 * @author mumu
 * @description 提供短信发送和验证相关功能
 *              支持短信验证码的发送、验证和管理
 * @since 2025-01-15
 */
public interface SmsService {
    
    /**
     * 发送短信验证码
     *
     * @param mobile 手机号码
     * @return 是否发送成功
     */
    boolean sendSmsCode(String mobile);
    
    /**
     * 验证短信验证码
     *
     * @param mobile 手机号码
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifySmsCode(String mobile, String code);
    
    /**
     * 清除短信验证码（验证成功后调用）
     *
     * @param mobile 手机号码
     */
    void clearSmsCode(String mobile);
}
