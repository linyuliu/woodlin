package com.mumu.woodlin.security.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.common.config.CacheProperties;
import com.mumu.woodlin.security.dto.UserCacheDto;

import lombok.RequiredArgsConstructor;

/**
 * 权限缓存服务
 *
 * @author mumu
 * @description 提供权限相关的缓存管理，支持RBAC1模型的用户权限和角色权限缓存
 *              优化后的缓存结构：用户相关信息合并为一个缓存对象，角色权限单独缓存
 * @since 2025-01-04
 */
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheService.class);

    private final RedissonClient redissonClient;
    private final CacheProperties cacheProperties;

    /**
     * 用户缓存key前缀（合并后的用户信息、角色、权限等）
     */
    private static final String USER_CACHE_PREFIX = "auth:user:";

    /**
     * 角色权限缓存key前缀（独立缓存）
     */
    private static final String ROLE_PERMISSION_PREFIX = "auth:role:permissions:";

    /**
     * 获取用户缓存（合并的用户信息、角色、权限等）
     *
     * @param userId 用户ID
     * @return 用户缓存对象，如果未缓存返回null
     */
    public UserCacheDto getUserCache(Long userId) {
        if (isDisabled()) {
            return null;
        }

        try {
            String key = USER_CACHE_PREFIX + userId;
            RBucket<UserCacheDto> bucket = redissonClient.getBucket(key);
            UserCacheDto cache = bucket.get();
            if (cache != null) {
                log.debug("从缓存获取用户信息: userId={}", userId);
            }
            return cache;
        } catch (Exception e) {
            log.error("获取用户缓存失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户信息（合并用户信息、角色、权限等）
     *
     * @param userCache 用户缓存对象
     */
    public void cacheUser(UserCacheDto userCache) {
        if (isDisabled() || Objects.isNull(userCache) || Objects.isNull(userCache.getUserId())) {
            return;
        }

        try {
            String key = USER_CACHE_PREFIX + userCache.getUserId();
            long expireSeconds = cacheProperties.getPermission().getExpireSeconds();
            userCache.setCacheTime(System.currentTimeMillis());
            
            RBucket<UserCacheDto> bucket = redissonClient.getBucket(key);
            bucket.set(userCache, Duration.ofSeconds(expireSeconds));
            
            log.debug("缓存用户信息成功: userId={}, roles={}, permissions={}", 
                userCache.getUserId(), 
                userCache.getRoles() != null ? userCache.getRoles().size() : 0,
                userCache.getPermissions() != null ? userCache.getPermissions().size() : 0);
        } catch (Exception e) {
            log.error("缓存用户信息失败: userId={}", userCache.getUserId(), e);
        }
    }

    /**
     * 获取用户权限列表（从合并缓存中提取）
     *
     * @param userId 用户ID
     * @return 权限列表，如果未缓存返回null
     */
    public List<String> getUserPermissions(Long userId) {
        UserCacheDto cache = getUserCache(userId);
        return cache != null ? cache.getPermissions() : null;
    }

    /**
     * 获取用户角色列表（从合并缓存中提取）
     *
     * @param userId 用户ID
     * @return 角色编码列表，如果未缓存返回null
     */
    public List<String> getUserRoles(Long userId) {
        UserCacheDto cache = getUserCache(userId);
        return cache != null ? cache.getRoles() : null;
    }
    
    /**
     * 获取用户按钮权限列表（从合并缓存中提取）
     *
     * @param userId 用户ID
     * @return 按钮权限列表，如果未缓存返回null
     */
    public List<String> getUserButtonPermissions(Long userId) {
        UserCacheDto cache = getUserCache(userId);
        return cache != null ? cache.getButtonPermissions() : null;
    }
    
    /**
     * 获取用户菜单权限列表（从合并缓存中提取）
     *
     * @param userId 用户ID
     * @return 菜单权限列表，如果未缓存返回null
     */
    public List<String> getUserMenuPermissions(Long userId) {
        UserCacheDto cache = getUserCache(userId);
        return cache != null ? cache.getMenuPermissions() : null;
    }

    /**
     * 获取用户路由（从合并缓存中提取）
     *
     * @param userId 用户ID
     * @return 路由列表，如果未缓存返回null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getUserRoutes(Long userId) {
        UserCacheDto cache = getUserCache(userId);
        if (cache != null && cache.getRoutes() != null) {
            try {
                return (List<T>) cache.getRoutes();
            } catch (ClassCastException e) {
                log.error("用户路由类型转换失败: userId={}", userId, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 缓存用户权限（更新合并缓存中的权限信息）
     * 
     * @deprecated 使用 {@link #cacheUser(UserCacheDto)} 代替
     */
    @Deprecated
    public void cacheUserPermissions(Long userId, List<String> permissions) {
        UserCacheDto cache = getUserCache(userId);
        if (cache == null) {
            cache = UserCacheDto.builder().userId(userId).build();
        }
        cache.setPermissions(permissions);
        cacheUser(cache);
    }

    /**
     * 缓存用户角色（更新合并缓存中的角色信息）
     * 
     * @deprecated 使用 {@link #cacheUser(UserCacheDto)} 代替
     */
    @Deprecated
    public void cacheUserRoles(Long userId, List<String> roleCodes) {
        UserCacheDto cache = getUserCache(userId);
        if (cache == null) {
            cache = UserCacheDto.builder().userId(userId).build();
        }
        cache.setRoles(roleCodes);
        cacheUser(cache);
    }
    
    /**
     * 缓存用户按钮权限（更新合并缓存中的按钮权限信息）
     * 
     * @deprecated 使用 {@link #cacheUser(UserCacheDto)} 代替
     */
    @Deprecated
    public void cacheUserButtonPermissions(Long userId, List<String> buttonPermissions) {
        UserCacheDto cache = getUserCache(userId);
        if (cache == null) {
            cache = UserCacheDto.builder().userId(userId).build();
        }
        cache.setButtonPermissions(buttonPermissions);
        cacheUser(cache);
    }
    
    /**
     * 缓存用户菜单权限（更新合并缓存中的菜单权限信息）
     * 
     * @deprecated 使用 {@link #cacheUser(UserCacheDto)} 代替
     */
    @Deprecated
    public void cacheUserMenuPermissions(Long userId, List<String> menuPermissions) {
        UserCacheDto cache = getUserCache(userId);
        if (cache == null) {
            cache = UserCacheDto.builder().userId(userId).build();
        }
        cache.setMenuPermissions(menuPermissions);
        cacheUser(cache);
    }

    /**
     * 缓存用户路由（更新合并缓存中的路由信息）
     * 
     * @deprecated 使用 {@link #cacheUser(UserCacheDto)} 代替
     */
    @Deprecated
    public <T> void cacheUserRoutes(Long userId, List<T> routes) {
        UserCacheDto cache = getUserCache(userId);
        if (cache == null) {
            cache = UserCacheDto.builder().userId(userId).build();
        }
        cache.setRoutes(routes);
        cacheUser(cache);
    }

    /**
     * 获取角色权限缓存
     *
     * @param roleId 角色ID
     * @return 权限列表，如果未缓存返回null
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolePermissions(Long roleId) {
        if (isDisabled()) {
            return null;
        }

        try {
            String key = ROLE_PERMISSION_PREFIX + roleId;
            RBucket<List<String>> bucket = redissonClient.getBucket(key);
            return bucket.get();
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
        if (isDisabled() || Objects.isNull(permissions)) {
            return;
        }

        try {
            String key = ROLE_PERMISSION_PREFIX + roleId;
            long expireSeconds = cacheProperties.getPermission().getRoleExpireSeconds();
            RBucket<List<String>> bucket = redissonClient.getBucket(key);
            bucket.set(permissions, Duration.ofSeconds(expireSeconds));
            log.debug("缓存角色权限成功: roleId={}, count={}", roleId, permissions.size());
        } catch (Exception e) {
            log.error("缓存角色权限失败: roleId={}", roleId, e);
        }
    }

    /**
     * 清除用户的所有缓存（权限、角色和路由缓存）
     *
     * @param userId 用户ID
     */
    public void evictUserCache(Long userId) {
        try {
            String key = USER_CACHE_PREFIX + userId;
            
            if (cacheProperties.getDelayedDoubleDelete().getEnabled()) {
                deleteWithDelayedDoubleDelete(key);
            } else {
                RBucket<Object> bucket = redissonClient.getBucket(key);
                bucket.delete();
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
                RBucket<Object> bucket = redissonClient.getBucket(key);
                bucket.delete();
            }
            
            log.info("清除角色缓存: roleId={}", roleId);
        } catch (Exception e) {
            log.error("清除角色缓存失败: roleId={}", roleId, e);
        }
    }

    /**
     * 清除所有用户缓存（权限变更时使用）
     */
    public void evictAllUserPermissions() {
        try {
            RKeys keys = redissonClient.getKeys();
            long deleteCount = keys.deleteByPattern(USER_CACHE_PREFIX + "*");
            log.info("清除所有用户缓存，删除数量: {}", deleteCount);
        } catch (Exception e) {
            log.error("清除所有用户缓存失败", e);
        }
    }

    /**
     * 清除所有角色权限缓存（权限变更时使用）
     */
    public void evictAllRolePermissions() {
        try {
            // 使用Redisson的deleteByPattern清理所有角色权限缓存
            RKeys keys = redissonClient.getKeys();
            long deleteCount = keys.deleteByPattern(ROLE_PERMISSION_PREFIX + "*");
            log.info("清除所有角色权限缓存，删除数量: {}", deleteCount);
        } catch (Exception e) {
            log.error("清除所有角色权限缓存失败", e);
        }
    }
    
    /**
     * 批量清除用户缓存
     *
     * @param userIds 用户ID列表
     */
    public void evictUserCacheBatch(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        try {
            for (Long userId : userIds) {
                evictUserCache(userId);
            }
            log.info("批量清除用户缓存: count={}", userIds.size());
        } catch (Exception e) {
            log.error("批量清除用户缓存失败", e);
        }
    }
    
    /**
     * 批量清除角色缓存
     *
     * @param roleIds 角色ID列表
     */
    public void evictRoleCacheBatch(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        
        try {
            for (Long roleId : roleIds) {
                evictRoleCache(roleId);
            }
            log.info("批量清除角色缓存: count={}", roleIds.size());
        } catch (Exception e) {
            log.error("批量清除角色缓存失败", e);
        }
    }
    
    /**
     * 预热用户缓存（批量加载用户权限到缓存）
     *
     * @param userId 用户ID
     * @param permissions 所有权限列表
     * @param buttonPermissions 按钮权限列表
     * @param menuPermissions 菜单权限列表
     * @param roles 角色编码列表
     */
    public void warmupUserCache(Long userId, List<String> permissions, 
                                List<String> buttonPermissions, 
                                List<String> menuPermissions,
                                List<String> roles) {
        if (isDisabled()) {
            return;
        }
        
        try {
            UserCacheDto userCache = UserCacheDto.builder()
                    .userId(userId)
                    .permissions(permissions != null ? permissions : Collections.emptyList())
                    .buttonPermissions(buttonPermissions != null ? buttonPermissions : Collections.emptyList())
                    .menuPermissions(menuPermissions != null ? menuPermissions : Collections.emptyList())
                    .roles(roles != null ? roles : Collections.emptyList())
                    .build();
            
            cacheUser(userCache);
            
            log.info("预热用户缓存成功: userId={}, 权限={}, 按钮={}, 菜单={}, 角色={}", 
                userId, 
                permissions != null ? permissions.size() : 0,
                buttonPermissions != null ? buttonPermissions.size() : 0,
                menuPermissions != null ? menuPermissions.size() : 0,
                roles != null ? roles.size() : 0);
        } catch (Exception e) {
            log.error("预热用户缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 判断权限缓存是否未启用（注意：返回true表示缓存未启用）
     *
     * @return true-缓存未启用, false-缓存已启用
     */
    private boolean isDisabled() {
        return Objects.isNull(cacheProperties.getPermission())
            || !Boolean.TRUE.equals(cacheProperties.getPermission().getEnabled());
    }

    /**
     * 延迟双删策略
     * 先删除一次，延迟后再删除一次，防止缓存与数据库不一致
     *
     * @param cacheKey 缓存键
     */
    private void deleteWithDelayedDoubleDelete(String cacheKey) {
        // 第一次删除
        RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
        boolean firstDelete = bucket.delete();
        log.debug("第一次删除缓存: {}, 结果: {}", cacheKey, firstDelete);
        
        // 异步延迟第二次删除
        long delayMillis = cacheProperties.getDelayedDoubleDelete().getDelayMillis();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMillis);
                RBucket<Object> delayBucket = redissonClient.getBucket(cacheKey);
                boolean secondDelete = delayBucket.delete();
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
