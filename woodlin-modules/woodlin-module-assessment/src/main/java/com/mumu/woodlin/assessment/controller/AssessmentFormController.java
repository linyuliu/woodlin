package com.mumu.woodlin.assessment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.service.IAssessmentFormService;
import com.mumu.woodlin.common.response.R;

/**
 * 测评主体管理接口（存根）
 *
 * <p>提供测评模块的边界入口，完整 CRUD 由后续迭代实现。
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
     * 根据ID获取测评详情
     *
     * @param formId 测评ID
     * @return 测评实体
     */
    @GetMapping("/{formId}")
    @Operation(summary = "获取测评详情", description = "根据测评ID获取量表/试卷/问卷详情")
    public R<AssessmentForm> getById(@PathVariable Long formId) {
        return R.ok(assessmentFormService.getById(formId));
    }
}
