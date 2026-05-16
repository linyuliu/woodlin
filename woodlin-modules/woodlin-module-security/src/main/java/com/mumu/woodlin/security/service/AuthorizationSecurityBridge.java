package com.mumu.woodlin.security.service;

import java.util.List;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.mumu.woodlin.authorization.entity.AuthRole;
import com.mumu.woodlin.authorization.model.AuthorizationContext;
import com.mumu.woodlin.authorization.model.AuthorizationRequest;
import com.mumu.woodlin.authorization.model.AuthorizationResource;
import com.mumu.woodlin.authorization.model.AuthorizationSubject;
import com.mumu.woodlin.authorization.service.AuthorizationService;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.util.SecurityUtil;

/**
 * 将 Sa-Token 会话权限检查桥接到统一授权中心。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Component
@RequiredArgsConstructor
public class AuthorizationSecurityBridge implements SecurityUtil.AuthorizationChecker {

    private final AuthorizationService authorizationService;

    /**
     * 注册授权检查器。
     */
    @PostConstruct
    public void register() {
        SecurityUtil.setAuthorizationChecker(this);
    }

    /**
     * 注销授权检查器。
     */
    @PreDestroy
    public void unregister() {
        SecurityUtil.setAuthorizationChecker(null);
    }

    @Override
    public boolean hasPermission(LoginUser loginUser, String permission) {
        if (loginUser == null || StrUtil.isBlank(permission)) {
            return false;
        }
        AuthorizationRequest request = new AuthorizationRequest()
            .setSubject(new AuthorizationSubject().setType("user").setId(String.valueOf(loginUser.getUserId())))
            .setAction(permission)
            .setResource(new AuthorizationResource().setType("permission").setId(permission))
            .setContext(new AuthorizationContext()
                .setTenantId(loginUser.getTenantId())
                .setDeptId(loginUser.getDeptId()));
        return authorizationService.can(request).isAllowed();
    }

    @Override
    public boolean hasRole(LoginUser loginUser, String roleCode) {
        if (loginUser == null || loginUser.getUserId() == null || StrUtil.isBlank(roleCode)) {
            return false;
        }
        List<AuthRole> roles = authorizationService.listUserRoles(loginUser.getUserId());
        return roles.stream()
            .map(AuthRole::getRoleCode)
            .filter(StrUtil::isNotBlank)
            .anyMatch(code -> StrUtil.equals(code, roleCode)
                || StrUtil.equals(code, CommonConstant.SUPER_ADMIN_ROLE_CODE));
    }
}
