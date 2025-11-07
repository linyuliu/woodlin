package com.mumu.woodlin.admin.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.service.AuthenticationService;
import com.mumu.woodlin.security.service.CaptchaService;
import com.mumu.woodlin.security.config.DevTokenProperties;
import com.mumu.woodlin.admin.service.DevTokenStartupListener;

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
    private final CaptchaService captchaService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private DevTokenProperties devTokenProperties;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private DevTokenStartupListener devTokenStartupListener;
    
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
    
    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    @Operation(
        summary = "获取验证码",
        description = "生成图形验证码，返回验证码ID和Base64编码的图片"
    )
    public R<CaptchaService.CaptchaInfo> getCaptcha() {
        CaptchaService.CaptchaInfo captchaInfo = captchaService.generateCaptcha();
        return R.ok(captchaInfo);
    }
    
    /**
     * 生成开发调试令牌（仅开发环境）
     */
    @GetMapping("/dev-token")
    @Operation(
        summary = "生成开发调试令牌",
        description = "为开发调试生成便捷令牌，仅在开发和测试环境可用。返回可直接使用的访问令牌。"
    )
    public R<Object> generateDevToken() {
        // 检查是否启用开发令牌功能
        if (devTokenProperties == null || !devTokenProperties.getEnabled()) {
            return R.fail("开发令牌功能未启用。请在配置文件中设置 woodlin.security.dev-token.enabled=true");
        }
        
        if (devTokenStartupListener == null) {
            return R.fail("开发令牌服务不可用");
        }
        
        try {
            // 使用启动监听器的方法生成令牌（复用逻辑）
            java.lang.reflect.Method method = DevTokenStartupListener.class
                .getDeclaredMethod("generateDevToken", String.class);
            method.setAccessible(true);
            
            Object tokenInfo = method.invoke(devTokenStartupListener, devTokenProperties.getUsername());
            
            log.info("通过 API 端点生成开发令牌成功");
            return R.ok(tokenInfo, "开发令牌生成成功");
            
        } catch (Exception e) {
            log.error("生成开发令牌失败: {}", e.getMessage(), e);
            return R.fail("生成开发令牌失败: " + e.getMessage());
        }
    }
    
}