package com.mumu.woodlin.security.service;

import cn.dev33.satoken.stp.StpUtil;
import com.mumu.woodlin.security.config.ActivityMonitoringProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户活动监控服务
 *
 * @author mumu
 * @description 监控用户活动，在用户超时后自动登出
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "woodlin.security.activity-monitoring.enabled", havingValue = "true", matchIfMissing = true)
public class UserActivityMonitoringService {

    private final ActivityMonitoringProperties activityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_ACTIVITY_KEY_PREFIX = "user_activity:";
    private static final String USER_WARNING_KEY_PREFIX = "user_warning:";

    /**
     * 记录用户活动
     *
     * @param userId 用户ID
     * @param activityType 活动类型（api、interaction）
     */
    public void recordActivity(String userId, String activityType) {
        if (!activityProperties.getEnabled()) {
            return;
        }

        // 检查是否需要监控此类型的活动
        if ("api".equals(activityType) && !activityProperties.getMonitorApiRequests()) {
            return;
        }
        if ("interaction".equals(activityType) && !activityProperties.getMonitorUserInteractions()) {
            return;
        }

        try {
            String activityKey = USER_ACTIVITY_KEY_PREFIX + userId;
            String currentTime = LocalDateTime.now().toString();
            
            // 记录最后活动时间，设置过期时间为超时时间的2倍（防止过早清理）
            redisTemplate.opsForValue().set(activityKey, currentTime, 
                activityProperties.getTimeoutSeconds() * 2, TimeUnit.SECONDS);
            
            log.debug("记录用户活动: userId={}, activityType={}, time={}", userId, activityType, currentTime);
        } catch (Exception e) {
            log.error("记录用户活动失败: userId={}, activityType={}", userId, activityType, e);
        }
    }

    /**
     * 检查用户是否超时
     *
     * @param userId 用户ID
     * @return 是否超时
     */
    public boolean isUserTimeout(String userId) {
        if (!activityProperties.getEnabled() || activityProperties.getTimeoutSeconds() <= 0) {
            return false;
        }

        try {
            String activityKey = USER_ACTIVITY_KEY_PREFIX + userId;
            String lastActivityTimeStr = (String) redisTemplate.opsForValue().get(activityKey);
            
            if (lastActivityTimeStr == null) {
                return true; // 没有活动记录，认为超时
            }
            
            LocalDateTime lastActivityTime = LocalDateTime.parse(lastActivityTimeStr);
            LocalDateTime now = LocalDateTime.now();
            long inactiveSeconds = java.time.Duration.between(lastActivityTime, now).getSeconds();
            
            return inactiveSeconds > activityProperties.getTimeoutSeconds();
        } catch (Exception e) {
            log.error("检查用户超时状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 检查是否需要发出警告
     *
     * @param userId 用户ID
     * @return 是否需要警告
     */
    public boolean shouldWarnUser(String userId) {
        if (!activityProperties.getEnabled() || activityProperties.getTimeoutSeconds() <= 0) {
            return false;
        }

        try {
            String activityKey = USER_ACTIVITY_KEY_PREFIX + userId;
            String warningKey = USER_WARNING_KEY_PREFIX + userId;
            String lastActivityTimeStr = (String) redisTemplate.opsForValue().get(activityKey);
            
            if (lastActivityTimeStr == null) {
                return false;
            }
            
            LocalDateTime lastActivityTime = LocalDateTime.parse(lastActivityTimeStr);
            LocalDateTime now = LocalDateTime.now();
            long inactiveSeconds = java.time.Duration.between(lastActivityTime, now).getSeconds();
            
            long warningThreshold = activityProperties.getTimeoutSeconds() - activityProperties.getWarningBeforeTimeoutSeconds();
            
            // 如果超过警告阈值且未发过警告
            if (inactiveSeconds > warningThreshold) {
                String warningTime = (String) redisTemplate.opsForValue().get(warningKey);
                if (warningTime == null) {
                    // 记录警告时间，避免重复警告
                    redisTemplate.opsForValue().set(warningKey, now.toString(), 
                        activityProperties.getWarningBeforeTimeoutSeconds(), TimeUnit.SECONDS);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查用户警告状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 强制用户登出
     *
     * @param userId 用户ID
     */
    public void forceLogout(String userId) {
        try {
            StpUtil.logout(userId);
            
            // 清理活动记录
            String activityKey = USER_ACTIVITY_KEY_PREFIX + userId;
            String warningKey = USER_WARNING_KEY_PREFIX + userId;
            redisTemplate.delete(activityKey);
            redisTemplate.delete(warningKey);
            
            log.info("用户因长时间无活动被强制登出: userId={}", userId);
        } catch (Exception e) {
            log.error("强制用户登出失败: userId={}", userId, e);
        }
    }

    /**
     * 获取配置信息
     *
     * @return 活动监控配置
     */
    public ActivityMonitoringProperties getConfig() {
        return activityProperties;
    }
}