package com.mumu.woodlin.security.service;

import com.mumu.woodlin.security.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限验证服务
 * 
 * @author mumu
 * @description 权限验证服务，用于@PreAuthorize注解的权限检查
 * @since 2025-01-01
 */
@Slf4j
@Service("auth")
public class PermissionService {
    
    /**
     * 验证用户是否具备某权限
     * 
     * @param permission 权限标识
     * @return 是否具备权限
     */
    public boolean hasPermission(String permission) {
        try {
            return SecurityUtil.hasPermission(permission);
        } catch (Exception e) {
            log.debug("权限验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证用户是否具备某角色
     * 
     * @param role 角色标识
     * @return 是否具备角色
     */
    public boolean hasRole(String role) {
        try {
            return SecurityUtil.hasRole(role);
        } catch (Exception e) {
            log.debug("角色验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证用户是否具备任意一个权限
     * 
     * @param permissions 权限列表
     * @return 是否具备任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证用户是否具备任意一个角色
     * 
     * @param roles 角色列表
     * @return 是否具备任意一个角色
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}