package com.mumu.woodlin.assessment.service;

import com.mumu.woodlin.assessment.validation.ValidationReport;

/**
 * Publish-time validation service for assessment versions and publish instances.
 *
 * <p>Loads all required domain data for the specified version and delegates to
 * {@link com.mumu.woodlin.assessment.validation.AssessmentPublishValidator} to
 * produce a structured {@link ValidationReport}.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentPublishValidationService {

    /**
     * Validates an assessment version's configuration.
     *
     * <p>Runs all publish-readiness checks (score ranges, dimension feasibility,
     * reverse scoring maps, rule targets, section structure, anchor consistency)
     * and returns a structured report.</p>
     *
     * @param versionId the version to validate
     * @return structured validation report; never {@code null}
     */
    ValidationReport validateVersion(Long versionId);

    /**
     * Validates a publish instance together with its underlying version.
     *
     * <p>Includes all version-level checks plus publish-instance-specific checks
     * (e.g. time-window validity).</p>
     *
     * @param publishId the publish instance to validate
     * @return structured validation report; never {@code null}
     */
    ValidationReport validatePublish(Long publishId);
}
