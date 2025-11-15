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
 * Passkey无密码登录策略
 *
 * @author mumu
 * @description 使用WebAuthn/FIDO2标准进行无密码认证
 *              支持生物识别（指纹、面部识别）和硬件密钥
 *              提供更安全和便捷的登录体验
 *              当前为框架实现，需要集成WebAuthn服务端库
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasskeyLoginStrategy implements LoginStrategy {
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行Passkey登录策略: credentialId={}", loginRequest.getPasskeyCredentialId());
        
        // TODO: 实现Passkey登录逻辑
        // 1. 验证WebAuthn认证响应
        // 2. 验证凭证签名
        // 3. 查找关联的用户账号
        // 4. 完成登录流程
        
        throw BusinessException.of(ResultCode.METHOD_NOT_ALLOWED, 
                "Passkey登录功能尚未完全实现，需要集成WebAuthn服务端库（如webauthn4j）");
    }
    
    @Override
    public LoginType getLoginType() {
        return LoginType.PASSKEY;
    }
    
    @Override
    public boolean validateRequest(LoginRequest loginRequest) {
        if (!LoginStrategy.super.validateRequest(loginRequest)) {
            return false;
        }
        // 验证必需的Passkey登录字段
        return loginRequest.getPasskeyCredentialId() != null && !loginRequest.getPasskeyCredentialId().isBlank()
                && loginRequest.getPasskeyAuthResponse() != null && !loginRequest.getPasskeyAuthResponse().isBlank();
    }
}
