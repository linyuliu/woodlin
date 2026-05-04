package com.mumu.woodlin.task.controller;

import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.task.entity.SysJobLog;
import com.mumu.woodlin.task.service.ISysJobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务日志接口
 *
 * @author yulin
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/schedule/log")
@RequiredArgsConstructor
@Tag(name = "定时任务日志", description = "定时任务执行日志查询与清理")
public class ScheduleJobLogController {

    private final ISysJobLogService jobLogService;

    /**
     * 分页查询日志
     */
    @GetMapping
    @Operation(summary = "分页查询任务日志")
    // TODO: requirePermission("schedule:job:list")
    public R<PageResult<SysJobLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) String status) {
        SysJobLog query = new SysJobLog().setJobName(jobName).setJobGroup(jobGroup).setStatus(status);
        return R.ok(jobLogService.queryLogPage(query, page, size));
    }

    /**
     * 删除单条日志
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务日志")
    // TODO: requirePermission("schedule:job:remove")
    public R<Void> remove(@Parameter(description = "日志ID", required = true) @PathVariable("id") Long id) {
        jobLogService.deleteLog(id);
        return R.ok();
    }

    /**
     * 清空全部日志
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清空任务日志")
    // TODO: requirePermission("schedule:job:remove")
    public R<Void> clean() {
        jobLogService.cleanLogs();
        return R.ok();
    }
}
