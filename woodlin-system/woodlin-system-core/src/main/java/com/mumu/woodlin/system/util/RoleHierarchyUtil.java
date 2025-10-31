package com.mumu.woodlin.system.util;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysRoleHierarchy;

/**
 * 角色层次结构工具类
 * 
 * @author mumu
 * @description 提供角色继承层次相关的工具方法，支持RBAC1
 * @since 2025-10-31
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleHierarchyUtil {
    
    /**
     * 最大角色层级深度（防止无限递归）
     */
    public static final int MAX_ROLE_DEPTH = 10;
    
    /**
     * 路径分隔符
     */
    public static final String PATH_SEPARATOR = "/";
    
    /**
     * 构建角色路径
     * 
     * @param parentPath 父角色路径
     * @param roleId 当前角色ID
     * @return 角色路径
     */
    public static String buildRolePath(String parentPath, Long roleId) {
        if (StrUtil.isBlank(parentPath)) {
            return PATH_SEPARATOR + roleId + PATH_SEPARATOR;
        }
        // 移除尾部斜杠（如果有）
        String normalizedPath = parentPath.endsWith(PATH_SEPARATOR) ? 
            parentPath.substring(0, parentPath.length() - 1) : parentPath;
        return normalizedPath + PATH_SEPARATOR + roleId + PATH_SEPARATOR;
    }
    
    /**
     * 从路径中提取角色ID列表
     * 
     * @param rolePath 角色路径
     * @return 角色ID列表
     */
    public static List<Long> extractRoleIdsFromPath(String rolePath) {
        if (StrUtil.isBlank(rolePath)) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(rolePath.split(PATH_SEPARATOR))
            .filter(StrUtil::isNotBlank)
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }
    
    /**
     * 计算角色层级
     * 
     * @param rolePath 角色路径
     * @return 角色层级（0为顶级）
     */
    public static int calculateRoleLevel(String rolePath) {
        if (StrUtil.isBlank(rolePath)) {
            return 0;
        }
        
        List<Long> roleIds = extractRoleIdsFromPath(rolePath);
        return Math.max(0, roleIds.size() - 1);
    }
    
    /**
     * 构建角色层次关系（闭包表记录）
     * 
     * @param roleId 当前角色ID
     * @param ancestorRoleIds 祖先角色ID列表
     * @param tenantId 租户ID
     * @return 角色层次关系列表
     */
    public static List<SysRoleHierarchy> buildHierarchies(Long roleId, List<Long> ancestorRoleIds, String tenantId) {
        List<SysRoleHierarchy> hierarchies = new ArrayList<>();
        
        // 添加自己到自己的关系（距离为0）
        hierarchies.add(new SysRoleHierarchy()
            .setAncestorRoleId(roleId)
            .setDescendantRoleId(roleId)
            .setDistance(0)
            .setTenantId(tenantId));
        
        // 添加祖先关系
        if (CollUtil.isNotEmpty(ancestorRoleIds)) {
            for (int i = 0; i < ancestorRoleIds.size(); i++) {
                hierarchies.add(new SysRoleHierarchy()
                    .setAncestorRoleId(ancestorRoleIds.get(i))
                    .setDescendantRoleId(roleId)
                    .setDistance(i + 1)
                    .setTenantId(tenantId));
            }
        }
        
        return hierarchies;
    }
    
    /**
     * 检查是否存在循环依赖
     * 
     * @param roleId 当前角色ID
     * @param parentRoleId 父角色ID
     * @param hierarchyMap 角色层次关系映射（key: 后代角色ID, value: 祖先角色ID列表）
     * @return 是否存在循环依赖
     */
    public static boolean hasCircularDependency(Long roleId, Long parentRoleId, 
                                                Map<Long, List<Long>> hierarchyMap) {
        if (roleId == null || parentRoleId == null) {
            return false;
        }
        
        // 如果父角色就是自己，直接返回true
        if (roleId.equals(parentRoleId)) {
            return true;
        }
        
        // 检查父角色是否在当前角色的后代中
        List<Long> descendants = hierarchyMap.get(roleId);
        if (descendants != null && descendants.contains(parentRoleId)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 构建角色树
     * 
     * @param roles 角色列表
     * @param parentId 父角色ID（null表示顶级）
     * @return 角色树
     */
    public static List<SysRole> buildRoleTree(List<SysRole> roles, Long parentId) {
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        
        return roles.stream()
            .filter(role -> {
                if (parentId == null) {
                    return role.getParentRoleId() == null || role.getParentRoleId() == 0L;
                }
                return parentId.equals(role.getParentRoleId());
            })
            .peek(role -> {
                // 递归设置子角色
                List<SysRole> children = buildRoleTree(roles, role.getRoleId());
                // 这里可以通过扩展SysRole添加children字段，或者使用DTO
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 扁平化角色树
     * 
     * @param role 角色节点
     * @param result 结果列表
     */
    public static void flattenRoleTree(SysRole role, List<SysRole> result) {
        if (role == null) {
            return;
        }
        result.add(role);
        // 如果有子角色字段，继续递归
        // 这里需要SysRole扩展支持children字段
    }
    
    /**
     * 验证角色层级深度
     * 
     * @param roleLevel 角色层级
     * @return 是否有效
     */
    public static boolean isValidRoleLevel(int roleLevel) {
        return roleLevel >= 0 && roleLevel < MAX_ROLE_DEPTH;
    }
    
    /**
     * 合并角色ID列表（去重）
     * 
     * @param roleLists 多个角色ID列表
     * @return 合并后的角色ID列表
     */
    @SafeVarargs
    public static List<Long> mergeRoleIds(List<Long>... roleLists) {
        Set<Long> mergedSet = new LinkedHashSet<>();
        for (List<Long> roleList : roleLists) {
            if (CollUtil.isNotEmpty(roleList)) {
                mergedSet.addAll(roleList);
            }
        }
        return new ArrayList<>(mergedSet);
    }
    
    /**
     * 获取角色的根角色ID（路径中的第一个角色）
     * 
     * @param rolePath 角色路径
     * @return 根角色ID
     */
    public static Long getRootRoleId(String rolePath) {
        List<Long> roleIds = extractRoleIdsFromPath(rolePath);
        return CollUtil.isEmpty(roleIds) ? null : roleIds.get(0);
    }
    
    /**
     * 判断角色A是否是角色B的祖先
     * 
     * @param ancestorPath 角色A的路径
     * @param descendantPath 角色B的路径
     * @return 是否是祖先关系
     */
    public static boolean isAncestor(String ancestorPath, String descendantPath) {
        if (StrUtil.isBlank(ancestorPath) || StrUtil.isBlank(descendantPath)) {
            return false;
        }
        return descendantPath.contains(ancestorPath.replace(PATH_SEPARATOR + PATH_SEPARATOR, PATH_SEPARATOR));
    }
}
