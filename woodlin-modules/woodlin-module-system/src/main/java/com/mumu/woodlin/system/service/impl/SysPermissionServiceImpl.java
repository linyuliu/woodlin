package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.system.dto.RouteVO;
import com.mumu.woodlin.system.entity.SysPermission;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.mapper.SysPermissionMapper;
import com.mumu.woodlin.system.mapper.SysRoleMapper;
import com.mumu.woodlin.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限信息服务实现
 *
 * @author mumu
 * @description 权限信息服务实现，支持RBAC1权限继承。
 *              通过统一的 {@link #ensureUserCacheLoaded(Long)} 方法一次性加载并缓存
 *              用户所有权限（全部权限码、按钮权限、菜单权限、路由树），避免多次独立查库。
 *              使用分布式锁防止并发场景下缓存击穿（thundering herd）。
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
        implements ISysPermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    /**
     * 权限缓存服务（可选依赖，如果不存在则不使用缓存）
     */
    @Autowired(required = false)
    private com.mumu.woodlin.security.service.PermissionCacheService permissionCacheService;

    @Override
    public List<SysPermission> selectPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    public List<SysPermission> selectPermissionsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return permissionMapper.selectPermissionsByRoleIds(roleIds);
    }

    @Override
    public List<SysPermission> selectPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        if (isSuperAdminUser(userId)) {
            LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysPermission::getDeleted, "0")
                .eq(SysPermission::getStatus, "1")
                .orderByAsc(SysPermission::getSortOrder)
                .orderByAsc(SysPermission::getPermissionId);
            return list(wrapper);
        }
        return permissionMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<String> selectPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 使用统一的缓存加载，一次性加载所有权限类型
        com.mumu.woodlin.security.dto.UserCacheDto cache = ensureUserCacheLoaded(userId);
        if (cache != null && cache.getPermissions() != null) {
            return cache.getPermissions();
        }

        // 降级：直接查库
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }

        return permissions.stream()
            .map(SysPermission::getPermissionCode)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();
    }

    @Override
    public List<String> selectButtonPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 使用统一的缓存加载
        com.mumu.woodlin.security.dto.UserCacheDto cache = ensureUserCacheLoaded(userId);
        if (cache != null && cache.getButtonPermissions() != null) {
            return cache.getButtonPermissions();
        }

        // 降级：直接查库
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }

        return permissions.stream()
            .filter(p -> "F".equals(p.getPermissionType()))
            .map(SysPermission::getPermissionCode)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();
    }

    @Override
    public List<String> selectMenuPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 使用统一的缓存加载
        com.mumu.woodlin.security.dto.UserCacheDto cache = ensureUserCacheLoaded(userId);
        if (cache != null && cache.getMenuPermissions() != null) {
            return cache.getMenuPermissions();
        }

        // 降级：直接查库
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }

        return permissions.stream()
            .filter(p -> "M".equals(p.getPermissionType()) || "C".equals(p.getPermissionType()))
            .map(SysPermission::getPermissionCode)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();
    }

    @Override
    public List<RouteVO> selectRoutesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 使用统一的缓存加载
        com.mumu.woodlin.security.dto.UserCacheDto cache = ensureUserCacheLoaded(userId);
        if (cache != null && cache.getRoutes() != null) {
            try {
                @SuppressWarnings("unchecked")
                List<RouteVO> routes = (List<RouteVO>) cache.getRoutes();
                if (routes != null) {
                    return routes;
                }
            } catch (ClassCastException e) {
                log.warn("路由缓存类型转换失败，重新加载: userId={}", userId, e);
            }
        }

        // 降级：直接从数据库构建路由
        return buildRoutesFromDb(userId);
    }

    /**
     * 统一加载并缓存用户所有权限数据。
     * 使用 PermissionCacheService.getOrLoadUserCache() 防止并发缓存击穿：
     * 同一用户的多个并发请求中只有一个线程执行数据库查询，其他线程等待后读取缓存。
     * 一次查库同时填充所有权限（全部权限码、按钮权限、菜单权限、路由树）。
     *
     * @param userId 用户ID
     * @return 用户缓存对象
     */
    private com.mumu.woodlin.security.dto.UserCacheDto ensureUserCacheLoaded(Long userId) {
        if (permissionCacheService == null) {
            return null;
        }

        return permissionCacheService.getOrLoadUserCache(userId, () -> {
            log.info("统一加载用户权限: userId={}", userId);

            // 一次性从数据库查询用户所有权限
            List<SysPermission> allPermissions = selectPermissionsByUserId(userId);

            // 分类提取各种权限码
            List<String> permissionCodes = Collections.emptyList();
            List<String> buttonPermissions = Collections.emptyList();
            List<String> menuPermissions = Collections.emptyList();
            List<RouteVO> routeTree = Collections.emptyList();

            if (CollUtil.isNotEmpty(allPermissions)) {
                permissionCodes = allPermissions.stream()
                    .map(SysPermission::getPermissionCode)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .toList();

                buttonPermissions = allPermissions.stream()
                    .filter(p -> "F".equals(p.getPermissionType()))
                    .map(SysPermission::getPermissionCode)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .toList();

                menuPermissions = allPermissions.stream()
                    .filter(p -> "M".equals(p.getPermissionType()) || "C".equals(p.getPermissionType()))
                    .map(SysPermission::getPermissionCode)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .toList();

                // 构建路由树（包含菜单/目录/按钮全部节点，按钮作为叶子挂到菜单下）
                List<SysPermission> menuPerms = allPermissions.stream()
                    .filter(p -> "1".equals(p.getStatus()))
                    .sorted((p1, p2) -> {
                        int order1 = p1.getSortOrder() != null ? p1.getSortOrder() : 0;
                        int order2 = p2.getSortOrder() != null ? p2.getSortOrder() : 0;
                        return Integer.compare(order1, order2);
                    })
                    .toList();

                List<RouteVO> routes = menuPerms.stream()
                    .map(this::convertToRouteVO)
                    .toList();

                routeTree = buildRouteTree(routes, 0L);
            }

            log.info("统一加载用户权限完成: userId={}, 全部={}, 按钮={}, 菜单={}, 路由={}",
                userId, permissionCodes.size(), buttonPermissions.size(),
                menuPermissions.size(), routeTree.size());

            return com.mumu.woodlin.security.dto.UserCacheDto.builder()
                .userId(userId)
                .permissions(permissionCodes)
                .buttonPermissions(buttonPermissions)
                .menuPermissions(menuPermissions)
                .routes(routeTree)
                .build();
        });
    }

    /**
     * 从数据库直接构建路由（降级路径，不使用缓存）
     *
     * @param userId 用户ID
     * @return 路由列表
     */
    private List<RouteVO> buildRoutesFromDb(Long userId) {
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }

        List<SysPermission> menuPermissions = permissions.stream()
            .filter(p -> "1".equals(p.getStatus()))
            .sorted((p1, p2) -> {
                int order1 = p1.getSortOrder() != null ? p1.getSortOrder() : 0;
                int order2 = p2.getSortOrder() != null ? p2.getSortOrder() : 0;
                return Integer.compare(order1, order2);
            })
            .toList();

        List<RouteVO> routes = menuPermissions.stream()
            .map(this::convertToRouteVO)
            .toList();

        return buildRouteTree(routes, 0L);
    }

    /**
     * 将SysPermission转换为RouteVO（扁平结构，与前端 RouteItem 契约对齐）
     *
     * @param permission 权限信息
     * @return 路由VO
     */
    private RouteVO convertToRouteVO(SysPermission permission) {
        RouteVO route = new RouteVO();
        route.setId(permission.getPermissionId());
        route.setParentId(permission.getParentId() != null ? permission.getParentId() : 0L);
        route.setName(convertToRouteName(permission.getPermissionName()));
        route.setTitle(permission.getPermissionName());
        route.setPath(permission.getPath());
        route.setComponent(permission.getComponent());
        route.setRedirect(permission.getRedirect());
        route.setIcon(permission.getIcon());
        route.setPermission(permission.getPermissionCode());
        route.setIsHidden(!"1".equals(permission.getVisible()));
        route.setIsCache("1".equals(permission.getIsCache()));
        route.setIsFrame("1".equals(permission.getIsFrame()));
        route.setShowInTabs(!"0".equals(permission.getShowInTabs()));
        route.setActiveMenu(permission.getActiveMenu());
        route.setSort(permission.getSortOrder());

        // 节点类型映射：M-目录→1，C-菜单→2，F-按钮→3
        route.setType(switch (StrUtil.emptyToDefault(permission.getPermissionType(), "C")) {
            case "M" -> 1;
            case "F" -> 3;
            default -> 2;
        });

        return route;
    }

    /**
     * 将权限名称转换为 PascalCase 路由名称
     * 例如: 用户管理 → 用户管理（保留中文），或英文 system-user → SystemUser
     *
     * <p>若传入包含冒号分隔的权限码（如 system:user），则提取各段首字母大写拼接；
     * 否则直接 toCamelCase 后首字母大写作为路由名。前端以此区分同名路由。</p>
     *
     * @param name 权限名称或权限编码
     * @return 路由名称（PascalCase）
     */
    private String convertToRouteName(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        // 如果含冒号（权限码风格），则按冒号拆分转 PascalCase
        if (name.contains(":")) {
            return StrUtil.upperFirst(StrUtil.toCamelCase(name.replace(":", "_")));
        }
        return StrUtil.upperFirst(StrUtil.toCamelCase(name));
    }

    /**
     * 构建路由树形结构
     *
     * @param routes 路由列表
     * @param parentId 父ID
     * @return 树形路由列表
     */
    private List<RouteVO> buildRouteTree(List<RouteVO> routes, Long parentId) {
        List<RouteVO> tree = new ArrayList<>();

        // 按父ID分组
        Map<Long, List<RouteVO>> groupedByParent = routes.stream()
            .collect(Collectors.groupingBy(RouteVO::getParentId));

        // 构建树
        List<RouteVO> rootNodes = groupedByParent.getOrDefault(parentId, Collections.emptyList());
        for (RouteVO node : rootNodes) {
            List<RouteVO> children = groupedByParent.get(node.getId());
            if (CollUtil.isNotEmpty(children)) {
                node.setChildren(children);
                // 递归构建子节点的children
                buildChildrenRecursive(node, groupedByParent);
            }
            tree.add(node);
        }

        return tree;
    }

    /**
     * 递归构建子节点的children
     *
     * @param parent 父节点
     * @param groupedByParent 按父ID分组的路由Map
     */
    private void buildChildrenRecursive(RouteVO parent, Map<Long, List<RouteVO>> groupedByParent) {
        if (parent.getChildren() == null) {
            return;
        }

        for (RouteVO child : parent.getChildren()) {
            List<RouteVO> grandChildren = groupedByParent.get(child.getId());
            if (CollUtil.isNotEmpty(grandChildren)) {
                child.setChildren(grandChildren);
                buildChildrenRecursive(child, groupedByParent);
            }
        }
    }

    /**
     * 判断用户是否超级管理员
     *
     * @param userId 用户ID
     * @return 是否超级管理员
     */
    private boolean isSuperAdminUser(Long userId) {
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        if (CollUtil.isEmpty(roles)) {
            return false;
        }
        return roles.stream()
            .map(SysRole::getRoleCode)
            .filter(StrUtil::isNotBlank)
            .anyMatch(roleCode -> StrUtil.equals(roleCode, CommonConstant.SUPER_ADMIN_ROLE_CODE));
    }
}
