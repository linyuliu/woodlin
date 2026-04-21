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

import com.mumu.woodlin.assessment.model.entity.AssessmentNormSet;
import com.mumu.woodlin.assessment.model.query.AssessmentNormSetQuery;
import com.mumu.woodlin.assessment.service.IAssessmentNormSetService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;

/**
 * 常模集管理接口
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/norm-set")
@RequiredArgsConstructor
@Validated
@Tag(name = "常模管理", description = "测评常模集管理接口")
public class AssessmentNormSetController {

    private final IAssessmentNormSetService normSetService;

    /**
     * 分页查询常模集列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询常模集列表")
    public R<PageResult<AssessmentNormSet>> list(AssessmentNormSetQuery query) {
        return R.ok(normSetService.pageList(query));
    }

    /**
     * 根据ID获取常模集详情
     */
    @GetMapping("/{normSetId}")
    @Operation(summary = "获取常模集详情")
    public R<AssessmentNormSet> getById(
            @Parameter(description = "常模集ID", required = true) @PathVariable Long normSetId) {
        return R.ok(normSetService.getById(normSetId));
    }

    /**
     * 新增常模集
     */
    @PostMapping
    @Operation(summary = "新增常模集")
    public R<Void> add(@Valid @RequestBody AssessmentNormSet normSet) {
        normSetService.save(normSet);
        return R.ok();
    }

    /**
     * 修改常模集
     */
    @PutMapping
    @Operation(summary = "修改常模集")
    public R<Void> update(@Valid @RequestBody AssessmentNormSet normSet) {
        normSetService.updateById(normSet);
        return R.ok();
    }

    /**
     * 删除常模集
     */
    @DeleteMapping("/{normSetId}")
    @Operation(summary = "删除常模集")
    public R<Void> delete(@PathVariable Long normSetId) {
        normSetService.removeById(normSetId);
        return R.ok();
    }
}
