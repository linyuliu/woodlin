package com.mumu.woodlin.etl.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.mumu.woodlin.common.response.Result;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.service.IEtlJobService;

/**
 * ETL任务管理控制器
 * 
 * @author mumu
 * @description ETL任务管理API接口
 * @since 2025-01-01
 */
@Tag(name = "ETL任务管理", description = "ETL任务管理相关接口")
@RestController
@RequestMapping("/etl/jobs")
@RequiredArgsConstructor
public class EtlJobController {
    
    private final IEtlJobService etlJobService;
    
    @Operation(summary = "创建ETL任务")
    @PostMapping
    public Result<Boolean> create(@Validated @RequestBody EtlJob etlJob) {
        boolean success = etlJobService.createJob(etlJob);
        return success ? Result.success(true) : Result.fail("创建ETL任务失败");
    }
    
    @Operation(summary = "更新ETL任务")
    @PutMapping("/{jobId}")
    public Result<Boolean> update(@PathVariable Long jobId, 
                                   @Validated @RequestBody EtlJob etlJob) {
        etlJob.setJobId(jobId);
        boolean success = etlJobService.updateJob(etlJob);
        return success ? Result.success(true) : Result.fail("更新ETL任务失败");
    }
    
    @Operation(summary = "删除ETL任务")
    @DeleteMapping("/{jobId}")
    public Result<Boolean> delete(@PathVariable Long jobId) {
        boolean success = etlJobService.deleteJob(jobId);
        return success ? Result.success(true) : Result.fail("删除ETL任务失败");
    }
    
    @Operation(summary = "获取ETL任务详情")
    @GetMapping("/{jobId}")
    public Result<EtlJob> getById(@PathVariable Long jobId) {
        EtlJob job = etlJobService.getById(jobId);
        return job != null ? Result.success(job) : Result.fail("ETL任务不存在");
    }
    
    @Operation(summary = "查询所有ETL任务")
    @GetMapping
    public Result<?> list() {
        return Result.success(etlJobService.list());
    }
    
    @Operation(summary = "启用ETL任务")
    @PostMapping("/{jobId}/enable")
    public Result<Boolean> enable(@PathVariable Long jobId) {
        boolean success = etlJobService.enableJob(jobId);
        return success ? Result.success(true) : Result.fail("启用ETL任务失败");
    }
    
    @Operation(summary = "禁用ETL任务")
    @PostMapping("/{jobId}/disable")
    public Result<Boolean> disable(@PathVariable Long jobId) {
        boolean success = etlJobService.disableJob(jobId);
        return success ? Result.success(true) : Result.fail("禁用ETL任务失败");
    }
    
    @Operation(summary = "立即执行ETL任务")
    @PostMapping("/{jobId}/execute")
    public Result<Boolean> execute(@PathVariable Long jobId) {
        boolean success = etlJobService.executeJob(jobId);
        return success ? Result.success(true) : Result.fail("执行ETL任务失败");
    }
}
