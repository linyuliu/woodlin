package com.mumu.woodlin.system.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
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
        
        IPage<SysRole> page = roleService.selectRolePage(role, pageNum, pageSize);
        return R.ok(PageResult.of(page));
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
        // 检查角色名称唯一性
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("角色名称已存在");
        }
        
        // 检查角色编码唯一性
        if (!roleService.checkRoleCodeUnique(role)) {
            return R.fail("角色编码已存在");
        }
        
        boolean result = roleService.insertRole(role);
        return result ? R.ok("新增角色成功") : R.fail("新增角色失败");
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
        // 检查角色名称唯一性
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("角色名称已存在");
        }
        
        // 检查角色编码唯一性
        if (!roleService.checkRoleCodeUnique(role)) {
            return R.fail("角色编码已存在");
        }
        
        boolean result = roleService.updateRole(role);
        return result ? R.ok("修改角色成功") : R.fail("修改角色失败");
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
        boolean result = roleService.deleteRoleByIds(List.of(roleIds));
        return result ? R.ok("删除角色成功") : R.fail("删除角色失败");
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
        boolean hasCircular = roleService.checkCircularDependency(roleId, parentRoleId);
        if (hasCircular) {
            return R.fail("设置该父角色会造成循环依赖");
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
        boolean result = roleService.refreshRoleHierarchy(roleId);
        return result ? R.ok("刷新成功") : R.fail("刷新失败");
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
        List<String> permissions = roleService.selectAllPermissionsByRoleId(roleId);
        return R.ok(permissions);
    }
    
    /**
     * 获取角色树（RBAC1）
     */
    @GetMapping("/tree")
    @Operation(
        summary = "获取角色树",
        description = "获取角色的树形结构，用于前端展示角色层次关系（RBAC1功能）"
    )
    public R<List<com.mumu.woodlin.system.dto.RoleTreeDTO>> getRoleTree(
            @Parameter(description = "租户ID", example = "tenant001") @RequestParam(required = false) String tenantId) {
        List<com.mumu.woodlin.system.dto.RoleTreeDTO> tree = roleService.buildRoleTree(tenantId);
        return R.ok(tree);
    }
}
