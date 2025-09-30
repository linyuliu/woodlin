package com.mumu.woodlin.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysUserBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        
        IPage<SysUser> page = userBusinessService.queryUserPage(user, pageNum, pageSize);
        return R.ok(PageResult.of(page));
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
        boolean result = userBusinessService.createUser(user);
        return result ? R.ok("新增用户成功") : R.fail("新增用户失败");
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
        boolean result = userBusinessService.updateUser(user);
        return result ? R.ok("修改用户成功") : R.fail("修改用户失败");
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
        List<String> ids = Arrays.asList(userIds.split(","));
        boolean result = userBusinessService.deleteUsers(ids);
        return result ? R.ok("删除用户成功") : R.fail("删除用户失败");
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
        boolean result = userBusinessService.resetUserPassword(userId, password);
        return result ? R.ok("重置密码成功") : R.fail("重置密码失败");
    }
    
    /**
     * 状态修改
     */
    @PutMapping("/changeStatus")
    @Operation(summary = "状态修改")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        boolean result = userBusinessService.changeUserStatus(user);
        return result ? R.ok("修改状态成功") : R.fail("修改状态失败");
    }
    
    /**
     * 导出用户数据
     */
    @PostMapping("/export")
    @Operation(summary = "导出用户数据")
    public void export(HttpServletResponse response, @RequestBody SysUser user) throws IOException {
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
        
        String message = userBusinessService.importUsers(file, updateSupport);
        return R.ok(message);
    }
    
    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    @Operation(summary = "下载导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        userBusinessService.downloadTemplate(response);
    }
}