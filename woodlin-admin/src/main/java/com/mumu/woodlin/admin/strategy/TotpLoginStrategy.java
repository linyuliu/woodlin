package com.mumu.woodlin.admin.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.service.PasswordPolicyService;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.security.util.TotpUtil;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.entity.SysUserAuthIdentity;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserAuthIdentityService;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

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

    private final ISysUserService userService;
    private final PasswordPolicyService passwordPolicyService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final ISysUserAuthIdentityService authIdentityService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行TOTP双因素认证策略: username={}", loginRequest.getUsername());

        SysUser user = userService.selectUserByUsername(loginRequest.getUsername());
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        // 先验证密码
        if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            handlePasswordError(user);
            throw BusinessException.of(ResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }

        PasswordPolicyService.PasswordValidationResult validationResult =
            passwordPolicyService.validateUserLogin(new UserPasswordInfoAdapter(user));
        if (!validationResult.isValid()) {
            throw BusinessException.of(ResultCode.PASSWORD_POLICY_VIOLATION, validationResult.getMessage());
        }

        // 获取/绑定TOTP密钥
        SysUserAuthIdentity totpIdentity =
            authIdentityService.findByUserIdAndType(user.getUserId(), LoginType.TOTP.getCode());
        if (totpIdentity == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "未绑定TOTP密钥，请先完成绑定后再登录");
        }

        if (!TotpUtil.verifyCode(totpIdentity.getCredential(), loginRequest.getTotpCode())) {
            throw BusinessException.of(ResultCode.CAPTCHA_ERROR, "TOTP验证码错误或已过期");
        }

        totpIdentity.setLastUsedTime(LocalDateTime.now());
        authIdentityService.updateById(totpIdentity);

        // 登录成功，更新用户信息并构建响应
        String clientIp = LoginStrategyHelper.getClientIp();
        LoginStrategyHelper.updateLoginInfo(user, userService, clientIp);
        LoginUser loginUser = LoginStrategyHelper.buildLoginUser(user, roleService, permissionService, clientIp);
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set(SecurityUtil.USER_KEY, loginUser);

        return new LoginResponse()
            .setToken(StpUtil.getTokenValue())
            .setTokenType("Bearer")
            .setExpiresIn(StpUtil.getTokenTimeout())
            .setRequirePasswordChange(validationResult.isRequireChange())
            .setPasswordExpiringSoon(validationResult.isExpiringSoon())
            .setDaysUntilPasswordExpiration(validationResult.getDaysUntilExpiration())
            .setMessage("TOTP登录成功");
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
        return loginRequest.getUsername() != null && !loginRequest.getUsername().isBlank()
                && loginRequest.getPassword() != null && !loginRequest.getPassword().isBlank()
                && loginRequest.getTotpCode() != null && !loginRequest.getTotpCode().isBlank()
                && loginRequest.getTotpCode().length() == 6;
    }

    private void handlePasswordError(SysUser user) {
        int errorCount = user.getPwdErrorCount() == null ? 0 : user.getPwdErrorCount();
        if (passwordPolicyService.handlePasswordError(new UserPasswordInfoAdapter(user))) {
            user.setPwdErrorCount(errorCount + 1);
            user.setLockTime(LocalDateTime.now());
        } else {
            user.setPwdErrorCount(errorCount + 1);
        }
        userService.updateById(user);
    }

    /**
     * 用户密码信息适配器
     */
    private record UserPasswordInfoAdapter(SysUser user) implements PasswordPolicyService.UserPasswordInfo {

        @Override
        public Boolean getIsFirstLogin() {
            return user.getIsFirstLogin();
        }

        @Override
        public LocalDateTime getPwdChangeTime() {
            return user.getPwdChangeTime();
        }

        @Override
        public LocalDateTime getCreateTime() {
            return user.getCreateTime();
        }

        @Override
        public Integer getPwdExpireDays() {
            return user.getPwdExpireDays();
        }

        @Override
        public LocalDateTime getLockTime() {
            return user.getLockTime();
        }

        @Override
        public Integer getPwdErrorCount() {
            return user.getPwdErrorCount();
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "127.0.0.1";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
