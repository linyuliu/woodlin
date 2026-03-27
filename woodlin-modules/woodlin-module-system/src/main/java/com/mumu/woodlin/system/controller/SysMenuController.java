package com.mumu.woodlin.system.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.dto.MenuTreeDTO;
import com.mumu.woodlin.system.entity.SysPermission;
import com.mumu.woodlin.system.entity.SysRoleInheritedPermission;
import com.mumu.woodlin.system.entity.SysRolePermission;
import com.mumu.woodlin.system.mapper.SysRoleInheritedPermissionMapper;
import com.mumu.woodlin.system.mapper.SysRolePermissionMapper;
import com.mumu.woodlin.system.service.ISysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器
 *
 * @author mumu
 * @description 系统菜单/按钮权限管理接口
 * @since 2026-03-15
 */
@Slf4j
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@Validated
@Tag(name = "菜单管理", description = "系统菜单管理相关接口")
public class SysMenuController {

    private final ISysPermissionService permissionService;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleInheritedPermissionMapper inheritedPermissionMapper;

    /**
     * 查询菜单列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询菜单列表", description = "按条件查询菜单/按钮权限列表")
    public R<List<SysPermission>> list(SysPermission menu) {
        requirePermission("system:menu:list");
        return R.ok(selectMenuList(menu));
    }

    /**
     * 查询菜单树
     */
    @GetMapping("/tree")
    @Operation(summary = "查询菜单树", description = "查询菜单/按钮权限树")
    public R<List<MenuTreeDTO>> tree(SysPermission menu) {
        requirePermission("system:menu:list");
        List<SysPermission> menus = selectMenuList(menu);
        return R.ok(buildMenuTree(menus));
    }

    /**
     * 根据ID查询菜单详情
     */
    @GetMapping("/{menuId}")
    @Operation(summary = "查询菜单详情", description = "根据菜单ID查询菜单详情")
    public R<SysPermission> getInfo(
            @Parameter(description = "菜单ID", required = true, example = "1") @PathVariable Long menuId) {
        requirePermission("system:menu:list");
        return R.ok(requireMenu(permissionService.getById(menuId)));
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @Operation(summary = "新增菜单", description = "新增目录/菜单/按钮权限")
    public R<Void> add(@Valid @RequestBody SysPermission menu) {
        requirePermission("system:menu:add");
        normalizeMenuParent(menu);

        if (!checkMenuNameUnique(menu)) {
            throw BusinessException.of(ResultCode.CONFLICT, "菜单名称已存在");
        }
        if (StrUtil.isNotBlank(menu.getPermissionCode()) && !checkPermissionCodeUnique(menu)) {
            throw BusinessException.of(ResultCode.CONFLICT, "权限编码已存在");
        }

        ensureSuccess(permissionService.save(menu), "新增失败");
        return R.ok("新增成功");
    }

    /**
     * 修改菜单
     */
    @PutMapping
    @Operation(summary = "修改菜单", description = "修改目录/菜单/按钮权限")
    public R<Void> edit(@Valid @RequestBody SysPermission menu) {
        requirePermission("system:menu:edit");
        if (ObjectUtil.isNull(menu.getPermissionId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "菜单ID不能为空");
        }
        if (ObjectUtil.equals(menu.getPermissionId(), menu.getParentId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "上级菜单不能选择自己");
        }

        normalizeMenuParent(menu);

        if (!checkMenuNameUnique(menu)) {
            throw BusinessException.of(ResultCode.CONFLICT, "菜单名称已存在");
        }
        if (StrUtil.isNotBlank(menu.getPermissionCode()) && !checkPermissionCodeUnique(menu)) {
            throw BusinessException.of(ResultCode.CONFLICT, "权限编码已存在");
        }

        ensureSuccess(permissionService.updateById(menu), "修改失败");
        return R.ok("修改成功");
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    @Operation(summary = "删除菜单", description = "删除目录/菜单/按钮权限")
    public R<Void> remove(
            @Parameter(description = "菜单ID", required = true, example = "1") @PathVariable Long menuId) {
        requirePermission("system:menu:remove");

        if (hasChildByMenuId(menuId)) {
            throw BusinessException.of(ResultCode.CONFLICT, "存在子菜单，不允许删除");
        }

        LambdaQueryWrapper<SysRolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
        rolePermWrapper.eq(SysRolePermission::getPermissionId, menuId);
        if (rolePermissionMapper.selectCount(rolePermWrapper) > 0) {
            throw BusinessException.of(ResultCode.CONFLICT, "菜单已分配给角色，不允许删除");
        }

        ensureSuccess(permissionService.removeById(menuId), "删除失败");
        LambdaQueryWrapper<SysRoleInheritedPermission> inheritedWrapper = new LambdaQueryWrapper<>();
        inheritedWrapper.eq(SysRoleInheritedPermission::getPermissionId, menuId);
        inheritedPermissionMapper.delete(inheritedWrapper);
        return R.ok("删除成功");
    }

    /**
     * 查询菜单列表（内部）
     */
    private List<SysPermission> selectMenuList(SysPermission query) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getPermissionName()), SysPermission::getPermissionName, query.getPermissionName())
            .like(StrUtil.isNotBlank(query.getPermissionCode()), SysPermission::getPermissionCode, query.getPermissionCode())
            .eq(StrUtil.isNotBlank(query.getPermissionType()), SysPermission::getPermissionType, query.getPermissionType())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysPermission::getStatus, query.getStatus())
            .orderByAsc(SysPermission::getSortOrder)
            .orderByAsc(SysPermission::getPermissionId);
        return permissionService.list(wrapper);
    }

    /**
     * 构建菜单树
     */
    private List<MenuTreeDTO> buildMenuTree(List<SysPermission> menus) {
        Map<Long, MenuTreeDTO> nodeMap = new HashMap<>(menus.size());
        List<MenuTreeDTO> roots = new ArrayList<>();

        for (SysPermission menu : menus) {
            MenuTreeDTO node = toMenuTreeNode(menu);
            nodeMap.put(node.getMenuId(), node);
        }

        for (MenuTreeDTO node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (ObjectUtil.isNull(parentId) || parentId == 0L || !nodeMap.containsKey(parentId)) {
                roots.add(node);
                continue;
            }
            nodeMap.get(parentId).getChildren().add(node);
        }

        sortTree(roots);
        return roots;
    }

    /**
     * 递归排序树节点
     */
    private void sortTree(List<MenuTreeDTO> nodes) {
        nodes.sort(Comparator
            .comparing((MenuTreeDTO node) -> ObjectUtil.defaultIfNull(node.getSortOrder(), 0))
            .thenComparing(node -> ObjectUtil.defaultIfNull(node.getMenuId(), 0L)));
        for (MenuTreeDTO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    /**
     * 转换菜单节点
     */
    private MenuTreeDTO toMenuTreeNode(SysPermission menu) {
        return new MenuTreeDTO()
            .setMenuId(menu.getPermissionId())
            .setParentId(menu.getParentId())
            .setMenuName(menu.getPermissionName())
            .setPermissionCode(menu.getPermissionCode())
            .setPermissionType(menu.getPermissionType())
            .setPath(menu.getPath())
            .setComponent(menu.getComponent())
            .setIcon(menu.getIcon())
            .setSortOrder(menu.getSortOrder())
            .setStatus(menu.getStatus())
            .setVisible(menu.getVisible())
            .setIsFrame(menu.getIsFrame())
            .setIsCache(menu.getIsCache())
            .setRemark(menu.getRemark());
    }

    /**
     * 是否存在子节点
     */
    private boolean hasChildByMenuId(Long menuId) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, menuId);
        return permissionService.count(wrapper) > 0;
    }

    /**
     * 校验菜单名称唯一
     */
    private boolean checkMenuNameUnique(SysPermission menu) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, menu.getParentId())
            .eq(SysPermission::getPermissionName, menu.getPermissionName())
            .ne(ObjectUtil.isNotNull(menu.getPermissionId()), SysPermission::getPermissionId, menu.getPermissionId())
            .last("LIMIT 1");
        return ObjectUtil.isNull(permissionService.getOne(wrapper));
    }

    /**
     * 校验权限编码唯一
     */
    private boolean checkPermissionCodeUnique(SysPermission menu) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, menu.getPermissionCode())
            .ne(ObjectUtil.isNotNull(menu.getPermissionId()), SysPermission::getPermissionId, menu.getPermissionId())
            .last("LIMIT 1");
        return ObjectUtil.isNull(permissionService.getOne(wrapper));
    }

    /**
     * 规范化父节点
     */
    private void normalizeMenuParent(SysPermission menu) {
        if (ObjectUtil.isNull(menu.getParentId())) {
            menu.setParentId(0L);
        }
    }

    private SysPermission requireMenu(SysPermission menu) {
        if (menu == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "菜单不存在");
        }
        return menu;
    }

    private void ensureSuccess(boolean result, String failureMessage) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, failureMessage);
        }
    }

    /**
     * 权限校验
     */
    private void requirePermission(String permission) {
        if (!SecurityUtil.hasPermission(permission)) {
            throw BusinessException.of(ResultCode.PERMISSION_DENIED, "权限不足: " + permission);
        }
    }
}
