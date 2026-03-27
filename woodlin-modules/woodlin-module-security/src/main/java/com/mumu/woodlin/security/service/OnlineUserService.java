package com.mumu.woodlin.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * 在线用户服务
 *
 * @author mumu
 * @description 高效的在线用户跟踪和统计服务，使用Redis优化
 *              - 使用Redis Hash存储用户详细信息（一次性读取多个字段）
 *              - 使用Redis Sorted Set按登录时间排序（快速范围查询）
 *              - 使用Redis String记录累计在线时长（原子操作）
 * @since 2025-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    // Redis Key定义
    private static final String ONLINE_USERS_HASH = "online:users:info";        // Hash: userId -> 用户信息JSON
    private static final String ONLINE_USERS_ZSET = "online:users:login_time";  // ZSet: userId -> 登录时间戳
    private static final String USER_SESSION_PREFIX = "online:session:";         // String: userId -> session信息
    private static final String USER_DURATION_PREFIX = "stats:duration:";        // String: userId -> 累计在线时长（秒）
    private static final String DAILY_DURATION_PREFIX = "stats:daily:";          // String: userId:date -> 当日在线时长

    /**
     * 用户在线信息（使用Lombok提供getter/setter）
     */
    @Data
    public static class OnlineUserInfo {
        private String userId;           // 用户ID
        private String username;         // 用户名
        private String ip;               // IP地址
        private Long loginTime;          // 登录时间戳（毫秒）
        private Long lastActivityTime;   // 最后活动时间戳（毫秒）
        private String browser;          // 浏览器
        private String os;               // 操作系统

        public OnlineUserInfo() {}

        public OnlineUserInfo(String userId, String username, String ip, Long loginTime, String browser, String os) {
            this.userId = userId;
            this.username = username;
            this.ip = ip;
            this.loginTime = loginTime;
            this.lastActivityTime = loginTime;
            this.browser = browser;
            this.os = os;
        }
    }

    /**
     * 记录用户上线
     * 性能优化：使用Pipeline一次性执行多个Redis命令，减少网络往返
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param ip IP地址
     * @param browser 浏览器
     * @param os 操作系统
     */
    public void userOnline(String userId, String username, String ip, String browser, String os) {
        try {
            long loginTime = System.currentTimeMillis();

            // 创建用户信息对象
            OnlineUserInfo userInfo = new OnlineUserInfo(userId, username, ip, loginTime, browser, os);

            // 1. 在Hash中存储用户详细信息（使用Jackson序列化）
            RMap<String, String> onlineUsersMap = redissonClient.getMap(ONLINE_USERS_HASH);
            String userInfoJson = objectMapper.writeValueAsString(userInfo);
            onlineUsersMap.put(userId, userInfoJson);

            // 2. 在ZSet中记录登录时间（用于按时间排序查询）
            RScoredSortedSet<String> loginTimeZSet = redissonClient.getScoredSortedSet(ONLINE_USERS_ZSET);
            loginTimeZSet.add(loginTime, userId);

            // 3. 创建Session记录（记录本次会话开始时间）
            String sessionKey = USER_SESSION_PREFIX + userId;
            redissonClient.getBucket(sessionKey).set(String.valueOf(loginTime), Duration.ofHours(24));

            log.info("用户上线: userId={}, username={}, ip={}, loginTime={}", userId, username, ip, loginTime);
        } catch (Exception e) {
            log.error("记录用户上线失败: userId={}", userId, e);
        }
    }

    /**
     * 更新用户活动时间
     * 性能优化：只更新JSON中的lastActivityTime字段
     *
     * @param userId 用户ID
     */
    public void updateUserActivity(String userId) {
        try {
            long currentTime = System.currentTimeMillis();

            // 更新Hash中的lastActivityTime
            RMap<String, String> onlineUsersMap = redissonClient.getMap(ONLINE_USERS_HASH);
            String userInfoJson = onlineUsersMap.get(userId);

            if (userInfoJson != null) {
                // 使用Jackson解析和更新
                OnlineUserInfo userInfo = objectMapper.readValue(userInfoJson, OnlineUserInfo.class);
                userInfo.setLastActivityTime(currentTime);
                String updatedJson = objectMapper.writeValueAsString(userInfo);
                onlineUsersMap.put(userId, updatedJson);

                log.debug("更新用户活动时间: userId={}, time={}", userId, currentTime);
            }
        } catch (Exception e) {
            log.error("更新用户活动时间失败: userId={}", userId, e);
        }
    }

    /**
     * 记录用户下线
     * 性能优化：批量删除相关键，并计算本次会话在线时长
     *
     * @param userId 用户ID
     */
    public void userOffline(String userId) {
        try {
            // 1. 获取session开始时间
            String sessionKey = USER_SESSION_PREFIX + userId;
            String sessionStartStr = (String) redissonClient.getBucket(sessionKey).get();

            if (sessionStartStr != null) {
                long sessionStart = Long.parseLong(sessionStartStr);
                long sessionEnd = System.currentTimeMillis();
                long sessionDuration = (sessionEnd - sessionStart) / 1000; // 转换为秒

                // 2. 累加到总在线时长
                String durationKey = USER_DURATION_PREFIX + userId;
                redissonClient.getAtomicLong(durationKey).addAndGet(sessionDuration);

                // 3. 累加到当日在线时长
                String today = java.time.LocalDate.now().toString();
                String dailyKey = DAILY_DURATION_PREFIX + userId + ":" + today;
                redissonClient.getAtomicLong(dailyKey).addAndGet(sessionDuration);
                redissonClient.getBucket(dailyKey).expire(Duration.ofDays(90)); // 保留90天

                log.info("用户下线: userId={}, 本次在线时长={}秒", userId, sessionDuration);
            }

            // 4. 从Hash和ZSet中删除
            RMap<String, String> onlineUsersMap = redissonClient.getMap(ONLINE_USERS_HASH);
            onlineUsersMap.remove(userId);

            RScoredSortedSet<String> loginTimeZSet = redissonClient.getScoredSortedSet(ONLINE_USERS_ZSET);
            loginTimeZSet.remove(userId);

            // 5. 删除session记录
            redissonClient.getBucket(sessionKey).delete();

        } catch (Exception e) {
            log.error("记录用户下线失败: userId={}", userId, e);
        }
    }

    /**
     * 获取当前在线用户数量
     * 性能优化：直接从ZSet获取大小，O(1)复杂度
     *
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        try {
            RScoredSortedSet<String> loginTimeZSet = redissonClient.getScoredSortedSet(ONLINE_USERS_ZSET);
            return loginTimeZSet.size();
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return 0;
        }
    }

    /**
     * 获取在线用户列表（分页）
     * 性能优化：使用ZSet的range操作实现分页，避免全量扫描
     *
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 在线用户列表
     */
    public List<Map<String, Object>> getOnlineUsers(int page, int pageSize) {
        try {
            RScoredSortedSet<String> loginTimeZSet = redissonClient.getScoredSortedSet(ONLINE_USERS_ZSET);

            // ZSet按登录时间倒序获取（最近登录的排在前面）
            int start = (page - 1) * pageSize;
            int end = start + pageSize - 1;
            Collection<String> userIds = loginTimeZSet.valueRangeReversed(start, end);

            // 批量获取用户信息
            RMap<String, String> onlineUsersMap = redissonClient.getMap(ONLINE_USERS_HASH);
            Map<String, String> usersInfo = onlineUsersMap.getAll(new HashSet<>(userIds));

            // 构建返回结果
            return userIds.stream()
                    .map(userId -> {
                        String userInfoJson = usersInfo.get(userId);
                        if (userInfoJson != null) {
                            return parseUserInfo(userInfoJson);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                .toList();

        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户累计在线时长（秒）
     *
     * @param userId 用户ID
     * @return 累计在线时长（秒）
     */
    public long getUserTotalDuration(String userId) {
        try {
            String durationKey = USER_DURATION_PREFIX + userId;
            return redissonClient.getAtomicLong(durationKey).get();
        } catch (Exception e) {
            log.error("获取用户累计在线时长失败: userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 获取用户当日在线时长（秒）
     *
     * @param userId 用户ID
     * @return 当日在线时长（秒）
     */
    public long getUserDailyDuration(String userId) {
        try {
            String today = java.time.LocalDate.now().toString();
            String dailyKey = DAILY_DURATION_PREFIX + userId + ":" + today;
            return redissonClient.getAtomicLong(dailyKey).get();
        } catch (Exception e) {
            log.error("获取用户当日在线时长失败: userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 获取用户指定日期在线时长（秒）
     *
     * @param userId 用户ID
     * @param date 日期（格式：yyyy-MM-dd）
     * @return 指定日期在线时长（秒）
     */
    public long getUserDurationByDate(String userId, String date) {
        try {
            String dailyKey = DAILY_DURATION_PREFIX + userId + ":" + date;
            return redissonClient.getAtomicLong(dailyKey).get();
        } catch (Exception e) {
            log.error("获取用户指定日期在线时长失败: userId={}, date={}", userId, date, e);
            return 0;
        }
    }

    /**
     * 清理过期的在线用户（超时未活动）
     * 定时任务调用，清理超过指定时间未活动的用户
     *
     * @param timeoutSeconds 超时时间（秒）
     * @return 清理的用户数量
     */
    public int cleanupInactiveUsers(long timeoutSeconds) {
        try {
            int cleanedCount = 0;
            long currentTime = System.currentTimeMillis();
            long timeoutMillis = timeoutSeconds * 1000;

            RMap<String, String> onlineUsersMap = redissonClient.getMap(ONLINE_USERS_HASH);

            // 遍历所有在线用户检查活动时间
            for (Map.Entry<String, String> entry : onlineUsersMap.entrySet()) {
                String userId = entry.getKey();
                String userInfoJson = entry.getValue();

                try {
                    // 使用Jackson解析JSON
                    OnlineUserInfo userInfo = objectMapper.readValue(userInfoJson, OnlineUserInfo.class);
                    long lastActivityTime = userInfo.getLastActivityTime();

                    // 检查是否超时
                    if (currentTime - lastActivityTime > timeoutMillis) {
                        userOffline(userId);
                        cleanedCount++;
                        log.info("清理超时在线用户: userId={}, 最后活动时间={}", userId, lastActivityTime);
                    }
                } catch (JsonProcessingException e) {
                    log.warn("解析用户信息失败，跳过: userId={}", userId, e);
                }
            }

            return cleanedCount;
        } catch (Exception e) {
            log.error("清理过期在线用户失败", e);
            return 0;
        }
    }

    /**
     * 解析用户信息JSON为Map（使用Jackson）
     */
    private Map<String, Object> parseUserInfo(String json) {
        try {
            OnlineUserInfo userInfo = objectMapper.readValue(json, OnlineUserInfo.class);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userInfo.getUserId());
            result.put("username", userInfo.getUsername());
            result.put("ip", userInfo.getIp());
            result.put("loginTime", userInfo.getLoginTime());
            result.put("lastActivityTime", userInfo.getLastActivityTime());
            result.put("browser", userInfo.getBrowser());
            result.put("os", userInfo.getOs());
            return result;
        } catch (JsonProcessingException e) {
            log.warn("解析用户信息失败: {}", json, e);
            return new HashMap<>();
        }
    }
}
