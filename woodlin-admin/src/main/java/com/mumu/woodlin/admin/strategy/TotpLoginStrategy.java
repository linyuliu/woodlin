package com.mumu.woodlin.admin.strategy;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TOTP双因素认证策略
 *
 * @author mumu
 * @description 使用时间基准的一次性密码进行二次认证
 *              通常与密码登录配合使用，提供额外的安全层（2FA）
 *              支持Google Authenticator、Microsoft Authenticator等TOTP应用
 *              当前为框架实现，需要集成TOTP库（如Google Authenticator库）
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TotpLoginStrategy implements LoginStrategy {
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行TOTP双因素认证策略");
        
        // 此功能需要集成TOTP库（如google-authenticator）
        // 实现步骤：
        // 1. 验证用户是否已绑定TOTP
        // 2. 验证TOTP验证码是否正确
        // 3. 检查时间窗口和防重放
        // 4. 完成二次认证流程
        
        throw BusinessException.of(ResultCode.METHOD_NOT_ALLOWED, 
                "TOTP认证功能尚未完全实现，需要集成TOTP库（如google-authenticator）");
    }
    
    @Override
    public LoginType getLoginType() {
        return LoginType.TOTP;
    }
    
    @Override
    public boolean validateRequest(LoginRequest loginRequest) {
        if (!LoginStrategy.super.validateRequest(loginRequest)) {
            return false;
        }
        // 验证必需的TOTP认证字段
        return loginRequest.getTotpCode() != null && !loginRequest.getTotpCode().isBlank()
                && loginRequest.getTotpCode().length() == 6;
    }
}
