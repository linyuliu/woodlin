package com.mumu.woodlin.security.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.config.DevTokenProperties;
import com.mumu.woodlin.security.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 开发令牌服务
 *
 * @author mumu
 * @description 为开发调试提供便捷的令牌生成和管理服务
 * @since 2025-01-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(DevTokenEnabledCondition.class)
public class DevTokenService {

    private final DevTokenProperties devTokenProperties;

    /**
     * 生成开发令牌
     *
     * @param username 用户名（如果为空，使用配置的默认用户名）
     * @param userService 用户服务
     * @param roleService 角色服务
     * @param permissionService 权限服务
     * @return 令牌信息
     */
    public DevTokenInfo generateDevToken(
            String username,
            Object userService,
            Object roleService,
            Object permissionService) {

        if (!devTokenProperties.getEnabled()) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "开发令牌功能未启用");
        }

        // 使用默认用户名如果未提供
        String actualUsername = StrUtil.isNotBlank(username)
            ? username
            : devTokenProperties.getUsername();

        log.info("正在为用户 {} 生成开发令牌...", actualUsername);

        // 获取用户信息并创建令牌
        // 这里需要动态调用传入的服务
        // 由于服务接口未暴露，使用反射或在实际实现中注入具体服务

        // 返回令牌信息
        DevTokenInfo tokenInfo = new DevTokenInfo();
        tokenInfo.setUsername(actualUsername);
        tokenInfo.setGenerateTime(LocalDateTime.now());

        return tokenInfo;
    }

    /**
     * 为指定用户创建令牌会话
     *
     * @param userId 用户ID
     * @param loginUser 登录用户信息
     * @return 生成的令牌
     */
    public String createTokenSession(Long userId, LoginUser loginUser) {
        // 登录并创建会话
        StpUtil.login(userId);

        // 设置会话数据
        StpUtil.getSession().set("USER_KEY", loginUser);

        // 获取令牌
        String token = StpUtil.getTokenValue();

        log.debug("已为用户 {} 创建令牌会话: {}", loginUser.getUsername(), token);

        return token;
    }

    /**
     * 检查开发令牌功能是否启用
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return devTokenProperties.getEnabled();
    }

    /**
     * 打印令牌信息到控制台
     *
     * @param tokenInfo 令牌信息
     */
    public void printTokenInfo(DevTokenInfo tokenInfo) {
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
    private void printBannerFormat(DevTokenInfo tokenInfo) {
        StringBuilder banner = new StringBuilder();
        banner.append("\n");
        banner.append("╔═══════════════════════════════════════════════════════════════════════════════╗\n");
        banner.append("║                          开发调试令牌 (Development Token)                      ║\n");
        banner.append("╠═══════════════════════════════════════════════════════════════════════════════╣\n");
        banner.append(String.format("║ 用户名 (Username)    : %-54s ║\n", tokenInfo.getUsername()));
        banner.append(String.format("║ 令牌 (Token)         : %-54s ║\n", truncateToken(tokenInfo.getToken())));
        banner.append(String.format("║ 生成时间 (Time)       : %-54s ║\n", tokenInfo.getGenerateTime()));
        if (Objects.nonNull(tokenInfo.getExpiresIn()) && tokenInfo.getExpiresIn() > 0) {
            banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", formatExpireTime(tokenInfo.getExpiresIn())));
        } else {
            banner.append(String.format("║ 过期时间 (Expires)    : %-54s ║\n", "永不过期 (Never)"));
        }
        banner.append("╠═══════════════════════════════════════════════════════════════════════════════╣\n");
        banner.append("║ 使用方法 (Usage):                                                              ║\n");
        banner.append(String.format("║   1. 在请求头中添加: Authorization: Bearer %s\n", tokenInfo.getToken()));
        banner.append("║   2. 或访问 /auth/dev-token 端点重新生成                                       ║\n");
        banner.append("╚═══════════════════════════════════════════════════════════════════════════════╝\n");

        // 使用logger输出banner，保持日志一致性
        log.info("{}", banner.toString());
    }

    /**
     * 以简单格式打印令牌信息
     */
    private void printSimpleFormat(DevTokenInfo tokenInfo) {
        log.info("========== 开发调试令牌 ==========");
        log.info("用户名: {}", tokenInfo.getUsername());
        log.info("令牌: {}", tokenInfo.getToken());
        log.info("生成时间: {}", tokenInfo.getGenerateTime());
        if (Objects.nonNull(tokenInfo.getExpiresIn()) && tokenInfo.getExpiresIn() > 0) {
            log.info("过期时间: {}", formatExpireTime(tokenInfo.getExpiresIn()));
        } else {
            log.info("过期时间: 永不过期");
        }
        log.info("使用方法: 在请求头中添加 Authorization: Bearer {}", tokenInfo.getToken());
        log.info("================================");
    }

    /**
     * 截断令牌用于显示
     */
    private String truncateToken(String token) {
        if (Objects.isNull(token)) {
            return "N/A";
        }
        if (token.length() > 50) {
            return token.substring(0, 47) + "...";
        }
        return token;
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

    /**
     * 开发令牌信息
     */
    @lombok.Data
    public static class DevTokenInfo {
        /**
         * 用户名
         */
        private String username;

        /**
         * 令牌
         */
        private String token;

        /**
         * 生成时间
         */
        private LocalDateTime generateTime;

        /**
         * 过期时间（秒）
         */
        private Long expiresIn;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 角色列表
         */
        private List<String> roles;

        /**
         * 权限列表
         */
        private List<String> permissions;
    }
}
