package com.mumu.woodlin.task.controller;

import java.util.Map;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.task.entity.SysJob;
import com.mumu.woodlin.task.service.ISysJobService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务管理接口
 *
 * @author yulin
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/schedule/job")
@RequiredArgsConstructor
@Validated
@Tag(name = "定时任务管理", description = "定时任务的增删改查与执行控制")
public class ScheduleJobController {

    private final ISysJobService jobService;

    /**
     * 分页查询任务
     */
    @GetMapping
    @Operation(summary = "分页查询定时任务")
    // TODO: requirePermission("schedule:job:list")
    public R<PageResult<SysJob>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) String status) {
        SysJob query = new SysJob().setJobName(jobName).setJobGroup(jobGroup).setStatus(status);
        return R.ok(jobService.queryJobPage(query, page, size));
    }

    /**
     * 新增任务
     */
    @PostMapping
    @Operation(summary = "新增定时任务")
    // TODO: requirePermission("schedule:job:add")
    public R<Void> add(@Valid @RequestBody SysJob job) {
        jobService.createJob(job);
        return R.ok();
    }

    /**
     * 修改任务
     */
    @PutMapping
    @Operation(summary = "修改定时任务")
    // TODO: requirePermission("schedule:job:edit")
    public R<Void> update(@Valid @RequestBody SysJob job) {
        jobService.updateJob(job);
        return R.ok();
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除定时任务")
    // TODO: requirePermission("schedule:job:remove")
    public R<Void> remove(@Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        jobService.deleteJob(id);
        return R.ok();
    }

    /**
     * 修改任务状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "修改任务状态")
    // TODO: requirePermission("schedule:job:edit")
    public R<Void> changeStatus(
            @Parameter(description = "任务ID", required = true) @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String status = body == null ? null : body.get("status");
        jobService.changeJobStatus(id, status);
        return R.ok();
    }

    /**
     * 立即执行一次
     */
    @PostMapping("/{id}/run")
    @Operation(summary = "立即执行一次任务")
    // TODO: requirePermission("schedule:job:edit")
    public R<Void> runOnce(@Parameter(description = "任务ID", required = true) @PathVariable("id") Long id) {
        jobService.runJobOnce(id);
        return R.ok();
    }
}
