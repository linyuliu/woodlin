package com.mumu.woodlin.security.service;

import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;

/**
 * 认证服务接口
 * 
 * @author mumu
 * @description 提供用户认证、登录、登出、密码管理等服务接口
 * @since 2025-01-01
 */
public interface AuthenticationService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     */
    void changePassword(ChangePasswordRequest request);
    
    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    Object getCurrentUserInfo();
    
}