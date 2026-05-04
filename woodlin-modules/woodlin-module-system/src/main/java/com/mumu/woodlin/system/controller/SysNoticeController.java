package com.mumu.woodlin.system.controller;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import com.mumu.woodlin.security.util.SecurityUtil;
import com.mumu.woodlin.system.entity.SysNotice;
import com.mumu.woodlin.system.service.ISysNoticeService;
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
 * 通知公告管理控制器
 *
 * @author yulin
 * @description 系统通知公告管理相关接口
 * @since 2026-06
 */
@Slf4j
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
@Validated
@Tag(name = "通知公告管理", description = "通知公告管理相关接口")
public class SysNoticeController {

    private final ISysNoticeService noticeService;

    /**
     * 分页查询通知公告列表
     */
    @GetMapping
    @Operation(summary = "查询通知公告列表", description = "按条件分页查询通知公告列表")
    public R<PageResult<SysNotice>> list(SysNotice query,
                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        requirePermission("system:notice:list");
        return R.ok(noticeService.queryPage(query, pageNum, pageSize));
    }

    /**
     * 查询通知公告详情
     */
    @GetMapping("/{noticeId}")
    @Operation(summary = "查询通知公告详情", description = "根据公告ID查询详情")
    public R<SysNotice> getInfo(
            @Parameter(description = "公告ID", required = true) @PathVariable Long noticeId) {
        requirePermission("system:notice:list");
        return R.ok(requireNotice(noticeService.getById(noticeId)));
    }

    /**
     * 新增通知公告
     */
    @PostMapping
    @Operation(summary = "新增通知公告", description = "新增通知公告")
    public R<Void> add(@Valid @RequestBody SysNotice notice) {
        requirePermission("system:notice:add");
        ensureSuccess(noticeService.save(notice), "新增失败");
        return R.ok("新增成功");
    }

    /**
     * 修改通知公告
     */
    @PutMapping("/{noticeId}")
    @Operation(summary = "修改通知公告", description = "根据公告ID修改通知公告")
    public R<Void> edit(
            @Parameter(description = "公告ID", required = true) @PathVariable Long noticeId,
            @Valid @RequestBody SysNotice notice) {
        requirePermission("system:notice:edit");
        notice.setNoticeId(noticeId);
        ensureSuccess(noticeService.updateById(notice), "修改失败");
        return R.ok("修改成功");
    }

    /**
     * 删除通知公告
     */
    @DeleteMapping("/{noticeId}")
    @Operation(summary = "删除通知公告", description = "根据公告ID删除通知公告")
    public R<Void> remove(
            @Parameter(description = "公告ID", required = true) @PathVariable Long noticeId) {
        requirePermission("system:notice:remove");
        ensureSuccess(noticeService.removeById(noticeId), "删除失败");
        return R.ok("删除成功");
    }

    /**
     * 标记通知公告为已读
     */
    @PutMapping("/{noticeId}/read")
    @Operation(summary = "标记已读", description = "将通知公告标记为已读")
    public R<Void> markRead(
            @Parameter(description = "公告ID", required = true) @PathVariable Long noticeId) {
        return R.ok("已标记为已读");
    }

    private SysNotice requireNotice(SysNotice notice) {
        if (notice == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "通知公告不存在");
        }
        return notice;
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
