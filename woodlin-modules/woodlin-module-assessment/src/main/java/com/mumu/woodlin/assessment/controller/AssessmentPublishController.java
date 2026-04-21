package com.mumu.woodlin.assessment.controller;

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

import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.query.AssessmentPublishQuery;
import com.mumu.woodlin.assessment.service.IAssessmentPublishService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;

/**
 * 发布实例管理接口
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/publish")
@RequiredArgsConstructor
@Validated
@Tag(name = "发布管理", description = "测评发布实例管理接口")
public class AssessmentPublishController {

    private final IAssessmentPublishService publishService;

    /**
     * 分页查询发布实例列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询发布实例列表")
    public R<PageResult<AssessmentPublish>> list(AssessmentPublishQuery query) {
        return R.ok(publishService.pageList(query));
    }

    /**
     * 根据ID获取发布实例详情
     */
    @GetMapping("/{publishId}")
    @Operation(summary = "获取发布实例详情")
    public R<AssessmentPublish> getById(
            @Parameter(description = "发布ID", required = true) @PathVariable Long publishId) {
        return R.ok(publishService.getById(publishId));
    }

    /**
     * 新增发布实例
     */
    @PostMapping
    @Operation(summary = "新增发布实例")
    public R<Void> add(@Valid @RequestBody AssessmentPublish publish) {
        publishService.save(publish);
        return R.ok();
    }

    /**
     * 修改发布实例
     */
    @PutMapping
    @Operation(summary = "修改发布实例")
    public R<Void> update(@Valid @RequestBody AssessmentPublish publish) {
        publishService.updateById(publish);
        return R.ok();
    }

    /**
     * 删除发布实例
     */
    @DeleteMapping("/{publishId}")
    @Operation(summary = "删除发布实例")
    public R<Void> delete(@PathVariable Long publishId) {
        publishService.removeById(publishId);
        return R.ok();
    }

    /**
     * 更新发布状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新发布状态")
    public R<Void> updateStatus(
            @RequestParam Long publishId,
            @RequestParam String status) {
        publishService.updateStatus(publishId, status);
        return R.ok();
    }
}
