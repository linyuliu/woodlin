package com.mumu.woodlin.admin.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.service.SmsService;
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

/**
 * 手机号短信登录策略
 *
 * @author mumu
 * @description 使用手机号和短信验证码进行认证的登录方式
 *              适用于移动端和快速登录场景，无需记住密码
 *              通过验证手机短信验证码来确认用户身份
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MobileSmsLoginStrategy implements LoginStrategy {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final SmsService smsService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("执行手机号短信登录策略: mobile={}", loginRequest.getMobile());

        // 验证短信验证码
        if (!smsService.verifySmsCode(loginRequest.getMobile(), loginRequest.getSmsCode())) {
            log.warn("手机号登录失败: 短信验证码错误或已过期, mobile={}", loginRequest.getMobile());
            throw BusinessException.of(ResultCode.CAPTCHA_ERROR, "短信验证码错误或已过期");
        }

        // 根据手机号查找用户（使用Lambda查询）
        SysUser user = userService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMobile, loginRequest.getMobile())
                .last("LIMIT 1"));

        if (user == null) {
            log.warn("手机号登录失败: 用户不存在, mobile={}", loginRequest.getMobile());
            // 清除验证码
            smsService.clearSmsCode(loginRequest.getMobile());
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "手机号未注册");
        }

        // 检查用户状态
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            log.warn("手机号登录失败: 账号已被禁用, mobile={}", loginRequest.getMobile());
            // 清除验证码
            smsService.clearSmsCode(loginRequest.getMobile());
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        // 验证成功，清除验证码
        smsService.clearSmsCode(loginRequest.getMobile());

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
            .setMessage("手机号登录成功");

        log.info("手机号登录成功: mobile={}, username={}", loginRequest.getMobile(), user.getUsername());
        return response;
    }

    @Override
    public LoginType getLoginType() {
        return LoginType.MOBILE_SMS;
    }

    @Override
    public boolean validateRequest(LoginRequest loginRequest) {
        if (!LoginStrategy.super.validateRequest(loginRequest)) {
            return false;
        }
        // 验证必需的手机号登录字段
        return loginRequest.getMobile() != null && !loginRequest.getMobile().isBlank()
                && loginRequest.getSmsCode() != null && !loginRequest.getSmsCode().isBlank();
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
            .toList();

        List<String> roleCodes = roles.stream()
            .map(SysRole::getRoleCode)
            .toList();

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
