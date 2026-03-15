package com.mumu.woodlin.system.controller;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysDept;
import com.mumu.woodlin.system.service.ISysDeptService;
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

import java.util.List;

/**
 * 部门管理控制器
 *
 * @author mumu
 * @description 系统部门管理相关接口
 * @since 2026-03-15
 */
@Slf4j
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Validated
@Tag(name = "部门管理", description = "系统部门管理相关接口")
public class SysDeptController {

    private final ISysDeptService deptService;

    /**
     * 查询部门列表（树形）
     */
    @GetMapping("/list")
    @Operation(summary = "查询部门列表", description = "按条件查询部门树列表")
    public R<List<SysDept>> list(SysDept dept) {
        requirePermission("system:dept:list");
        return R.ok(deptService.selectDeptTree(dept));
    }

    /**
     * 查询部门树
     */
    @GetMapping("/tree")
    @Operation(summary = "查询部门树", description = "查询部门树结构")
    public R<List<SysDept>> tree(SysDept dept) {
        requirePermission("system:dept:list");
        return R.ok(deptService.selectDeptTree(dept));
    }

    /**
     * 根据ID查询部门详情
     */
    @GetMapping("/{deptId}")
    @Operation(summary = "查询部门详情", description = "根据部门ID查询部门详情")
    public R<SysDept> getInfo(
            @Parameter(description = "部门ID", required = true, example = "100") @PathVariable Long deptId) {
        requirePermission("system:dept:list");
        return R.ok(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @PostMapping
    @Operation(summary = "新增部门", description = "新增部门")
    public R<Void> add(@Valid @RequestBody SysDept dept) {
        requirePermission("system:dept:add");
        boolean result = deptService.insertDept(dept);
        return result ? R.ok("新增成功") : R.fail("新增失败");
    }

    /**
     * 修改部门
     */
    @PutMapping
    @Operation(summary = "修改部门", description = "修改部门")
    public R<Void> edit(@Valid @RequestBody SysDept dept) {
        requirePermission("system:dept:edit");
        boolean result = deptService.updateDept(dept);
        return result ? R.ok("修改成功") : R.fail("修改失败");
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    @Operation(summary = "删除部门", description = "根据部门ID删除部门")
    public R<Void> remove(
            @Parameter(description = "部门ID", required = true, example = "100") @PathVariable Long deptId) {
        requirePermission("system:dept:remove");
        boolean result = deptService.deleteDeptById(deptId);
        return result ? R.ok("删除成功") : R.fail("删除失败");
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
