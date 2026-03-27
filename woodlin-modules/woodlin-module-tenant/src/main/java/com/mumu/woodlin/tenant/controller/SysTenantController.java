package com.mumu.woodlin.tenant.controller;

import java.util.Arrays;
import java.util.List;

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

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.tenant.entity.SysTenant;
import com.mumu.woodlin.tenant.model.request.TenantStatusRequest;
import com.mumu.woodlin.tenant.service.ISysTenantService;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 租户管理控制器
 *
 * @author mumu
 * @since 2026-03-18
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/tenant")
@Tag(name = "租户管理", description = "系统租户管理相关接口")
public class SysTenantController {

    private final ISysTenantService tenantService;

    /**
     * 分页查询租户列表
     *
     * @param tenant 查询条件
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询租户列表", description = "根据租户名称、编码、状态分页查询租户列表")
    public R<PageResult<SysTenant>> list(
            SysTenant tenant,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页显示数量", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        requirePermission("tenant:list");
        return R.ok(tenantService.selectTenantPage(tenant, pageNum, pageSize));
    }

    /**
     * 查询租户详情
     *
     * @param tenantId 租户ID
     * @return 租户信息
     */
    @GetMapping("/{tenantId}")
    @Operation(summary = "查询租户详情", description = "根据租户ID查询租户详情")
    public R<SysTenant> getInfo(
            @Parameter(description = "租户ID", required = true) @PathVariable String tenantId) {
        requirePermission("tenant:list");
        SysTenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户不存在");
        }
        return R.ok(tenant);
    }

    /**
     * 新增租户
     *
     * @param tenant 租户信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "新增租户", description = "新增租户基础信息")
    public R<Void> add(@Valid @RequestBody SysTenant tenant) {
        requirePermission("tenant:add");
        ensureSuccess(tenantService.createTenant(tenant), "新增租户失败");
        return R.ok("新增租户成功");
    }

    /**
     * 修改租户
     *
     * @param tenant 租户信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "修改租户", description = "修改租户基础信息")
    public R<Void> edit(@Valid @RequestBody SysTenant tenant) {
        requirePermission("tenant:edit");
        ensureSuccess(tenantService.updateTenant(tenant), "修改租户失败");
        return R.ok("修改租户成功");
    }

    /**
     * 删除租户
     *
     * @param tenantIds 租户ID列表
     * @return 操作结果
     */
    @DeleteMapping("/{tenantIds}")
    @Operation(summary = "删除租户", description = "按租户ID批量删除租户")
    public R<Void> remove(
            @Parameter(description = "租户ID列表，逗号分隔", required = true) @PathVariable String tenantIds) {
        requirePermission("tenant:remove");
        List<String> tenantIdList = Arrays.stream(StrUtil.splitToArray(tenantIds, ','))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .toList();
        ensureSuccess(tenantService.deleteTenantByIds(tenantIdList), "删除租户失败");
        return R.ok("删除租户成功");
    }

    /**
     * 修改租户状态
     *
     * @param request 状态请求
     * @return 操作结果
     */
    @PutMapping("/changeStatus")
    @Operation(summary = "修改租户状态", description = "启用或禁用租户")
    public R<Void> changeStatus(@Valid @RequestBody TenantStatusRequest request) {
        requirePermission("tenant:edit");
        ensureSuccess(tenantService.updateTenantStatus(request.getTenantId(), request.getStatus()), "修改租户状态失败");
        return R.ok("修改租户状态成功");
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

    private void ensureSuccess(boolean result, String failureMessage) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, failureMessage);
        }
    }
}
