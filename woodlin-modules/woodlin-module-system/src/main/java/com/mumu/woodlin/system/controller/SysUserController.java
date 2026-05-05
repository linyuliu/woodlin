package com.mumu.woodlin.system.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.multipart.MultipartFile;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.dto.RouteVO;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysUserBusinessService;

/**
 * 用户管理控制器
 * 
 * @author mumu
 * @description 用户管理控制器，提供用户的增删改查和Excel导入导出功能
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "系统用户管理相关接口，包括用户的增删改查、Excel导入导出等功能")
public class SysUserController {
    
    private final ISysUserBusinessService userBusinessService;
    private final ISysPermissionService permissionService;

    /**
     * 获取当前用户的动态路由菜单树
     *
     * <p>登录后前端调用此接口获取菜单树，结构包含目录(type=1)、菜单(type=2)、按钮(type=3)
     * 三类节点，按角色过滤后以树形返回。前端通过 import.meta.glob 将 component 字段
     * 映射为实际 Vue 组件。</p>
     *
     * @return 当前用户的路由树
     * @see ISysPermissionService#selectRoutesByUserId(Long)
     */
    @GetMapping("/route")
    @Operation(summary = "获取当前用户动态路由", description = "返回当前登录用户的菜单路由树（含按钮权限），用于前端动态路由注册")
    public R<List<RouteVO>> getRoutes() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "未登录");
        }
        return R.ok(permissionService.selectRoutesByUserId(userId));
    }
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @Operation(
        summary = "分页查询用户列表",
        description = "根据查询条件分页获取用户列表，支持按用户名、部门、状态等条件筛选"
    )
    public R<PageResult<SysUser>> list(
            SysUser user,
            @Parameter(description = "页码，从1开始", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页显示数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        requirePermission("system:user:list");
        return R.ok(userBusinessService.queryUserPage(user, pageNum, pageSize));
    }
    
    /**
     * 根据用户ID获取详细信息
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "根据用户ID获取详细信息",
        description = "通过用户ID获取用户的详细信息，包括基本信息、角色、部门等"
    )
    public R<SysUser> getInfo(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable Long userId) {
        requirePermission("system:user:list");
        SysUser user = userBusinessService.getUserById(userId);
        return R.ok(user);
    }
    
    /**
     * 新增用户
     */
    @PostMapping
    @Operation(
        summary = "新增用户",
        description = "创建新用户，用户名必须唯一，可同时设置用户角色和部门信息"
    )
    public R<Void> add(@Valid @RequestBody SysUser user) {
        requirePermission("system:user:add");
        ensureSuccess(userBusinessService.createUser(user), "新增用户失败");
        return R.ok("新增用户成功");
    }
    
    /**
     * 修改用户
     */
    @PutMapping
    @Operation(
        summary = "修改用户",
        description = "更新用户信息，不能修改用户名，可更新其他基本信息、角色和部门"
    )
    public R<Void> edit(@Valid @RequestBody SysUser user) {
        requirePermission("system:user:edit");
        ensureSuccess(userBusinessService.updateUser(user), "修改用户失败");
        return R.ok("修改用户成功");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userIds}")
    @Operation(
        summary = "删除用户",
        description = "根据用户ID删除用户，支持批量删除，多个ID用逗号分隔。注意：不能删除当前登录用户和超级管理员"
    )
    public R<Void> remove(
            @Parameter(description = "用户ID，多个用逗号分隔", required = true, example = "1,2,3") @PathVariable String userIds) {
        requirePermission("system:user:remove");
        List<String> ids = Arrays.asList(userIds.split(","));
        ensureSuccess(userBusinessService.deleteUsers(ids), "删除用户失败");
        return R.ok("删除用户成功");
    }
    
    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    @Operation(
        summary = "重置密码",
        description = "重置指定用户的密码为系统默认密码"
    )
    public R<Void> resetPwd(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "新密码") @RequestParam String password) {
        requirePermission("system:user:resetPwd");
        ensureSuccess(userBusinessService.resetUserPassword(userId, password), "重置密码失败");
        return R.ok("重置密码成功");
    }
    
    /**
     * 状态修改
     */
    @PutMapping("/changeStatus")
    @Operation(summary = "状态修改")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        requirePermission("system:user:edit");
        ensureSuccess(userBusinessService.changeUserStatus(user), "修改状态失败");
        return R.ok("修改状态成功");
    }
    
    /**
     * 导出用户数据
     */
    @PostMapping("/export")
    @Operation(summary = "导出用户数据")
    public void export(HttpServletResponse response, @RequestBody SysUser user) throws IOException {
        requirePermission("system:user:export");
        userBusinessService.exportUsers(response, user);
    }
    
    /**
     * 导入用户数据
     */
    @PostMapping("/importData")
    @Operation(summary = "导入用户数据")
    public R<String> importData(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否更新支持") @RequestParam(defaultValue = "false") Boolean updateSupport) throws IOException {
        requirePermission("system:user:import");
        String message = userBusinessService.importUsers(file, updateSupport);
        return R.ok(message);
    }
    
    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    @Operation(summary = "下载导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        requirePermission("system:user:import");
        userBusinessService.downloadTemplate(response);
    }

    /**
     * 分配角色给用户
     */
    @PutMapping("/{userId}/roles")
    @Operation(
        summary = "分配角色给用户",
        description = "为指定用户分配角色列表，会覆盖原有角色"
    )
    public R<Void> assignRoles(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable Long userId,
            @RequestBody AssignRolesRequest request) {
        requirePermission("system:user:edit");
        SysUser user = userBusinessService.getUserById(userId);
        if (user == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setRoleIds(request.getRoleIds());
        ensureSuccess(userBusinessService.updateUser(user), "分配角色失败");
        return R.ok("分配角色成功");
    }

    /**
     * 获取当前登录用户个人资料
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人资料", description = "获取当前登录用户的个人资料")
    public R<SysUser> profile() {
        return R.ok(userBusinessService.getCurrentUserProfile());
    }

    /**
     * 更新当前登录用户个人资料
     */
    @PostMapping("/profile")
    @Operation(summary = "更新个人资料", description = "更新当前登录用户的昵称、邮箱、手机号、头像等资料")
    public R<Void> updateProfile(@RequestBody SysUser user) {
        ensureSuccess(userBusinessService.updateCurrentUserProfile(user), "更新个人资料失败");
        return R.ok("更新个人资料成功");
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

    /**
     * 分配角色请求DTO
     */
    public static class AssignRolesRequest {
        private List<Long> roleIds;

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }
}
