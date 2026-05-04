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
import com.mumu.woodlin.tenant.entity.SysTenantPackage;
import com.mumu.woodlin.tenant.service.ISysTenantPackageService;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 租户套餐管理控制器
 *
 * @author yulin
 * @since 2026-06
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/tenant/package")
@Tag(name = "租户套餐管理", description = "租户套餐管理相关接口")
public class SysTenantPackageController {

    private final ISysTenantPackageService tenantPackageService;

    /**
     * 分页查询租户套餐列表
     *
     * @param query    查询条件
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询租户套餐列表", description = "根据套餐名称、状态分页查询租户套餐")
    public R<PageResult<SysTenantPackage>> list(
            SysTenantPackage query,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页显示数量", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        requirePermission("tenant:package:list");
        return R.ok(tenantPackageService.queryPage(query, pageNum, pageSize));
    }

    /**
     * 查询所有启用状态的租户套餐
     *
     * @return 套餐列表
     */
    @GetMapping("/all")
    @Operation(summary = "查询所有启用的租户套餐", description = "用于下拉选择")
    public R<List<SysTenantPackage>> listAll() {
        requirePermission("tenant:package:list");
        return R.ok(tenantPackageService.listEnabled());
    }

    /**
     * 查询租户套餐详情
     *
     * @param packageId 套餐ID
     * @return 套餐信息
     */
    @GetMapping("/{packageId}")
    @Operation(summary = "查询租户套餐详情", description = "根据套餐ID查询详情")
    public R<SysTenantPackage> getInfo(
            @Parameter(description = "套餐ID", required = true) @PathVariable Long packageId) {
        requirePermission("tenant:package:list");
        SysTenantPackage entity = tenantPackageService.getById(packageId);
        if (entity == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户套餐不存在");
        }
        return R.ok(entity);
    }

    /**
     * 新增租户套餐
     *
     * @param tenantPackage 套餐信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "新增租户套餐", description = "新增租户套餐")
    public R<Void> add(@Valid @RequestBody SysTenantPackage tenantPackage) {
        requirePermission("tenant:package:add");
        tenantPackage.setPackageId(null);
        ensureSuccess(tenantPackageService.save(tenantPackage), "新增租户套餐失败");
        return R.ok("新增租户套餐成功");
    }

    /**
     * 修改租户套餐
     *
     * @param packageId     套餐ID
     * @param tenantPackage 套餐信息
     * @return 操作结果
     */
    @PutMapping("/{packageId}")
    @Operation(summary = "修改租户套餐", description = "根据套餐ID修改套餐信息")
    public R<Void> edit(
            @Parameter(description = "套餐ID", required = true) @PathVariable Long packageId,
            @Valid @RequestBody SysTenantPackage tenantPackage) {
        requirePermission("tenant:package:edit");
        tenantPackage.setPackageId(packageId);
        if (tenantPackageService.getById(packageId) == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户套餐不存在");
        }
        ensureSuccess(tenantPackageService.updateById(tenantPackage), "修改租户套餐失败");
        return R.ok("修改租户套餐成功");
    }

    /**
     * 删除租户套餐
     *
     * @param packageIds 套餐ID列表（逗号分隔）
     * @return 操作结果
     */
    @DeleteMapping("/{packageIds}")
    @Operation(summary = "删除租户套餐", description = "按套餐ID批量删除")
    public R<Void> remove(
            @Parameter(description = "套餐ID列表，逗号分隔", required = true) @PathVariable String packageIds) {
        requirePermission("tenant:package:remove");
        List<Long> ids = Arrays.stream(StrUtil.splitToArray(packageIds, ','))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .map(Long::valueOf)
            .toList();
        if (ids.isEmpty()) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "套餐ID不能为空");
        }
        ensureSuccess(tenantPackageService.removeByIds(ids), "删除租户套餐失败");
        return R.ok("删除租户套餐成功");
    }

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
