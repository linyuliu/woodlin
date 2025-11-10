package com.mumu.woodlin.security.service;

import com.mumu.woodlin.common.config.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyScanOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存服务
 *
 * @author mumu
 * @description 提供权限相关的缓存管理，支持用户权限和角色权限的缓存
 * @since 2025-01-04
 */
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;

    /**
     * 用户权限缓存key前缀
     */
    private static final String USER_PERMISSION_PREFIX = "auth:user:permissions:";

    /**
     * 用户角色缓存key前缀
     */
    private static final String USER_ROLE_PREFIX = "auth:user:roles:";

    /**
     * 角色权限缓存key前缀
     */
    private static final String ROLE_PERMISSION_PREFIX = "auth:role:permissions:";

    /**
     * 获取用户权限缓存
     *
     * @param userId 用户ID
     * @return 权限列表，如果未缓存返回null
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(Long userId) {
        if (isEnabled()) {
            return null;
        }

        try {
            String key = USER_PERMISSION_PREFIX + userId;
            return (List<String>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户权限缓存失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户权限
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     */
    public void cacheUserPermissions(Long userId, List<String> permissions) {
        if (isEnabled() || Objects.isNull(permissions)) {
            return;
        }

        try {
            String key = USER_PERMISSION_PREFIX + userId;
            long expireSeconds = cacheProperties.getPermission().getExpireSeconds();
            redisTemplate.opsForValue().set(key, permissions, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存用户权限成功: userId={}, count={}", userId, permissions.size());
        } catch (Exception e) {
            log.error("缓存用户权限失败: userId={}", userId, e);
        }
    }

    /**
     * 获取用户角色缓存
     *
     * @param userId 用户ID
     * @return 角色编码列表，如果未缓存返回null
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(Long userId) {
        if (isEnabled()) {
            return null;
        }

        try {
            String key = USER_ROLE_PREFIX + userId;
            return (List<String>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户角色缓存失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户角色
     *
     * @param userId 用户ID
     * @param roleCodes 角色编码列表
     */
    public void cacheUserRoles(Long userId, List<String> roleCodes) {
        if (isEnabled() || Objects.isNull(roleCodes)) {
            return;
        }

        try {
            String key = USER_ROLE_PREFIX + userId;
            long expireSeconds = cacheProperties.getPermission().getExpireSeconds();
            redisTemplate.opsForValue().set(key, roleCodes, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存用户角色成功: userId={}, count={}", userId, roleCodes.size());
        } catch (Exception e) {
            log.error("缓存用户角色失败: userId={}", userId, e);
        }
    }

    /**
     * 获取角色权限缓存
     *
     * @param roleId 角色ID
     * @return 权限列表，如果未缓存返回null
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolePermissions(Long roleId) {
        if (isEnabled()) {
            return null;
        }

        try {
            String key = ROLE_PERMISSION_PREFIX + roleId;
            return (List<String>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取角色权限缓存失败: roleId={}", roleId, e);
            return null;
        }
    }

    /**
     * 缓存角色权限
     *
     * @param roleId 角色ID
     * @param permissions 权限列表
     */
    public void cacheRolePermissions(Long roleId, List<String> permissions) {
        if (isEnabled() || Objects.isNull(permissions)) {
            return;
        }

        try {
            String key = ROLE_PERMISSION_PREFIX + roleId;
            long expireSeconds = cacheProperties.getPermission().getRoleExpireSeconds();
            redisTemplate.opsForValue().set(key, permissions, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存角色权限成功: roleId={}, count={}", roleId, permissions.size());
        } catch (Exception e) {
            log.error("缓存角色权限失败: roleId={}", roleId, e);
        }
    }

    /**
     * 清除用户的权限和角色缓存
     *
     * @param userId 用户ID
     */
    public void evictUserCache(Long userId) {
        try {
            String permKey = USER_PERMISSION_PREFIX + userId;
            String roleKey = USER_ROLE_PREFIX + userId;
            
            if (cacheProperties.getDelayedDoubleDelete().getEnabled()) {
                // 延迟双删策略
                deleteWithDelayedDoubleDelete(permKey);
                deleteWithDelayedDoubleDelete(roleKey);
            } else {
                // 直接删除
                redisTemplate.delete(permKey);
                redisTemplate.delete(roleKey);
            }
            
            log.info("清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 清除角色的权限缓存
     *
     * @param roleId 角色ID
     */
    public void evictRoleCache(Long roleId) {
        try {
            String key = ROLE_PERMISSION_PREFIX + roleId;
            
            if (cacheProperties.getDelayedDoubleDelete().getEnabled()) {
                // 延迟双删策略
                deleteWithDelayedDoubleDelete(key);
            } else {
                // 直接删除
                redisTemplate.delete(key);
            }
            
            log.info("清除角色缓存: roleId={}", roleId);
        } catch (Exception e) {
            log.error("清除角色缓存失败: roleId={}", roleId, e);
        }
    }

    /**
     * 清除所有用户权限缓存（权限变更时使用）
     */
    public void evictAllUserPermissions() {
        try {
            // 使用scan清理所有用户权限缓存（避免KEYS命令阻塞Redis）
            scanAndDelete(USER_PERMISSION_PREFIX + "*");
            log.info("清除所有用户权限缓存");
        } catch (Exception e) {
            log.error("清除所有用户权限缓存失败", e);
        }
    }

    /**
     * 清除所有角色权限缓存（权限变更时使用）
     */
    public void evictAllRolePermissions() {
        try {
            // 使用scan清理所有角色权限缓存（避免KEYS命令阻塞Redis）
            scanAndDelete(ROLE_PERMISSION_PREFIX + "*");
            log.info("清除所有角色权限缓存");
        } catch (Exception e) {
            log.error("清除所有角色权限缓存失败", e);
        }
    }

    /**
     * 判断权限缓存是否启用
     *
     * @return 是否启用
     */
    private boolean isEnabled() {
        return Objects.isNull(cacheProperties.getPermission())
            || !Boolean.TRUE.equals(cacheProperties.getPermission().getEnabled());
    }

    /**
     * 使用SCAN命令批量删除键（避免KEYS命令阻塞Redis）
     *
     * @param pattern 键模式
     */
    private void scanAndDelete(String pattern) {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.commands().scan(KeyScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    connection.commands().del(cursor.next());
                }
            } catch (Exception e) {
                log.error("关闭Redis SCAN游标失败", e);
            }
            // 关闭游标

            return null;
        });
    }

    /**
     * 延迟双删策略
     * 先删除一次，延迟后再删除一次，防止缓存与数据库不一致
     *
     * @param cacheKey 缓存键
     */
    private void deleteWithDelayedDoubleDelete(String cacheKey) {
        // 第一次删除
        Boolean firstDelete = redisTemplate.delete(cacheKey);
        log.debug("第一次删除缓存: {}, 结果: {}", cacheKey, firstDelete);
        
        // 异步延迟第二次删除
        long delayMillis = cacheProperties.getDelayedDoubleDelete().getDelayMillis();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMillis);
                Boolean secondDelete = redisTemplate.delete(cacheKey);
                log.debug("第二次删除缓存: {}, 结果: {}", cacheKey, secondDelete);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("延迟双删被中断: {}", cacheKey, e);
            } catch (Exception e) {
                log.error("延迟双删失败: {}", cacheKey, e);
            }
        });
        
        log.info("已触发延迟双删: {}, 延迟: {}ms", cacheKey, delayMillis);
    }
}
