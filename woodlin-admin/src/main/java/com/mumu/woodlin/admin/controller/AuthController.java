package com.mumu.woodlin.admin.controller;

import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.dto.ForgotPasswordRequest;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.service.AuthenticationService;
import com.mumu.woodlin.security.service.CaptchaService;
import com.mumu.woodlin.security.service.SmsService;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.dto.RouteVO;
import com.mumu.woodlin.system.service.ISysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 *
 * @author mumu
 * @description 处理用户登录、登出、密码修改等认证相关操作
 *              支持多种登录方式：密码登录、验证码登录、手机号登录、SSO登录、Passkey登录、TOTP认证
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户身份认证相关接口，包括多种登录方式、登出、密码管理等功能")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CaptchaService captchaService;
    private final SmsService smsService;
    private final ISysPermissionService permissionService;

    /**
     * 统一登录接口 - 支持多种登录方式
     *
     * <p>根据loginType字段选择相应的登录策略：</p>
     * <ul>
     *   <li>password - 密码登录（需要username和password）</li>
     *   <li>captcha - 验证码登录（需要username、captcha和uuid）</li>
     *   <li>mobile_sms - 手机号登录（需要mobile和smsCode）</li>
     *   <li>sso - SSO单点登录（需要ssoToken和ssoProvider）</li>
     *   <li>passkey - Passkey登录（需要passkeyCredentialId和passkeyAuthResponse）</li>
     *   <li>totp - TOTP双因素认证（需要totpCode）</li>
     * </ul>
     */
    @PostMapping("/login")
    @Operation(
        summary = "用户登录",
        description = "统一登录接口，支持多种登录方式（密码、验证码、手机号、SSO、Passkey、TOTP）。" +
                     "根据loginType字段选择相应的认证策略，不同登录方式需要提供不同的字段组合。"
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
        description = "修改当前登录用户的密码，需要提供旧密码和新密码。修改成功后会强制重新登录。"
    )
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return R.ok("密码修改成功，请重新登录");
    }
    
    /**
     * 忘记密码重置（通过验证码）
     */
    @PostMapping("/forgot-password")
    @Operation(
        summary = "忘记密码重置",
        description = "通过验证码重置密码，无需登录。支持短信验证码和邮件验证码。"
    )
    public R<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.resetPasswordByCode(request);
        return R.ok("密码重置成功，请使用新密码登录");
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
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    @Operation(
        summary = "获取图形验证码",
        description = "生成图形验证码，返回验证码ID和Base64编码的图片。用于验证码登录或增强密码登录安全性。"
    )
    public R<CaptchaService.CaptchaInfo> getCaptcha() {
        CaptchaService.CaptchaInfo captchaInfo = captchaService.generateCaptcha();
        return R.ok(captchaInfo);
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    @Operation(
        summary = "发送短信验证码",
        description = "向指定手机号发送短信验证码，用于手机号登录。验证码有效期5分钟。"
    )
    public R<Void> sendSmsCode(@NotBlank(message = "手机号不能为空") @RequestParam String mobile) {
        boolean success = smsService.sendSmsCode(mobile);
        if (success) {
            return R.ok("短信验证码已发送");
        } else {
            return R.fail("短信验证码发送失败");
        }
    }

    /**
     * 获取当前用户的路由菜单
     */
    @GetMapping("/routes")
    @Operation(
        summary = "获取用户路由菜单",
        description = "获取当前登录用户的路由菜单信息，用于前端动态路由生成"
    )
    public R<List<RouteVO>> getUserRoutes() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            return R.fail("用户未登录");
        }
        List<RouteVO> routes = permissionService.selectRoutesByUserId(userId);
        return R.ok(routes);
    }

}
