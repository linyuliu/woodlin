package com.mumu.woodlin.system.service;

import cn.hutool.core.collection.CollUtil;
import com.mumu.woodlin.system.dto.RoleTreeDTO;
import com.mumu.woodlin.system.entity.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色树比对服务
 *
 * @author mumu
 * @description 提供高效的RBAC1角色树比对算法，用于权限授权时的角色树比对
 * @since 2025-01-04
 */
@Slf4j
@Service
public class RoleTreeComparisonService {

    /**
     * 角色树比对结果
     */
    public static class ComparisonResult {
        /**
         * 新增的角色ID列表
         */
        private final List<Long> addedRoleIds = new ArrayList<>();

        /**
         * 删除的角色ID列表
         */
        private final List<Long> removedRoleIds = new ArrayList<>();

        /**
         * 保持不变的角色ID列表
         */
        private final List<Long> unchangedRoleIds = new ArrayList<>();

        /**
         * 是否有变化
         */
        private boolean hasChanges = false;

        public List<Long> getAddedRoleIds() {
            return addedRoleIds;
        }

        public List<Long> getRemovedRoleIds() {
            return removedRoleIds;
        }

        public List<Long> getUnchangedRoleIds() {
            return unchangedRoleIds;
        }

        public boolean hasChanges() {
            return hasChanges;
        }

        public void setHasChanges(boolean hasChanges) {
            this.hasChanges = hasChanges;
        }
    }

    /**
     * 基于路径的角色树比对（最高效，适用于有role_path的场景）
     *
     * @param userRoles 用户当前拥有的角色列表
     * @param assignedRoleIds 授权的角色ID列表
     * @param allRoles 系统所有角色列表（用于查找角色路径）
     * @return 比对结果
     */
    public ComparisonResult compareByPath(List<SysRole> userRoles, List<Long> assignedRoleIds,
                                         List<SysRole> allRoles) {
        ComparisonResult result = new ComparisonResult();

        if (CollUtil.isEmpty(assignedRoleIds)) {
            // 没有新分配的角色，所有现有角色都应删除
            if (CollUtil.isNotEmpty(userRoles)) {
                result.getRemovedRoleIds().addAll(
                    userRoles.stream().map(SysRole::getRoleId).toList()
                );
                result.setHasChanges(true);
            }
            return result;
        }

        // 构建角色ID到角色对象的映射
        Map<Long, SysRole> roleMap = allRoles.stream()
            .collect(Collectors.toMap(SysRole::getRoleId, role -> role));

        // 获取当前用户角色ID集合
        Set<Long> currentRoleIds = userRoles.stream()
            .map(SysRole::getRoleId)
            .collect(Collectors.toSet());

        // 使用路径快速查找所有需要的角色（包括祖先角色）
        Set<Long> targetRoleIds = expandRolesByPath(assignedRoleIds, roleMap);

        // 计算新增、删除和不变的角色 - 使用Java Set操作优化
        Set<Long> assignedSet = new HashSet<>(targetRoleIds);
        Set<Long> currentSet = new HashSet<>(currentRoleIds);

        // 新增的角色 = 目标角色 - 当前角色（使用Set差集操作）
        Set<Long> addedSet = new HashSet<>(assignedSet);
        addedSet.removeAll(currentSet);
        result.getAddedRoleIds().addAll(addedSet);

        // 删除的角色 = 当前角色 - 目标角色（使用Set差集操作）
        Set<Long> removedSet = new HashSet<>(currentSet);
        removedSet.removeAll(assignedSet);
        result.getRemovedRoleIds().addAll(removedSet);

        // 保持不变的角色 = 当前角色 ∩ 目标角色（使用Set交集操作）
        Set<Long> unchangedSet = new HashSet<>(currentSet);
        unchangedSet.retainAll(assignedSet);
        result.getUnchangedRoleIds().addAll(unchangedSet);

        result.setHasChanges(!result.getAddedRoleIds().isEmpty()
                          || !result.getRemovedRoleIds().isEmpty());

        log.debug("角色树比对完成: 新增={}, 删除={}, 不变={}",
            result.getAddedRoleIds().size(),
            result.getRemovedRoleIds().size(),
            result.getUnchangedRoleIds().size());

        return result;
    }

    /**
     * 基于集合的角色树比对（适用于没有role_path的场景，需要递归查找）
     *
     * @param userRoles 用户当前拥有的角色列表
     * @param assignedRoleIds 授权的角色ID列表
     * @param allRoles 系统所有角色列表
     * @return 比对结果
     */
    public ComparisonResult compareBySet(List<SysRole> userRoles, List<Long> assignedRoleIds,
                                        List<SysRole> allRoles) {
        ComparisonResult result = new ComparisonResult();

        if (CollUtil.isEmpty(assignedRoleIds)) {
            // 没有新分配的角色，所有现有角色都应删除
            if (CollUtil.isNotEmpty(userRoles)) {
                result.getRemovedRoleIds().addAll(
                    userRoles.stream().map(SysRole::getRoleId).toList()
                );
                result.setHasChanges(true);
            }
            return result;
        }

        // 构建角色父子关系映射
        Map<Long, SysRole> roleMap = allRoles.stream()
            .collect(Collectors.toMap(SysRole::getRoleId, role -> role));

        // 获取当前用户角色ID集合
        Set<Long> currentRoleIds = userRoles.stream()
            .map(SysRole::getRoleId)
            .collect(Collectors.toSet());

        // 递归查找所有需要的角色（包括祖先角色）
        Set<Long> targetRoleIds = expandRolesByRecursion(assignedRoleIds, roleMap);

        // 计算新增、删除和不变的角色 - 使用Java Set操作优化
        Set<Long> assignedSet = new HashSet<>(targetRoleIds);
        Set<Long> currentSet = new HashSet<>(currentRoleIds);

        // 新增的角色 = 目标角色 - 当前角色（使用Set差集操作）
        Set<Long> addedSet = new HashSet<>(assignedSet);
        addedSet.removeAll(currentSet);
        result.getAddedRoleIds().addAll(addedSet);

        // 删除的角色 = 当前角色 - 目标角色（使用Set差集操作）
        Set<Long> removedSet = new HashSet<>(currentSet);
        removedSet.removeAll(assignedSet);
        result.getRemovedRoleIds().addAll(removedSet);

        // 保持不变的角色 = 当前角色 ∩ 目标角色（使用Set交集操作）
        Set<Long> unchangedSet = new HashSet<>(currentSet);
        unchangedSet.retainAll(assignedSet);
        result.getUnchangedRoleIds().addAll(unchangedSet);

        result.setHasChanges(!result.getAddedRoleIds().isEmpty()
                          || !result.getRemovedRoleIds().isEmpty());

        log.debug("角色树比对完成: 新增={}, 删除={}, 不变={}",
            result.getAddedRoleIds().size(),
            result.getRemovedRoleIds().size(),
            result.getUnchangedRoleIds().size());

        return result;
    }

    /**
     * 通过路径展开角色（包括所有祖先角色）
     * 时间复杂度: O(n)，n为角色总数
     *
     * @param roleIds 角色ID列表
     * @param roleMap 角色映射
     * @return 展开后的角色ID集合（包括祖先角色）
     */
    private Set<Long> expandRolesByPath(List<Long> roleIds, Map<Long, SysRole> roleMap) {
        Set<Long> expanded = new HashSet<>();

        for (Long roleId : roleIds) {
            SysRole role = roleMap.get(roleId);
            if (role == null) {
                continue;
            }

            // 添加自己
            expanded.add(roleId);

            // 通过role_path快速获取所有祖先角色
            String rolePath = role.getRolePath();
            if (rolePath != null && !rolePath.isEmpty()) {
                // role_path格式: /1/2/3/
                String[] pathParts = rolePath.split("/");
                for (String part : pathParts) {
                    if (!part.isEmpty()) {
                        try {
                            Long ancestorId = Long.parseLong(part);
                            expanded.add(ancestorId);
                        } catch (NumberFormatException e) {
                            log.warn("无效的角色路径部分: {}", part);
                        }
                    }
                }
            }
        }

        return expanded;
    }

    /**
     * 通过递归展开角色（包括所有祖先角色）
     * 时间复杂度: O(n*h)，n为角色总数，h为树的高度
     *
     * @param roleIds 角色ID列表
     * @param roleMap 角色映射
     * @return 展开后的角色ID集合（包括祖先角色）
     */
    private Set<Long> expandRolesByRecursion(List<Long> roleIds, Map<Long, SysRole> roleMap) {
        Set<Long> expanded = new HashSet<>();

        for (Long roleId : roleIds) {
            expandRoleRecursive(roleId, roleMap, expanded);
        }

        return expanded;
    }

    /**
     * 递归展开单个角色
     *
     * @param roleId 角色ID
     * @param roleMap 角色映射
     * @param result 结果集合
     */
    private void expandRoleRecursive(Long roleId, Map<Long, SysRole> roleMap, Set<Long> result) {
        if (roleId == null || result.contains(roleId)) {
            return;
        }

        // 添加当前角色
        result.add(roleId);

        // 查找父角色并递归
        SysRole role = roleMap.get(roleId);
        if (role != null && role.getParentRoleId() != null && role.getParentRoleId() > 0) {
            expandRoleRecursive(role.getParentRoleId(), roleMap, result);
        }
    }

    /**
     * 构建前端友好的角色树（带选中状态）
     *
     * @param allRoles 所有角色列表
     * @param userRoleIds 用户拥有的角色ID列表
     * @return 角色树DTO列表
     */
    public List<RoleTreeDTO> buildRoleTreeWithSelection(List<SysRole> allRoles, List<Long> userRoleIds) {
        if (CollUtil.isEmpty(allRoles)) {
            return Collections.emptyList();
        }

        Set<Long> userRoleSet = userRoleIds != null ? new HashSet<>(userRoleIds) : new HashSet<>();

        // 转换为DTO
        List<RoleTreeDTO> dtoList = allRoles.stream()
            .map(role -> convertToDTO(role, userRoleSet.contains(role.getRoleId())))
            .toList();

        // 构建树形结构
        return buildTree(dtoList, 0L);
    }

    /**
     * 转换角色为DTO
     *
     * @param role 角色实体
     * @param isSelected 是否选中
     * @return 角色树DTO
     */
    private RoleTreeDTO convertToDTO(SysRole role, boolean isSelected) {
        RoleTreeDTO dto = new RoleTreeDTO();
        dto.setRoleId(role.getRoleId());
        dto.setParentRoleId(role.getParentRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleLevel(role.getRoleLevel());
        dto.setRolePath(role.getRolePath());
        dto.setIsInheritable(role.getIsInheritable());
        dto.setStatus(role.getStatus());
        dto.setSortOrder(role.getSortOrder());
        return dto;
    }

    /**
     * 构建树形结构
     *
     * @param roles 角色DTO列表
     * @param parentId 父ID
     * @return 树形角色DTO列表
     */
    private List<RoleTreeDTO> buildTree(List<RoleTreeDTO> roles, Long parentId) {
        List<RoleTreeDTO> tree = new ArrayList<>();

        Map<Long, List<RoleTreeDTO>> groupedByParent = roles.stream()
            .collect(Collectors.groupingBy(
                r -> r.getParentRoleId() != null ? r.getParentRoleId() : 0L
            ));

        List<RoleTreeDTO> rootNodes = groupedByParent.getOrDefault(parentId, Collections.emptyList());
        for (RoleTreeDTO node : rootNodes) {
            List<RoleTreeDTO> children = groupedByParent.get(node.getRoleId());
            if (CollUtil.isNotEmpty(children)) {
                node.setChildren(children);
                node.setHasChildren(true);
                buildChildrenRecursive(node, groupedByParent);
            } else {
                node.setHasChildren(false);
            }
            tree.add(node);
        }

        return tree;
    }

    /**
     * 递归构建子节点
     *
     * @param parent 父节点
     * @param groupedByParent 按父ID分组的角色Map
     */
    private void buildChildrenRecursive(RoleTreeDTO parent, Map<Long, List<RoleTreeDTO>> groupedByParent) {
        if (parent.getChildren() == null) {
            return;
        }

        for (RoleTreeDTO child : parent.getChildren()) {
            List<RoleTreeDTO> grandChildren = groupedByParent.get(child.getRoleId());
            if (CollUtil.isNotEmpty(grandChildren)) {
                child.setChildren(grandChildren);
                child.setHasChildren(true);
                buildChildrenRecursive(child, groupedByParent);
            } else {
                child.setHasChildren(false);
            }
        }
    }
}
