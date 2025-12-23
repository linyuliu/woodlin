package com.mumu.woodlin.admin.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.util.PasswordEncoderUtil;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.service.PasswordPolicyService;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 密码登录策略
 *
 * @author mumu
 * @description 使用用户名和密码进行认证的传统登录方式
 *              支持密码策略验证、账号锁定、密码错误次数限制等安全特性
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {
    
    private final ISysUserService userService;
    private final PasswordPolicyService passwordPolicyService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行密码登录策略: username={}", loginRequest.getUsername());
        
        // 查找用户
        SysUser user = userService.selectUserByUsername(loginRequest.getUsername());
        if (user == null) {
            log.warn("密码登录失败: 用户不存在, username={}", loginRequest.getUsername());
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            log.warn("密码登录失败: 账号已被禁用, username={}", user.getUsername());
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
        }
        
        // 创建用户密码信息适配器
        UserPasswordInfoAdapter userAdapter = new UserPasswordInfoAdapter(user);
        
        // 检查账号锁定
        if (passwordPolicyService.isAccountLocked(userAdapter)) {
            log.warn("密码登录失败: 账号已被锁定, username={}", user.getUsername());
            throw BusinessException.of(ResultCode.ACCOUNT_LOCKED, "账号已被锁定，请稍后再试");
        }
        
        // 验证密码
        if (!PasswordEncoderUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            // 处理密码错误
            handlePasswordError(user);
            log.warn("密码登录失败: 密码错误, username={}", user.getUsername());
            throw BusinessException.of(ResultCode.PASSWORD_ERROR, "用户名或密码错误");
        }
        
        // 验证密码策略
        PasswordPolicyService.PasswordValidationResult validationResult =
            passwordPolicyService.validateUserLogin(userAdapter);
        
        if (!validationResult.isValid()) {
            log.warn("密码登录失败: 密码策略验证失败, username={}, message={}", 
                    user.getUsername(), validationResult.getMessage());
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
        
        log.info("密码登录成功: username={}", user.getUsername());
        return response;
    }
    
    @Override
    public LoginType getLoginType() {
        return LoginType.PASSWORD;
    }
    
    @Override
    public boolean validateRequest(LoginRequest loginRequest) {
        if (!LoginStrategy.super.validateRequest(loginRequest)) {
            return false;
        }
        // 验证必需的密码登录字段
        return loginRequest.getUsername() != null && !loginRequest.getUsername().isBlank()
                && loginRequest.getPassword() != null && !loginRequest.getPassword().isBlank();
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
}
