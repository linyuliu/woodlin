package com.mumu.woodlin.admin.controller;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 
 * @author mumu
 * @description 处理用户登录、登出、密码修改等认证相关操作
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户身份认证相关接口，包括登录、登出、密码管理等功能")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(
        summary = "用户登录",
        description = "通过用户名和密码进行登录认证，登录成功后返回访问令牌（token）"
    )
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authenticationService.login(loginRequest);
        return R.ok(response);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(
        summary = "用户登出",
        description = "退出当前登录状态，清除服务端会话信息和token"
    )
    public R<Void> logout() {
        authenticationService.logout();
        return R.ok("退出成功");
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(
        summary = "修改密码",
        description = "修改当前登录用户的密码，需要提供旧密码和新密码"
    )
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return R.ok("密码修改成功");
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/userinfo")
    @Operation(
        summary = "获取当前用户信息",
        description = "获取当前登录用户的详细信息，包括用户基本信息、角色、权限等"
    )
    public R<Object> getUserInfo() {
        Object userInfo = authenticationService.getCurrentUserInfo();
        return R.ok(userInfo);
    }
    
}