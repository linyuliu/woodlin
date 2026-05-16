package com.mumu.woodlin.etl.controller;

import java.time.LocalDateTime;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.etl.constant.EtlPermissionConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Operation(summary = "分页查询执行历史")
    @GetMapping("/page")
    public PageResult<?> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        requirePermission(EtlPermissionConstants.OFFLINE_LOG_LIST);
        LambdaQueryWrapper<com.mumu.woodlin.etl.entity.EtlExecutionLog> wrapper =
                new LambdaQueryWrapper<com.mumu.woodlin.etl.entity.EtlExecutionLog>()
                        .eq(jobId != null, com.mumu.woodlin.etl.entity.EtlExecutionLog::getJobId, jobId)
                        .eq(status != null && !status.isBlank(),
                                com.mumu.woodlin.etl.entity.EtlExecutionLog::getExecutionStatus, status)
                        .ge(startTime != null && !startTime.isBlank(),
                                com.mumu.woodlin.etl.entity.EtlExecutionLog::getStartTime, parseDateTime(startTime))
                        .le(endTime != null && !endTime.isBlank(),
                                com.mumu.woodlin.etl.entity.EtlExecutionLog::getEndTime, parseDateTime(endTime))
                        .orderByDesc(com.mumu.woodlin.etl.entity.EtlExecutionLog::getCreateTime);
        Page<com.mumu.woodlin.etl.entity.EtlExecutionLog> page =
                executionLogService.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(pageNum, pageSize, page.getTotal(), page.getRecords());
    }

    @Operation(summary = "获取执行历史详情")
    @GetMapping("/{logId}")
    public Result<?> getById(@PathVariable Long logId) {
        requirePermission(EtlPermissionConstants.OFFLINE_LOG_DETAIL);
        return Result.success(executionLogService.getById(logId));
    }

    @Operation(summary = "清空执行历史")
    @DeleteMapping("/clean")
    public Result<Boolean> clean() {
        requirePermission(EtlPermissionConstants.OFFLINE_LOG_LIST);
        return Result.success(executionLogService.remove(new LambdaQueryWrapper<>()));
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

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value.replace(" ", "T"));
    }
}
