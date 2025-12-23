package com.mumu.woodlin.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.admin.strategy.LoginStrategy;
import com.mumu.woodlin.common.constant.SystemConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.util.PasswordEncoderUtil;
import com.mumu.woodlin.security.dto.ChangePasswordRequest;
import com.mumu.woodlin.security.dto.ForgotPasswordRequest;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.service.AuthenticationService;
import com.mumu.woodlin.security.service.PasswordPolicyService;
import com.mumu.woodlin.security.service.SmsService;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SmsService smsService;

    /**
     * 权限缓存服务（可选依赖）
     */
    @Autowired(required = false)
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
        if (!PasswordEncoderUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.of(ResultCode.OLD_PASSWORD_ERROR, "旧密码错误");
        }

        // 验证新密码策略
        PasswordPolicyService.PasswordValidationResult validationResult =
            passwordPolicyService.validatePassword(request.getNewPassword());

        if (!validationResult.isValid()) {
            throw BusinessException.of(ResultCode.PASSWORD_POLICY_VIOLATION, validationResult.getMessage());
        }

        // 更新密码
        user.setPassword(PasswordEncoderUtil.encode(request.getNewPassword()));
        user.setPwdChangeTime(LocalDateTime.now());
        user.setIsFirstLogin(false);
        userService.updateById(user);
        
        // 清除用户相关缓存（包括权限缓存、用户信息缓存、会话缓存等）
        if (permissionCacheService != null) {
            permissionCacheService.evictUserCache(userId);
            log.info("已清除用户 {} 的权限缓存", userId);
        }
        
        // 强制用户重新登录（清除当前会话）
        StpUtil.logout(userId);
        log.info("用户 {} 修改密码成功，已强制重新登录", user.getUsername());
    }
    
    /**
     * 忘记密码重置（通过验证码，无需登录）
     *
     * @param request 忘记密码重置请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByCode(ForgotPasswordRequest request) {
        // 验证新密码和确认密码是否一致
        if (!StrUtil.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw BusinessException.of(ResultCode.PASSWORD_MISMATCH, "新密码和确认密码不一致");
        }
        
        // 查找用户（支持用户名、手机号、邮箱）
        SysUser user = findUserByIdentifier(request.getUsername());
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 验证验证码
        validateVerificationCode(request.getCodeType(), request.getUsername(), request.getCode());
        
        // 验证新密码策略
        PasswordPolicyService.PasswordValidationResult validationResult =
            passwordPolicyService.validatePassword(request.getNewPassword());
        
        if (!validationResult.isValid()) {
            throw BusinessException.of(ResultCode.PASSWORD_POLICY_VIOLATION, validationResult.getMessage());
        }
        
        // 更新密码
        user.setPassword(PasswordEncoderUtil.encode(request.getNewPassword()));
        user.setPwdChangeTime(LocalDateTime.now());
        user.setIsFirstLogin(false);
        user.setPwdErrorCount(0);  // 重置错误次数
        user.setLockTime(null);     // 解除锁定
        userService.updateById(user);
        
        // 清除用户相关缓存
        if (permissionCacheService != null) {
            permissionCacheService.evictUserCache(user.getUserId());
            log.info("已清除用户 {} 的权限缓存", user.getUserId());
        }
        
        // 强制用户重新登录（清除所有会话）
        StpUtil.logout(user.getUserId());
        log.info("用户 {} 通过验证码重置密码成功", user.getUsername());
    }
    
    /**
     * 根据标识符查找用户（支持用户名）
     *
     * @param identifier 标识符（用户名）
     * @return 用户信息
     */
    private SysUser findUserByIdentifier(String identifier) {
        return userService.selectUserByUsername(identifier);
    }
    
    /**
     * 验证验证码
     *
     * @param codeType 验证码类型
     * @param identifier 标识符（手机号或邮箱）
     * @param code 验证码
     */
    private void validateVerificationCode(String codeType, String identifier, String code) {
        boolean isValid = false;
        
        if ("sms".equals(codeType)) {
            // 验证短信验证码
            isValid = smsService.verifySmsCode(identifier, code);
        } else if ("email".equals(codeType)) {
            // 邮件验证码功能需要实现 EmailService
            log.warn("邮件验证码功能暂未实现");
            throw BusinessException.of(ResultCode.BAD_REQUEST, "邮件验证码功能暂未实现");
        } else {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "不支持的验证码类型");
        }
        
        if (!isValid) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }
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
