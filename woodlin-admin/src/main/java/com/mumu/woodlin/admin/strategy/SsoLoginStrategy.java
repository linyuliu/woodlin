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
import com.mumu.woodlin.security.util.SsoTokenVerifier;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.entity.SysUserAuthIdentity;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserAuthIdentityService;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final ISysUserAuthIdentityService authIdentityService;

    @Value("${woodlin.security.sso.shared-secret:woodlin-sso-secret}")
    private String sharedSecret;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行SSO登录策略: provider={}", loginRequest.getSsoProvider());

        SsoTokenVerifier.SsoPrincipal principal;
        try {
            principal = SsoTokenVerifier.parse(loginRequest.getSsoToken(), loginRequest.getSsoProvider(), sharedSecret);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, ex.getMessage());
        }

        String identifier = principal.getProvider() + ":" + principal.getExternalId();
        SysUserAuthIdentity identity = authIdentityService.findByTypeAndIdentifier(LoginType.SSO.getCode(), identifier);

        SysUser user = null;
        if (identity != null) {
            user = userService.getById(identity.getUserId());
        }

        if (user == null && principal.getUsername() != null) {
            user = userService.selectUserByUsername(principal.getUsername());
            if (user != null) {
                // 自动绑定SSO到现有账号
                SysUserAuthIdentity newIdentity = new SysUserAuthIdentity()
                    .setUserId(user.getUserId())
                    .setAuthType(LoginType.SSO.getCode())
                    .setIdentifier(identifier)
                    .setExtData(getExtData(identity))
                    .setVerified(true)
                    .setStatus(CommonConstant.STATUS_ENABLE)
                    .setTenantId(user.getTenantId());
                newIdentity.setDeleted("0");
                authIdentityService.save(newIdentity);
                identity = newIdentity;
            }
        }

        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "SSO账号未绑定本地用户");
        }
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        if (identity != null) {
            identity.setLastUsedTime(LocalDateTime.now());
            authIdentityService.updateById(identity);
        }

        String clientIp = LoginStrategyHelper.getClientIp();
        LoginStrategyHelper.updateLoginInfo(user, userService, clientIp);
        LoginUser loginUser = LoginStrategyHelper.buildLoginUser(user, roleService, permissionService, clientIp);
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set(SecurityUtil.USER_KEY, loginUser);

        return new LoginResponse()
            .setToken(StpUtil.getTokenValue())
            .setTokenType("Bearer")
            .setExpiresIn(StpUtil.getTokenTimeout())
            .setMessage("SSO登录成功");
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

    private String getExtData(SysUserAuthIdentity identity) {
        return identity == null ? null : identity.getExtData();
    }
}
