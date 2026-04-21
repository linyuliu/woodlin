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
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.model.query.AssessmentFormVersionQuery;
import com.mumu.woodlin.assessment.service.IAssessmentFormVersionService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;

/**
 * 测评版本管理接口
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/form-version")
@RequiredArgsConstructor
@Validated
@Tag(name = "测评版本管理", description = "测评版本管理接口")
public class AssessmentFormVersionController {

    private final IAssessmentFormVersionService versionService;

    /**
     * 分页查询版本列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询版本列表")
    public R<PageResult<AssessmentFormVersion>> list(AssessmentFormVersionQuery query) {
        return R.ok(versionService.pageList(query));
    }

    /**
     * 根据ID获取版本详情
     */
    @GetMapping("/{versionId}")
    @Operation(summary = "获取版本详情")
    public R<AssessmentFormVersion> getById(
            @Parameter(description = "版本ID", required = true) @PathVariable Long versionId) {
        return R.ok(versionService.getById(versionId));
    }

    /**
     * 新增版本
     */
    @PostMapping
    @Operation(summary = "新增版本")
    public R<Void> add(@Valid @RequestBody AssessmentFormVersion version) {
        versionService.save(version);
        return R.ok();
    }

    /**
     * 修改版本
     */
    @PutMapping
    @Operation(summary = "修改版本")
    public R<Void> update(@Valid @RequestBody AssessmentFormVersion version) {
        versionService.updateById(version);
        return R.ok();
    }

    /**
     * 删除版本
     */
    @DeleteMapping("/{versionId}")
    @Operation(summary = "删除版本")
    public R<Void> delete(@PathVariable Long versionId) {
        versionService.removeById(versionId);
        return R.ok();
    }
}
