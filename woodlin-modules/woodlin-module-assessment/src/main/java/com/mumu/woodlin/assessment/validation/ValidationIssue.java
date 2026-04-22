package com.mumu.woodlin.assessment.validation;

/**
 * A single issue detected during publish-time validation of an assessment version.
 *
 * <p>Carries severity, a machine-readable code, a human-readable message, and
 * the type/code of the domain element that triggered the issue.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
public final class ValidationIssue {

    /** Severity level of a validation issue. */
    public enum Severity {
        /** Configuration error that must be fixed before the version can be published. */
        ERROR,
        /** Non-blocking advisory; the version can still be published but may behave unexpectedly. */
        WARNING
    }

    private final Severity severity;
    private final String code;
    private final String message;
    private final String targetType;
    private final String targetCode;

    /**
     * Constructs a validation issue.
     *
     * @param severity   severity level
     * @param code       machine-readable issue code (e.g. {@code "SCORE_RANGE_INVALID"})
     * @param message    human-readable description
     * @param targetType domain element type ({@code "item"}, {@code "section"},
     *                   {@code "dimension"}, {@code "rule"}, {@code "publish"}, {@code "version"})
     * @param targetCode identifier of the problematic element
     */
    public ValidationIssue(Severity severity, String code, String message,
                            String targetType, String targetCode) {
        this.severity = severity;
        this.code = code;
        this.message = message;
        this.targetType = targetType;
        this.targetCode = targetCode != null ? targetCode : "";
    }

    /** @return severity of this issue */
    public Severity getSeverity() { return severity; }

    /** @return machine-readable issue code */
    public String getCode() { return code; }

    /** @return human-readable message */
    public String getMessage() { return message; }

    /** @return the type of the domain element that triggered this issue */
    public String getTargetType() { return targetType; }

    /** @return identifier/code of the domain element that triggered this issue */
    public String getTargetCode() { return targetCode; }

    @Override
    public String toString() {
        return "[" + severity + "] " + code + " (" + targetType + ":" + targetCode + "): " + message;
    }
}
