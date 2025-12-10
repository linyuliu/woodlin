package com.mumu.woodlin.admin.strategy;

import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录策略通用辅助方法
 */
final class LoginStrategyHelper {

    private LoginStrategyHelper() {
    }

    static String getClientIp() {
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

    static void updateLoginInfo(SysUser user, ISysUserService userService, String clientIp) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        userService.updateById(user);
    }

    static LoginUser buildLoginUser(SysUser user, ISysRoleService roleService,
                                    ISysPermissionService permissionService, String clientIp) {
        List<SysRole> roles = roleService.selectAllRolesByUserId(user.getUserId());
        List<Long> roleIds = roles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        List<String> permissions = permissionService.selectPermissionCodesByUserId(user.getUserId());

        return new LoginUser()
            .setUserId(user.getUserId())
            .setUsername(user.getUsername())
            .setNickname(user.getNickname())
            .setRealName(user.getRealName())
            .setEmail(user.getEmail())
            .setMobile(user.getMobile())
            .setAvatar(user.getAvatar())
            .setGender(user.getGender())
            .setStatus(user.getStatus())
            .setTenantId(user.getTenantId())
            .setDeptId(user.getDeptId())
            .setRoleIds(roleIds)
            .setRoleCodes(roleCodes)
            .setPermissions(permissions)
            .setLoginTime(LocalDateTime.now())
            .setLoginIp(clientIp);
    }
}
