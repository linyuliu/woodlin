package com.mumu.woodlin.system.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.system.dto.RouteVO;
import com.mumu.woodlin.system.entity.SysPermission;
import com.mumu.woodlin.system.mapper.SysPermissionMapper;
import com.mumu.woodlin.system.service.ISysPermissionService;

/**
 * 权限信息服务实现
 * 
 * @author mumu
 * @description 权限信息服务实现，支持RBAC1权限继承
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> 
        implements ISysPermissionService {
    
    private final SysPermissionMapper permissionMapper;
    
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
        return permissionMapper.selectPermissionsByUserId(userId);
    }
    
    @Override
    public List<String> selectPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 尝试从缓存获取
        if (permissionCacheService != null) {
            List<String> cachedPermissions = permissionCacheService.getUserPermissions(userId);
            if (cachedPermissions != null) {
                log.debug("从缓存获取用户权限: userId={}, count={}", userId, cachedPermissions.size());
                return cachedPermissions;
            }
        }
        
        // 从数据库查询
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        
        List<String> permissionCodes = permissions.stream()
            .map(SysPermission::getPermissionCode)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());
        
        // 缓存结果
        if (permissionCacheService != null) {
            permissionCacheService.cacheUserPermissions(userId, permissionCodes);
        }
        
        return permissionCodes;
    }
    
    @Override
    public List<RouteVO> selectRoutesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 尝试从缓存获取
        if (permissionCacheService != null) {
            List<RouteVO> cachedRoutes = permissionCacheService.getUserRoutes(userId);
            if (cachedRoutes != null) {
                log.debug("从缓存获取用户路由: userId={}, count={}", userId, cachedRoutes.size());
                return cachedRoutes;
            }
        }
        
        // 从数据库查询用户所有权限（包括继承的权限）
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        
        // 只保留菜单和目录（M-目录，C-菜单），过滤掉按钮权限（F-按钮）
        List<SysPermission> menuPermissions = permissions.stream()
            .filter(p -> "M".equals(p.getPermissionType()) || "C".equals(p.getPermissionType()))
            .filter(p -> "1".equals(p.getStatus())) // 只保留启用的权限
            .sorted((p1, p2) -> {
                // 按排序字段排序
                int order1 = p1.getSortOrder() != null ? p1.getSortOrder() : 0;
                int order2 = p2.getSortOrder() != null ? p2.getSortOrder() : 0;
                return Integer.compare(order1, order2);
            })
            .collect(Collectors.toList());
        
        // 转换为RouteVO并构建树形结构
        List<RouteVO> routes = menuPermissions.stream()
            .map(this::convertToRouteVO)
            .collect(Collectors.toList());
        
        // 构建树形结构
        List<RouteVO> routeTree = buildRouteTree(routes, 0L);
        
        // 缓存结果
        if (permissionCacheService != null) {
            permissionCacheService.cacheUserRoutes(userId, routeTree);
        }
        
        return routeTree;
    }
    
    /**
     * 将SysPermission转换为RouteVO
     * 
     * @param permission 权限信息
     * @return 路由VO
     */
    private RouteVO convertToRouteVO(SysPermission permission) {
        RouteVO route = new RouteVO();
        route.setId(permission.getPermissionId());
        route.setParentId(permission.getParentId());
        route.setName(convertToRouteName(permission.getPermissionCode()));
        route.setPath(permission.getPath());
        route.setComponent(permission.getComponent());
        
        // 设置meta信息
        RouteVO.RouteMeta meta = new RouteVO.RouteMeta();
        meta.setTitle(permission.getPermissionName());
        meta.setIcon(permission.getIcon());
        meta.setHideInMenu("0".equals(permission.getVisible()));
        meta.setKeepAlive("1".equals(permission.getIsCache()));
        meta.setIsFrame("1".equals(permission.getIsFrame()));
        meta.setOrder(permission.getSortOrder());
        
        // 设置权限标识
        if (StrUtil.isNotBlank(permission.getPermissionCode())) {
            meta.setPermissions(Collections.singletonList(permission.getPermissionCode()));
        }
        
        route.setMeta(meta);
        
        return route;
    }
    
    /**
     * 将权限编码转换为路由名称
     * 例如: system:user -> SystemUser
     * 
     * @param permissionCode 权限编码
     * @return 路由名称
     */
    private String convertToRouteName(String permissionCode) {
        if (StrUtil.isBlank(permissionCode)) {
            return null;
        }
        
        // 移除冒号，将每个单词首字母大写
        return StrUtil.toCamelCase(permissionCode.replace(":", "_"));
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
}
