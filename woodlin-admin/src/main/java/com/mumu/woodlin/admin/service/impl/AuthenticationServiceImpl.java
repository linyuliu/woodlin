package com.mumu.woodlin.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.service.AuthenticationService;
import com.mumu.woodlin.security.service.PasswordPolicyService;
import com.mumu.woodlin.admin.strategy.LoginStrategy;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author mumu
 * @description 提供用户认证、登录、登出、密码管理等服务实现
 *              支持多种登录方式：密码登录、验证码登录、手机号登录、SSO登录、Passkey登录、TOTP认证
 *              使用策略模式实现不同的登录方式
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ISysUserService userService;
    private final PasswordPolicyService passwordPolicyService;
    private final List<LoginStrategy> loginStrategies;

    /**
     * 权限缓存服务（可选依赖）
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.mumu.woodlin.security.service.PermissionCacheService permissionCacheService;
    
    /**
     * 登录策略映射表（按登录类型索引）
     */
    private Map<LoginType, LoginStrategy> strategyMap;

    /**
     * 初始化策略映射表
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        strategyMap = loginStrategies.stream()
            .collect(Collectors.toMap(LoginStrategy::getLoginType, Function.identity()));
        log.info("已加载 {} 种登录策略: {}", strategyMap.size(), 
                strategyMap.keySet().stream().map(LoginType::getDescription).collect(Collectors.joining(", ")));
    }

    /**
     * 用户登录 - 根据登录类型选择相应的策略
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest loginRequest) {
        // 获取登录类型
        LoginType loginType = loginRequest.getLoginTypeEnum();
        if (loginType == null) {
            log.warn("登录失败: 不支持的登录类型, loginType={}", loginRequest.getLoginType());
            throw BusinessException.of(ResultCode.BAD_REQUEST, "不支持的登录类型");
        }
        
        // 获取对应的登录策略
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            log.warn("登录失败: 未找到对应的登录策略, loginType={}", loginType.getDescription());
            throw BusinessException.of(ResultCode.BAD_REQUEST, "登录类型暂不支持");
        }
        
        // 验证请求参数
        if (!strategy.validateRequest(loginRequest)) {
            log.warn("登录失败: 登录请求参数验证失败, loginType={}", loginType.getDescription());
            throw BusinessException.of(ResultCode.BAD_REQUEST, "登录请求参数不完整或不正确");
        }
        
        // 执行登录策略
        log.info("开始执行登录: loginType={}", loginType.getDescription());
        return strategy.login(loginRequest);
    }

    /**
     * dev环境用户登录（开发调试用）
     *
     * @param username 用户名
     * @return 登录响应
     */
    @Override
    public LoginResponse devLogin(String username) {
        log.info("执行开发环境登录: username={}", username);
        SysUser user = userService.selectUserByUsername(username);
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 使用Sa-Token登录
        StpUtil.login(user.getUserId(), false);
        StpUtil.getSession().set(SecurityUtil.USER_KEY, user);
        String token = StpUtil.getTokenValue();
        
        // 构建响应
        return new LoginResponse()
            .setToken(token)
            .setExpiresIn(StpUtil.getTokenTimeout())
            .setMessage("开发环境登录成功");
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
        user.setIsFirstLogin(false);
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
}
