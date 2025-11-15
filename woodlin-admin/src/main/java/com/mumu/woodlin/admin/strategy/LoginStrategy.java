package com.mumu.woodlin.admin.strategy;

import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;

/**
 * 登录策略接口
 *
 * @author mumu
 * @description 定义不同登录方式的统一接口，使用策略模式实现多种登录方式
 *              每种登录方式都应该实现此接口，提供各自的认证逻辑
 * @since 2025-01-15
 */
public interface LoginStrategy {
    
    /**
     * 执行登录认证
     * 
     * @param loginRequest 登录请求参数
     * @return 登录响应结果
     * @throws com.mumu.woodlin.common.exception.BusinessException 认证失败时抛出业务异常
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 获取当前策略支持的登录类型
     * 
     * @return 登录类型枚举
     */
    LoginType getLoginType();
    
    /**
     * 验证登录请求参数是否有效
     * 
     * @param loginRequest 登录请求参数
     * @return 如果参数有效返回true，否则返回false
     */
    default boolean validateRequest(LoginRequest loginRequest) {
        return loginRequest != null && loginRequest.getLoginTypeEnum() == getLoginType();
    }
}
