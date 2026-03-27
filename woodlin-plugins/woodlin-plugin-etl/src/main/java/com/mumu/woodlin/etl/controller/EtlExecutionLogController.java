package com.mumu.woodlin.etl.controller;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.etl.constant.EtlPermissionConstants;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.mumu.woodlin.etl.service.IEtlExecutionLogService;
import com.mumu.woodlin.security.util.SecurityUtil;

/**
 * ETL执行历史控制器
 * 
 * @author mumu
 * @description ETL执行历史查询API接口
 * @since 2025-01-01
 */
@Tag(name = "ETL执行历史", description = "ETL执行历史查询相关接口")
@RestController
@RequestMapping("/etl/logs")
@RequiredArgsConstructor
public class EtlExecutionLogController {
    
    private final IEtlExecutionLogService executionLogService;
    
    @Operation(summary = "查询所有执行历史")
    @GetMapping
    public Result<?> list() {
        requirePermission(EtlPermissionConstants.OFFLINE_LOG_LIST);
        return Result.success(executionLogService.list());
    }

    @Operation(summary = "获取执行历史详情")
    @GetMapping("/{logId}")
    public Result<?> getById(@PathVariable Long logId) {
        requirePermission(EtlPermissionConstants.OFFLINE_LOG_DETAIL);
        return Result.success(executionLogService.getById(logId));
    }

    /**
     * 校验权限。
     *
     * @param permission 权限码
     */
    private void requirePermission(String permission) {
        if (!SecurityUtil.hasPermission(permission)) {
            throw new BusinessException("权限不足: " + permission);
        }
    }
}
