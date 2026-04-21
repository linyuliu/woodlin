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

import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.model.query.AssessmentFormQuery;
import com.mumu.woodlin.assessment.service.IAssessmentFormService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;

/**
 * 测评主体管理接口
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/form")
@RequiredArgsConstructor
@Validated
@Tag(name = "测评管理", description = "量表/试卷/问卷测评主体管理接口")
public class AssessmentFormController {

    private final IAssessmentFormService assessmentFormService;

    /**
     * 分页查询测评列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询测评列表")
    public R<PageResult<AssessmentForm>> list(AssessmentFormQuery query) {
        return R.ok(assessmentFormService.pageList(query));
    }

    /**
     * 根据ID获取测评详情
     */
    @GetMapping("/{formId}")
    @Operation(summary = "获取测评详情")
    public R<AssessmentForm> getById(
            @Parameter(description = "测评ID", required = true) @PathVariable Long formId) {
        return R.ok(assessmentFormService.getById(formId));
    }

    /**
     * 新增测评
     */
    @PostMapping
    @Operation(summary = "新增测评")
    public R<Void> add(@Valid @RequestBody AssessmentForm form) {
        assessmentFormService.save(form);
        return R.ok();
    }

    /**
     * 修改测评
     */
    @PutMapping
    @Operation(summary = "修改测评")
    public R<Void> update(@Valid @RequestBody AssessmentForm form) {
        assessmentFormService.updateById(form);
        return R.ok();
    }

    /**
     * 删除测评
     */
    @DeleteMapping("/{formId}")
    @Operation(summary = "删除测评")
    public R<Void> delete(@PathVariable Long formId) {
        assessmentFormService.removeById(formId);
        return R.ok();
    }

    /**
     * 更新测评状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新测评状态")
    public R<Void> updateStatus(
            @RequestParam Long formId,
            @RequestParam Integer status) {
        assessmentFormService.updateStatus(formId, status);
        return R.ok();
    }
}
