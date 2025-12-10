package com.mumu.woodlin.admin.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysRole;
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
import java.util.stream.Collectors;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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

    private final ISysUserAuthIdentityService authIdentityService;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行Passkey登录策略: credentialId={}", loginRequest.getPasskeyCredentialId());

        SysUserAuthIdentity identity = authIdentityService.findByTypeAndIdentifier(
            LoginType.PASSKEY.getCode(), loginRequest.getPasskeyCredentialId());
        if (identity == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "Passkey凭证未绑定用户");
        }

        if (!verifyPasskeyAssertion(identity.getCredential(), loginRequest.getPasskeyAuthResponse())) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "Passkey认证失败");
        }

        SysUser user = userService.getById(identity.getUserId());
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "关联用户不存在");
        }
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        identity.setLastUsedTime(LocalDateTime.now());
        authIdentityService.updateById(identity);

        String clientIp = LoginStrategyHelper.getClientIp();
        LoginStrategyHelper.updateLoginInfo(user, userService, clientIp);
        LoginUser loginUser = LoginStrategyHelper.buildLoginUser(user, roleService, permissionService, clientIp);
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set(SecurityUtil.USER_KEY, loginUser);

        return new LoginResponse()
            .setToken(StpUtil.getTokenValue())
            .setTokenType("Bearer")
            .setExpiresIn(StpUtil.getTokenTimeout())
            .setMessage("Passkey登录成功");
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

    private boolean verifyPasskeyAssertion(String storedCredential, String clientAssertion) {
        if (storedCredential == null || clientAssertion == null) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] stored = digest.digest(storedCredential.getBytes(StandardCharsets.UTF_8));
            byte[] provided = digest.digest(clientAssertion.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(stored, provided);
        } catch (Exception ex) {
            log.warn("Passkey校验失败: {}", ex.getMessage());
            return false;
        }
    }
}
