package com.mumu.woodlin.admin.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.service.CaptchaService;
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
 * 验证码登录策略
 *
 * @author mumu
 * @description 使用用户名和图形验证码进行认证的登录方式
 *              适用于需要额外安全验证但不需要输入密码的快速登录场景
 *              验证码登录不进行密码策略检查
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaptchaLoginStrategy implements LoginStrategy {
    
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final CaptchaService captchaService;
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行验证码登录策略: username={}", loginRequest.getUsername());
        
        // 验证验证码
        if (StrUtil.isBlank(loginRequest.getUuid())) {
            log.warn("验证码登录失败: 验证码UUID不能为空, username={}", loginRequest.getUsername());
            throw BusinessException.of(ResultCode.CAPTCHA_ERROR, "验证码UUID不能为空");
        }
        
        if (StrUtil.isBlank(loginRequest.getCaptcha())) {
            log.warn("验证码登录失败: 验证码不能为空, username={}", loginRequest.getUsername());
            throw BusinessException.of(ResultCode.CAPTCHA_ERROR, "验证码不能为空");
        }
        
        if (!captchaService.verifyCaptcha(loginRequest.getUuid(), loginRequest.getCaptcha())) {
            log.warn("验证码登录失败: 验证码错误, username={}", loginRequest.getUsername());
            throw BusinessException.of(ResultCode.CAPTCHA_ERROR, "验证码错误或已过期");
        }
        
        // 查找用户
        SysUser user = userService.selectUserByUsername(loginRequest.getUsername());
        if (user == null) {
            log.warn("验证码登录失败: 用户不存在, username={}", loginRequest.getUsername());
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 检查用户状态
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            log.warn("验证码登录失败: 账号已被禁用, username={}", user.getUsername());
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
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
            .setMessage("验证码登录成功");
        
        log.info("验证码登录成功: username={}", user.getUsername());
        return response;
    }
    
    @Override
    public LoginType getLoginType() {
        return LoginType.CAPTCHA;
    }
    
    @Override
    public boolean validateRequest(LoginRequest loginRequest) {
        if (!LoginStrategy.super.validateRequest(loginRequest)) {
            return false;
        }
        // 验证必需的验证码登录字段
        return loginRequest.getUsername() != null && !loginRequest.getUsername().isBlank()
                && loginRequest.getCaptcha() != null && !loginRequest.getCaptcha().isBlank()
                && loginRequest.getUuid() != null && !loginRequest.getUuid().isBlank();
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
}
