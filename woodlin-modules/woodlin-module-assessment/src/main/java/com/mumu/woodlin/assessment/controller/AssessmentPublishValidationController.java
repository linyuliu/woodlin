package com.mumu.woodlin.assessment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mumu.woodlin.assessment.service.IAssessmentPublishValidationService;
import com.mumu.woodlin.assessment.validation.ValidationReport;
import com.mumu.woodlin.common.response.R;

/**
 * Publish-time validation API for assessment versions and publish instances.
 *
 * <p>Exposes two read-only endpoints that run the full Z3-backed validation suite
 * and return a {@link ValidationReport} in the standard response envelope.
 * Callers should inspect {@link ValidationReport#isValid()} and
 * {@link ValidationReport#getIssues()} to decide whether to proceed with publishing.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/assessment/publish/validate")
@RequiredArgsConstructor
@Tag(name = "发布校验", description = "测评版本/发布实例发布前合规校验接口")
public class AssessmentPublishValidationController {

    private final IAssessmentPublishValidationService validationService;

    /**
     * Validates an assessment version's publish readiness.
     *
     * <p>Performs all constraint checks (score ranges, dimension feasibility,
     * reverse scoring completeness, rule targets, section structure, anchor
     * consistency) and returns a structured report.</p>
     *
     * @param versionId version to validate
     * @return validation report
     */
    @GetMapping("/version/{versionId}")
    @Operation(summary = "校验测评版本发布合规性",
               description = "对测评版本进行发布前完整性和一致性校验，包含 Z3 约束验证。"
                       + "返回结构化校验报告，isValid=false 表示有阻塞性错误。")
    public R<ValidationReport> validateVersion(
            @Parameter(description = "版本ID", required = true)
            @PathVariable Long versionId) {
        log.info("Received validation request for versionId={}", versionId);
        ValidationReport report = validationService.validateVersion(versionId);
        return R.ok(report);
    }

    /**
     * Validates a publish instance together with its underlying version.
     *
     * <p>Includes all version-level checks plus publish-specific checks such as
     * the time-window validity.</p>
     *
     * @param publishId publish instance to validate
     * @return validation report
     */
    @GetMapping("/publish/{publishId}")
    @Operation(summary = "校验发布实例合规性",
               description = "对发布实例及其关联版本进行完整校验（含时间窗口等发布级约束）。")
    public R<ValidationReport> validatePublish(
            @Parameter(description = "发布ID", required = true)
            @PathVariable Long publishId) {
        log.info("Received validation request for publishId={}", publishId);
        ValidationReport report = validationService.validatePublish(publishId);
        return R.ok(report);
    }
}
