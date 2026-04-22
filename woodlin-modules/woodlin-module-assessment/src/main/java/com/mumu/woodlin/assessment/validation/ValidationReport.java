package com.mumu.woodlin.assessment.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated result of publish-time validation for an assessment version.
 *
 * <p>A report is considered {@link #isValid() valid} when it contains no
 * {@link ValidationIssue.Severity#ERROR} issues. Warnings are informational
 * and do not block publishing.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
public final class ValidationReport {

    private final Long versionId;
    private final List<ValidationIssue> issues;

    private ValidationReport(Long versionId, List<ValidationIssue> issues) {
        this.versionId = versionId;
        this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
    }

    /**
     * Creates a {@link ValidationReport}.
     *
     * @param versionId the version that was validated
     * @param issues    all detected issues
     * @return new instance
     */
    public static ValidationReport of(Long versionId, List<ValidationIssue> issues) {
        return new ValidationReport(versionId, issues);
    }

    /**
     * @return the version that was validated
     */
    public Long getVersionId() {
        return versionId;
    }

    /**
     * @return unmodifiable list of all detected issues
     */
    public List<ValidationIssue> getIssues() {
        return issues;
    }

    /**
     * @return {@code true} if there are no {@link ValidationIssue.Severity#ERROR} issues
     */
    public boolean isValid() {
        return issues.stream().noneMatch(i -> i.getSeverity() == ValidationIssue.Severity.ERROR);
    }

    /**
     * @return number of ERROR-level issues
     */
    public long getErrorCount() {
        return issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR).count();
    }

    /**
     * @return number of WARNING-level issues
     */
    public long getWarningCount() {
        return issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING).count();
    }

    @Override
    public String toString() {
        return "ValidationReport{versionId=" + versionId
                + ", valid=" + isValid()
                + ", errors=" + getErrorCount()
                + ", warnings=" + getWarningCount()
                + "}";
    }
}
