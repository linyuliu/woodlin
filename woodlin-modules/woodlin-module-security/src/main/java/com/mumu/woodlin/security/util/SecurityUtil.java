package com.mumu.woodlin.security.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.mumu.woodlin.security.model.LoginUser;

/**
 * 安全工具类
 * 
 * @author mumu
 * @description 提供用户认证、授权的便捷方法，基于Sa-Token实现
 * @since 2025-01-01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtil {
    
    /**
     * 用户ID的会话key
     */
    public static final String USER_KEY = "loginUser";
    
    /**
     * 获取用户账户
     * 
     * @return 用户账户
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getUsername() : null;
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser() {
        return (LoginUser) StpUtil.getSession().get(USER_KEY);
    }
    
    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getUserId() : null;
    }
    
    /**
     * 获取当前登录用户的租户ID
     * 
     * @return 租户ID
     */
    public static String getTenantId() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getTenantId() : null;
    }
    
    /**
     * 获取当前登录用户的部门ID
     * 
     * @return 部门ID
     */
    public static Long getDeptId() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getDeptId() : null;
    }
    
    /**
     * 设置用户数据（登录时调用）
     * 
     * @param loginUser 登录用户信息
     */
    public static void setLoginUser(LoginUser loginUser) {
        StpUtil.getSession().set(USER_KEY, loginUser);
    }
    
    /**
     * 是否为管理员
     * 
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) && loginUser.isSuperAdmin();
    }
    
    /**
     * 验证用户是否具备某权限
     * 
     * @param permission 权限标识
     * @return 是否具备权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) && loginUser.hasPermission(permission);
    }
    
    /**
     * 验证用户是否具备某角色
     * 
     * @param roleCode 角色编码
     * @return 是否具备角色
     */
    public static boolean hasRole(String roleCode) {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNotNull(loginUser) && loginUser.hasRole(roleCode);
    }
    
    /**
     * 验证用户是否不具备某权限，与 hasPermission 逻辑相反
     * 
     * @param permission 权限标识
     * @return 是否不具备权限
     */
    public static boolean lacksPermission(String permission) {
        return !hasPermission(permission);
    }
    
    /**
     * 验证用户是否不具备某角色，与 hasRole 逻辑相反
     * 
     * @param roleCode 角色编码
     * @return 是否不具备角色
     */
    public static boolean lacksRole(String roleCode) {
        return !hasRole(roleCode);
    }
    
    /**
     * 判断是否登录
     * 
     * @return 是否登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }
    
    /**
     * 检查登录状态，如果未登录则抛出异常
     */
    public static void checkLogin() {
        StpUtil.checkLogin();
    }
    
    /**
     * 登录
     * 
     * @param userId 用户ID
     */
    public static void login(Object userId) {
        StpUtil.login(userId);
    }
    
    /**
     * 登录，并指定设备类型
     * 
     * @param userId 用户ID
     * @param device 设备类型
     */
    public static void login(Object userId, String device) {
        StpUtil.login(userId, device);
    }
    
    /**
     * 注销登录
     */
    public static void logout() {
        StpUtil.logout();
    }
    
    /**
     * 注销指定用户
     * 
     * @param userId 用户ID
     */
    public static void kickout(Object userId) {
        StpUtil.kickout(userId);
    }
    
    /**
     * 获取当前登录的设备类型
     * 
     * @return 设备类型
     */
    public static String getLoginDevice() {
        return StpUtil.getLoginDevice();
    }
    
    /**
     * 获取当前 Token 值
     * 
     * @return Token 值
     */
    public static String getTokenValue() {
        return StpUtil.getTokenValue();
    }
    
    /**
     * 获取当前 Token 的剩余有效时间（单位：秒）
     * 
     * @return 剩余有效时间
     */
    public static long getTokenTimeout() {
        return StpUtil.getTokenTimeout();
    }
    
}