package com.mumu.woodlin.admin.service.impl;

import java.time.LocalDateTime;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.service.AuthenticationService;
import com.mumu.woodlin.security.service.PasswordPolicyService;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 * 
 * @author mumu
 * @description 提供用户认证、登录、登出、密码管理等服务实现
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final ISysUserService userService;
    private final PasswordPolicyService passwordPolicyService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    
    /**
     * 权限缓存服务（可选依赖）
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.mumu.woodlin.security.service.PermissionCacheService permissionCacheService;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest loginRequest) {
        // 查找用户
        SysUser user = userService.selectUserByUsername(loginRequest.getUsername());
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 检查用户状态
        if (!"1".equals(user.getStatus())) {
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
        }
        
        // 创建用户密码信息适配器
        UserPasswordInfoAdapter userAdapter = new UserPasswordInfoAdapter(user);
        
        // 检查账号锁定
        if (passwordPolicyService.isAccountLocked(userAdapter)) {
            throw BusinessException.of(ResultCode.ACCOUNT_LOCKED, "账号已被锁定，请稍后再试");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            // 处理密码错误
            handlePasswordError(user);
            throw BusinessException.of(ResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }
        
        // 验证密码策略
        PasswordPolicyService.PasswordValidationResult validationResult = 
            passwordPolicyService.validateUserLogin(userAdapter);
            
        if (!validationResult.isValid()) {
            throw BusinessException.of(ResultCode.PASSWORD_POLICY_VIOLATION, validationResult.getMessage());
        }
        
        // 清除密码错误次数
        if (user.getPwdErrorCount() != null && user.getPwdErrorCount() > 0) {
            user.setPwdErrorCount(0);
            user.setLockTime(null);
            userService.updateById(user);
        }
        
        // 更新登录信息
        updateLoginInfo(user);
        
        // 创建登录用户信息
        LoginUser loginUser = buildLoginUser(user);
        
        // 使用Sa-Token登录
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set(SecurityUtil.USER_KEY, loginUser);
        
        // 构建响应
        LoginResponse response = new LoginResponse()
            .setToken(StpUtil.getTokenValue())
            .setTokenType("Bearer")
            .setExpiresIn(StpUtil.getTokenTimeout())
            .setRequirePasswordChange(validationResult.isRequireChange())
            .setPasswordExpiringSoon(validationResult.isExpiringSoon())
            .setDaysUntilPasswordExpiration(validationResult.getDaysUntilExpiration())
            .setMessage(validationResult.getMessage());
            
        log.info("用户 {} 登录成功", user.getUsername());
        return response;
    }
    
    /**
     * 用户登出
     */
    @Override
    public void logout() {
        Long userId = SecurityUtil.getUserId();
        if (userId != null) {
            StpUtil.logout(userId);
            // 清除用户缓存
            if (permissionCacheService != null) {
                permissionCacheService.evictUserCache(userId);
            }
            log.info("用户 {} 登出成功", userId);
        }
    }
    
    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        // 验证新密码和确认密码是否一致
        if (!StrUtil.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw BusinessException.of(ResultCode.PASSWORD_MISMATCH, "新密码和确认密码不一致");
        }
        
        // 查询用户信息
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.of(ResultCode.OLD_PASSWORD_ERROR, "旧密码错误");
        }
        
        // 验证新密码策略
        PasswordPolicyService.PasswordValidationResult validationResult = 
            passwordPolicyService.validatePassword(request.getNewPassword());
            
        if (!validationResult.isValid()) {
            throw BusinessException.of(ResultCode.PASSWORD_POLICY_VIOLATION, validationResult.getMessage());
        }
        
        // 更新密码
        user.setPassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        user.setPwdChangeTime(LocalDateTime.now());
        user.setIsFirstLogin(false); // 修改密码后不再是首次登录
        
        userService.updateById(user);
        
        log.info("用户 {} 修改密码成功", user.getUsername());
    }
    
    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    @Override
    public Object getCurrentUserInfo() {
        return SecurityUtil.getLoginUser();
    }
    
    /**
     * 处理密码错误
     * 
     * @param user 用户信息
     */
    private void handlePasswordError(SysUser user) {
        UserPasswordInfoAdapter userAdapter = new UserPasswordInfoAdapter(user);
        if (passwordPolicyService.handlePasswordError(userAdapter)) {
            // 需要锁定账号
            user.setPwdErrorCount(user.getPwdErrorCount() + 1);
            user.setLockTime(LocalDateTime.now());
            userService.updateById(user);
            log.warn("用户 {} 密码错误次数过多，账号已被锁定", user.getUsername());
        } else {
            // 增加错误次数
            int errorCount = user.getPwdErrorCount() == null ? 0 : user.getPwdErrorCount();
            user.setPwdErrorCount(errorCount + 1);
            userService.updateById(user);
        }
    }
    
    /**
     * 更新用户登录信息
     * 
     * @param user 用户信息
     */
    private void updateLoginInfo(SysUser user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp());
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        userService.updateById(user);
    }
    
    /**
     * 构建登录用户信息（支持RBAC1）
     * 
     * @param user 用户实体
     * @return 登录用户信息
     */
    private LoginUser buildLoginUser(SysUser user) {
        // 查询用户的所有角色（包括继承的角色，支持RBAC1）
        List<SysRole> roles = roleService.selectAllRolesByUserId(user.getUserId());
        
        // 提取角色ID和角色编码
        List<Long> roleIds = roles.stream()
            .map(SysRole::getRoleId)
            .collect(Collectors.toList());
        
        List<String> roleCodes = roles.stream()
            .map(SysRole::getRoleCode)
            .collect(Collectors.toList());
        
        // 查询用户的所有权限（包括角色继承的权限，支持RBAC1）
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
            .setLoginIp(getClientIp());
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @return IP地址
     */
    private String getClientIp() {
        // 这里可以实现获取客户端真实IP的逻辑
        // 暂时返回本地IP
        return "127.0.0.1";
    }
    
    /**
     * 用户密码信息适配器
     */
    private static class UserPasswordInfoAdapter implements PasswordPolicyService.UserPasswordInfo {
        private final SysUser user;
        
        public UserPasswordInfoAdapter(SysUser user) {
            this.user = user;
        }
        
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
    
}