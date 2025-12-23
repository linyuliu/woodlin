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
 * SSO单点登录策略
 *
 * @author mumu
 * @description 使用第三方认证服务进行登录（OAuth2, SAML, CAS等）
 *              适用于企业内部统一认证场景
 *              当前为框架实现，需要根据实际SSO服务进行配置
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsoLoginStrategy implements LoginStrategy {
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行SSO登录策略: provider={}", loginRequest.getSsoProvider());
        
        // 此功能需要根据实际SSO服务进行配置
        // 实现步骤：
        // 1. 验证SSO Token的有效性
        // 2. 从SSO服务获取用户信息
        // 3. 在本地系统中查找或创建用户
        // 4. 完成登录流程
        
        throw BusinessException.of(ResultCode.METHOD_NOT_ALLOWED, 
                "SSO登录功能尚未完全实现，请配置相应的SSO服务提供商");
    }
    
    @Override
    public LoginType getLoginType() {
        return LoginType.SSO;
    }
    
    @Override
    public boolean validateRequest(LoginRequest loginRequest) {
        if (!LoginStrategy.super.validateRequest(loginRequest)) {
            return false;
        }
        // 验证必需的SSO登录字段
        return loginRequest.getSsoToken() != null && !loginRequest.getSsoToken().isBlank()
                && loginRequest.getSsoProvider() != null && !loginRequest.getSsoProvider().isBlank();
    }
}
