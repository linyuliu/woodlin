package com.mumu.woodlin.etl.controller;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.mumu.woodlin.common.result.Result;
import com.mumu.woodlin.etl.service.IEtlExecutionLogService;

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
        return Result.success(executionLogService.list());
    }
    
    @Operation(summary = "获取执行历史详情")
    @GetMapping("/{logId}")
    public Result<?> getById(@PathVariable Long logId) {
        return Result.success(executionLogService.getById(logId));
    }
}
