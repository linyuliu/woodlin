package com.mumu.woodlin.system.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.security.util.SecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.dto.RoleTreeDTO;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.service.ISysRoleService;

/**
 * 角色管理控制器（支持RBAC1角色继承）
 * 
 * @author mumu
 * @description 角色管理控制器，提供角色的CRUD和层次结构管理功能
 * @since 2025-10-31
 */
@Slf4j
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Validated
@Tag(name = "角色管理", description = "系统角色管理相关接口，支持RBAC1角色继承和权限管理")
public class SysRoleController {
    
    private final ISysRoleService roleService;
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    @Operation(
        summary = "分页查询角色列表",
        description = "根据查询条件分页获取角色列表，支持按角色名称、角色编码、状态等条件筛选"
    )
    public R<PageResult<SysRole>> list(
            SysRole role,
            @Parameter(description = "页码，从1开始", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页显示数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        requirePermission("system:role:list");
        return R.ok(roleService.selectRolePage(role, pageNum, pageSize));
    }
    
    /**
     * 根据角色ID获取详细信息
     */
    @GetMapping("/{roleId}")
    @Operation(
        summary = "根据角色ID获取详细信息",
        description = "通过角色ID获取角色的详细信息，包括基本信息和层次结构信息"
    )
    public R<SysRole> getInfo(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:list");
        SysRole role = roleService.getById(roleId);
        return R.ok(role);
    }
    
    /**
     * 新增角色
     */
    @PostMapping
    @Operation(
        summary = "新增角色",
        description = "创建新角色，角色编码必须唯一，可设置父角色实现角色继承（RBAC1）"
    )
    public R<Void> add(@Valid @RequestBody SysRole role) {
        requirePermission("system:role:add");
        if (!roleService.checkRoleNameUnique(role)) {
            throw BusinessException.of(ResultCode.CONFLICT, "角色名称已存在");
        }
        if (!roleService.checkRoleCodeUnique(role)) {
            throw BusinessException.of(ResultCode.CONFLICT, "角色编码已存在");
        }
        ensureSuccess(roleService.insertRole(role), "新增角色失败");
        return R.ok("新增角色成功");
    }
    
    /**
     * 修改角色
     */
    @PutMapping
    @Operation(
        summary = "修改角色",
        description = "更新角色信息，可修改父角色以调整角色层次结构"
    )
    public R<Void> edit(@Valid @RequestBody SysRole role) {
        requirePermission("system:role:edit");
        if (!roleService.checkRoleNameUnique(role)) {
            throw BusinessException.of(ResultCode.CONFLICT, "角色名称已存在");
        }
        if (!roleService.checkRoleCodeUnique(role)) {
            throw BusinessException.of(ResultCode.CONFLICT, "角色编码已存在");
        }
        ensureSuccess(roleService.updateRole(role), "修改角色失败");
        return R.ok("修改角色成功");
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{roleIds}")
    @Operation(
        summary = "批量删除角色",
        description = "根据角色ID删除角色，不能删除有子角色的角色"
    )
    public R<Void> remove(
            @Parameter(description = "角色ID列表，逗号分隔", required = true, example = "1,2,3") 
            @PathVariable Long[] roleIds) {
        requirePermission("system:role:remove");
        ensureSuccess(roleService.deleteRoleByIds(List.of(roleIds)), "删除角色失败");
        return R.ok("删除角色成功");
    }
    
    /**
     * 查询角色的所有祖先角色（RBAC1）
     */
    @GetMapping("/{roleId}/ancestors")
    @Operation(
        summary = "查询角色的祖先角色",
        description = "查询指定角色的所有祖先角色，按层级距离排序（RBAC1功能）"
    )
    public R<List<SysRole>> getAncestors(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:list");
        List<SysRole> ancestors = roleService.selectAncestorRoles(roleId);
        return R.ok(ancestors);
    }
    
    /**
     * 查询角色的所有后代角色（RBAC1）
     */
    @GetMapping("/{roleId}/descendants")
    @Operation(
        summary = "查询角色的后代角色",
        description = "查询指定角色的所有后代角色（子角色、孙角色等），按层级距离排序（RBAC1功能）"
    )
    public R<List<SysRole>> getDescendants(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:list");
        List<SysRole> descendants = roleService.selectDescendantRoles(roleId);
        return R.ok(descendants);
    }
    
    /**
     * 查询角色的直接子角色（RBAC1）
     */
    @GetMapping("/{roleId}/children")
    @Operation(
        summary = "查询角色的直接子角色",
        description = "查询指定角色的直接子角色（不包括孙角色），用于角色树展示（RBAC1功能）"
    )
    public R<List<SysRole>> getChildren(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:list");
        List<SysRole> children = roleService.selectDirectChildRoles(roleId);
        return R.ok(children);
    }
    
    /**
     * 查询顶级角色列表（RBAC1）
     */
    @GetMapping("/top-level")
    @Operation(
        summary = "查询顶级角色列表",
        description = "查询所有没有父角色的顶级角色，用于角色树的根节点（RBAC1功能）"
    )
    public R<List<SysRole>> getTopLevelRoles(
            @Parameter(description = "租户ID", example = "tenant001") @RequestParam(required = false) String tenantId) {
        requirePermission("system:role:list");
        List<SysRole> topRoles = roleService.selectTopLevelRoles(tenantId);
        return R.ok(topRoles);
    }
    
    /**
     * 检查角色继承是否会造成循环依赖（RBAC1）
     */
    @GetMapping("/check-circular")
    @Operation(
        summary = "检查循环依赖",
        description = "检查设置父角色是否会造成循环依赖，用于表单验证（RBAC1功能）"
    )
    public R<Boolean> checkCircularDependency(
            @Parameter(description = "角色ID", required = true, example = "1") @RequestParam Long roleId,
            @Parameter(description = "父角色ID", required = true, example = "2") @RequestParam Long parentRoleId) {
        requirePermission("system:role:list");
        boolean hasCircular = roleService.checkCircularDependency(roleId, parentRoleId);
        if (hasCircular) {
            throw BusinessException.of(ResultCode.CONFLICT, "设置该父角色会造成循环依赖");
        }
        return R.ok(false);
    }
    
    /**
     * 刷新角色的继承层次关系（RBAC1）
     */
    @PostMapping("/{roleId}/refresh-hierarchy")
    @Operation(
        summary = "刷新角色层次关系",
        description = "手动刷新角色的继承层次关系和权限缓存，用于修复数据一致性（RBAC1功能）"
    )
    public R<Void> refreshHierarchy(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:edit");
        ensureSuccess(roleService.refreshRoleHierarchy(roleId), "刷新失败");
        return R.ok("刷新成功");
    }
    
    /**
     * 查询角色的所有权限（包括继承的，RBAC1）
     */
    @GetMapping("/{roleId}/all-permissions")
    @Operation(
        summary = "查询角色的所有权限",
        description = "查询角色的所有权限，包括直接拥有的和从父角色继承的权限（RBAC1功能）"
    )
    public R<List<String>> getAllPermissions(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:list");
        List<String> permissions = roleService.selectAllPermissionsByRoleId(roleId);
        return R.ok(permissions);
    }

    /**
     * 查询角色已分配权限ID
     */
    @GetMapping("/menu/{roleId}")
    @Operation(summary = "查询角色权限ID", description = "查询角色已分配的菜单/按钮/API权限ID列表")
    public R<List<Long>> getRoleMenus(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId) {
        requirePermission("system:role:edit");
        return R.ok(roleService.selectPermissionIdsByRoleId(roleId));
    }

    /**
     * 分配角色权限
     */
    @PutMapping("/menu/{roleId}")
    @Operation(summary = "分配角色权限", description = "按角色ID覆盖更新角色的菜单/按钮/API权限")
    public R<Void> assignRoleMenus(
            @Parameter(description = "角色ID", required = true, example = "1") @PathVariable Long roleId,
            @RequestBody(required = false) List<Long> menuIds) {
        requirePermission("system:role:edit");
        ensureSuccess(roleService.assignRolePermissions(roleId, menuIds), "分配失败");
        return R.ok("分配成功");
    }
    
    /**
     * 获取角色树（RBAC1）
     */
    @GetMapping("/tree")
    @Operation(
        summary = "获取角色树",
        description = "获取角色的树形结构，用于前端展示角色层次关系（RBAC1功能）"
    )
    public R<List<RoleTreeDTO>> getRoleTree(
            @Parameter(description = "租户ID", example = "tenant001") @RequestParam(required = false) String tenantId) {
        requirePermission("system:role:list");
        List<RoleTreeDTO> tree = roleService.buildRoleTree(tenantId);
        return R.ok(tree);
    }

    /**
     * 权限校验
     *
     * @param permission 权限编码
     */
    private void requirePermission(String permission) {
        if (!SecurityUtil.hasPermission(permission)) {
            throw BusinessException.of(ResultCode.PERMISSION_DENIED, "权限不足: " + permission);
        }
    }

    /**
     * 成功状态校验
     *
     * @param result 操作结果
     * @param failureMessage 失败消息
     */
    private void ensureSuccess(boolean result, String failureMessage) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, failureMessage);
        }
    }
}
