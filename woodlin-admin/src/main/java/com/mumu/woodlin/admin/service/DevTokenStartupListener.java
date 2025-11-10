package com.mumu.woodlin.admin.service;

import cn.dev33.satoken.temp.SaTempUtil;
import com.mumu.woodlin.security.config.DevTokenProperties;
import com.mumu.woodlin.security.model.LoginUser;
import com.mumu.woodlin.security.service.DevTokenService;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 开发令牌启动监听器
 *
 * @author mumu
 * @description 在应用启动时自动生成开发调试令牌
 * @since 2025-01-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevTokenStartupListener {

    private final DevTokenProperties devTokenProperties;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;

    /**
     * 应用启动完成后生成开发令牌
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 检查是否启用开发令牌功能
        if (!devTokenProperties.getEnabled()) {
            return;
        }

        // 检查是否自动生成
        if (!devTokenProperties.getAutoGenerateOnStartup()) {
            log.info("开发令牌功能已启用，但未配置自动生成。请访问 /auth/dev-token 端点手动生成。");
            return;
        }

        try {
            // 生成开发令牌
            DevTokenService.DevTokenInfo tokenInfo = generateDevToken(devTokenProperties.getUsername());

            // 打印令牌信息
            printTokenInfo(tokenInfo);

        } catch (Exception e) {
            log.error("生成开发令牌失败: {}", e.getMessage(), e);
            log.warn("开发令牌生成失败，但应用继续启动。请检查用户 {} 是否存在。", devTokenProperties.getUsername());
        }
    }

    /**
     * 生成开发令牌
     *
     * @param username 用户名
     * @return 令牌信息
     */
    private DevTokenService.DevTokenInfo generateDevToken(String username) {
        log.info("正在为用户 {} 生成开发令牌...", username);

        // 查询用户
        SysUser user = userService.selectUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }

        // 构建登录用户信息
        LoginUser loginUser = buildLoginUser(user);

        String token = SaTempUtil.createToken("10014", 200);

        // 获取令牌
        Long timeout = SaTempUtil.getTimeout(token);

        // 构建令牌信息
        DevTokenService.DevTokenInfo tokenInfo = new DevTokenService.DevTokenInfo();
        tokenInfo.setUsername(user.getUsername());
        tokenInfo.setUserId(user.getUserId());
        tokenInfo.setToken(token);
        tokenInfo.setGenerateTime(LocalDateTime.now());
        tokenInfo.setExpiresIn(timeout);
        tokenInfo.setRoles(loginUser.getRoleCodes());
        tokenInfo.setPermissions(loginUser.getPermissions());
        log.info("开发令牌生成成功: userId={}, token={}", user.getUserId(), token);

        return tokenInfo;
    }

    /**
     * 构建登录用户信息
     *
     * @param user 用户实体
     * @return 登录用户信息
     */
    private LoginUser buildLoginUser(SysUser user) {
        // 查询用户的所有角色
        List<SysRole> roles = roleService.selectAllRolesByUserId(user.getUserId());

        // 提取角色ID和角色编码
        List<Long> roleIds = roles.stream()
            .map(SysRole::getRoleId)
            .collect(Collectors.toList());

        List<String> roleCodes = roles.stream()
            .map(SysRole::getRoleCode)
            .collect(Collectors.toList());

        // 查询用户的所有权限
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
            .setLoginIp("127.0.0.1");
    }

    /**
     * 打印令牌信息
     */
    private void printTokenInfo(DevTokenService.DevTokenInfo tokenInfo) {
        if (!devTokenProperties.getPrintToConsole()) {
            return;
        }

        String format = devTokenProperties.getDisplayFormat();

        if ("banner".equalsIgnoreCase(format)) {
            printBannerFormat(tokenInfo);
        } else {
            printSimpleFormat(tokenInfo);
        }
    }

    /**
     * 以横幅格式打印令牌信息
     */
    private void printBannerFormat(DevTokenService.DevTokenInfo tokenInfo) {
        StringBuilder banner = new StringBuilder();
        banner.append("\n");
        banner.append("╔═══════════════════════════════════════════════════════════════════════════════╗\n");
        banner.append("║                          开发调试令牌 (Development Token)                      ║\n");
        banner.append("╠═══════════════════════════════════════════════════════════════════════════════╣\n");
        banner.append(String.format("║ 用户名 (Username)    : %-54s ║\n", tokenInfo.getUsername()));
        banner.append(String.format("║ 令牌 (Token)         : %-54s ║\n", tokenInfo.getToken()));
        banner.append(String.format("║ 生成时间 (Time)       : %-54s ║\n", tokenInfo.getGenerateTime()));
        if (tokenInfo.getExpiresIn() != null && tokenInfo.getExpiresIn() > 0) {
            banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", formatExpireTime(tokenInfo.getExpiresIn())));
        } else {
            banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", "永不过期 (Never)"));
        }
        banner.append("╠═══════════════════════════════════════════════════════════════════════════════╣\n");
        banner.append("║ 使用方法 (Usage):                                                              ║\n");
        banner.append("║   1. 在请求头中添加: Authorization: " + tokenInfo.getToken());
        // 填充空格以对齐
        int padding = 79 - 42 - tokenInfo.getToken().length();
        for (int i = 0; i < padding; i++) {
            banner.append(" ");
        }
        banner.append("║\n");
        banner.append("║   2. 或访问 /auth/dev-token 端点重新生成                                       ║\n");
        banner.append("╚═══════════════════════════════════════════════════════════════════════════════╝\n");

        System.out.println(banner.toString());
        log.info("开发令牌已生成并打印到控制台");
    }

    /**
     * 以简单格式打印令牌信息
     */
    private void printSimpleFormat(DevTokenService.DevTokenInfo tokenInfo) {
        log.info("========== 开发调试令牌 ==========");
        log.info("用户名: {}", tokenInfo.getUsername());
        log.info("令牌: {}", tokenInfo.getToken());
        log.info("生成时间: {}", tokenInfo.getGenerateTime());
        if (tokenInfo.getExpiresIn() != null && tokenInfo.getExpiresIn() > 0) {
            log.info("过期时间: {}", formatExpireTime(tokenInfo.getExpiresIn()));
        } else {
            log.info("过期时间: 永不过期");
        }
        log.info("使用方法: 在请求头中添加 Authorization: {}", tokenInfo.getToken());
        log.info("================================");
    }

    /**
     * 格式化过期时间
     */
    private String formatExpireTime(Long seconds) {
        if (seconds < 0) {
            return "永不过期";
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return String.format("%d天%d小时", days, hours);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }
}
