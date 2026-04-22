package com.mumu.woodlin.assessment.validation;

import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.mumu.woodlin.assessment.enums.ReverseMode;
import com.mumu.woodlin.assessment.enums.ScoreMode;
import com.mumu.woodlin.assessment.model.entity.AssessmentDimension;
import com.mumu.woodlin.assessment.model.entity.AssessmentDimensionItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentOption;
import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.entity.AssessmentRule;
import com.mumu.woodlin.assessment.model.entity.AssessmentSection;
import com.mumu.woodlin.z3.model.ConstraintResult;
import com.mumu.woodlin.z3.service.Z3SolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Z3-backed publish-time validator for assessment versions.
 *
 * <p>Accepts a {@link VersionContext} snapshot of all domain data and returns a
 * {@link ValidationReport} with structured {@link ValidationIssue} entries.
 * Some checks delegate to {@link Z3SolverService} for SMT-level satisfiability
 * proofs; others are enforced with ordinary Java logic.</p>
 *
 * <h3>Checks performed</h3>
 * <ol>
 *   <li><b>Version/schema compilation status</b> – version must not be in {@code draft}
 *       and schema must have no {@code compile_error}.</li>
 *   <li><b>Item score range feasibility (Z3)</b> – for every scored item that declares
 *       both {@code min_score} and {@code max_score}, Z3 verifies {@code min ≤ max}.</li>
 *   <li><b>Dimension score sum reachability (Z3)</b> – for {@code SUM}/{@code MEAN}/
 *       {@code WEIGHTED_SUM} dimensions, Z3 verifies there exists a valid score
 *       assignment for the mapped items.</li>
 *   <li><b>Reverse scoring map completeness</b> – TABLE-mode items require all
 *       options to carry {@code score_reverse_value}; FORMULA-mode items require both
 *       {@code min_score} and {@code max_score}.</li>
 *   <li><b>Rule target existence</b> – every active rule's {@code target_code} must
 *       refer to an existing item/section/dimension code.</li>
 *   <li><b>Section structure</b> – every required section must contain at least one item.</li>
 *   <li><b>Anchor item consistency</b> – sections that declare an {@code anchor_code}
 *       must contain at least one item with {@code is_anchor = true}.</li>
 *   <li><b>Publish time window</b> – when a publish instance is included,
 *       {@code start_time} must precede {@code end_time}.</li>
 * </ol>
 *
 * @author mumu
 * @since 2025-01-01
 */
@Service
public class AssessmentPublishValidator {

    private static final Logger log = LoggerFactory.getLogger(AssessmentPublishValidator.class);

    /** Fallback max-score used when an item omits {@code max_score}. */
    private static final int DEFAULT_MAX_SCORE = 1000;

    private final Z3SolverService z3SolverService;

    /**
     * Constructs the validator.
     *
     * @param z3SolverService Z3 constraint solver
     */
    public AssessmentPublishValidator(Z3SolverService z3SolverService) {
        this.z3SolverService = z3SolverService;
    }

    /**
     * Validates the supplied version context and returns a structured report.
     *
     * @param ctx all domain data for the version to validate
     * @return validation report (never {@code null})
     */
    public ValidationReport validate(VersionContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();

        checkVersionStatus(ctx, issues);
        checkSchemaCompilation(ctx, issues);
        checkItemScoreRanges(ctx, issues);
        checkDimensionSumFeasibility(ctx, issues);
        checkReverseScoring(ctx, issues);
        checkRuleTargets(ctx, issues);
        checkSectionStructure(ctx, issues);
        checkAnchorConsistency(ctx, issues);
        checkPublishTimeWindow(ctx, issues);

        ValidationReport report = ValidationReport.of(ctx.getVersionId(), issues);
        log.info("Assessment validation complete – versionId={}, valid={}, errors={}, warnings={}",
                ctx.getVersionId(), report.isValid(), report.getErrorCount(), report.getWarningCount());
        return report;
    }

    // -------------------------------------------------------------------------
    // Check 1: Version and schema compilation status
    // -------------------------------------------------------------------------

    private void checkVersionStatus(VersionContext ctx, List<ValidationIssue> issues) {
        if (ctx.getVersion() == null) {
            issues.add(error("VERSION_NOT_FOUND", "version", String.valueOf(ctx.getVersionId()),
                    "Version record could not be loaded."));
            return;
        }
        String status = ctx.getVersion().getStatus();
        if ("draft".equals(status)) {
            issues.add(error("VERSION_DRAFT", "version", ctx.getVersion().getVersionNo(),
                    "Version is still in draft status; it must be compiled before publishing."));
        }
    }

    private void checkSchemaCompilation(VersionContext ctx, List<ValidationIssue> issues) {
        if (ctx.getSchema() == null) {
            issues.add(warning("SCHEMA_MISSING", "version", String.valueOf(ctx.getVersionId()),
                    "No schema is associated with this version."));
            return;
        }
        if (ctx.getSchema().getCompileError() != null && !ctx.getSchema().getCompileError().isBlank()) {
            issues.add(error("SCHEMA_COMPILE_ERROR", "version", String.valueOf(ctx.getVersionId()),
                    "Schema has a compilation error: " + ctx.getSchema().getCompileError()));
        }
    }

    // -------------------------------------------------------------------------
    // Check 2: Item score range feasibility (Z3)
    // -------------------------------------------------------------------------

    /**
     * For each scored item that declares both {@code min_score} and {@code max_score},
     * uses Z3 to verify that the integer range {@code [min, max]} is non-empty
     * (i.e. {@code min ≤ max}).
     */
    private void checkItemScoreRanges(VersionContext ctx, List<ValidationIssue> issues) {
        for (AssessmentItem item : ctx.getItems()) {
            if (!Boolean.TRUE.equals(item.getIsScored())) {
                continue;
            }
            BigDecimal minBd = item.getMinScore();
            BigDecimal maxBd = item.getMaxScore();
            if (minBd == null || maxBd == null) {
                // Both omitted is acceptable (unconstrained); one present without the other is a warning.
                if (minBd != null || maxBd != null) {
                    issues.add(warning("ITEM_PARTIAL_SCORE_RANGE", "item", item.getItemCode(),
                            "Scored item declares only one of min_score/max_score; both should be set together."));
                }
                continue;
            }
            int min = minBd.intValue();
            int max = maxBd.intValue();
            // Z3: check that the range [min, max] is satisfiable (i.e. min ≤ max).
            ConstraintResult result = z3SolverService.checkIntRange("score_" + sanitize(item.getItemCode()), min, max, null);
            if (!result.isSatisfiable()) {
                issues.add(error("ITEM_SCORE_RANGE_INVALID", "item", item.getItemCode(),
                        "Item score range [" + min + ", " + max + "] is empty (min > max)."));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 3: Dimension score sum reachability (Z3)
    // -------------------------------------------------------------------------

    /**
     * For {@code SUM}, {@code MEAN}, and {@code WEIGHTED_SUM} dimensions, uses
     * {@link Z3SolverService#checkLinearSum} to confirm that the item score ranges
     * permit at least one valid assignment where the sum is ≥ 0.
     * Also warns when a scored dimension has no mapped items, or when the maximum
     * achievable sum is zero (all items capped at 0).
     */
    private void checkDimensionSumFeasibility(VersionContext ctx, List<ValidationIssue> issues) {
        // Build item lookup maps for quick access.
        Map<Long, AssessmentItem> itemById = ctx.getItems().stream()
                .collect(Collectors.toMap(AssessmentItem::getItemId, i -> i, (a, b) -> a));

        // Group dimension-item mappings by dimension.
        Map<Long, List<AssessmentDimensionItem>> dimMappings = ctx.getDimensionItems().stream()
                .collect(Collectors.groupingBy(AssessmentDimensionItem::getDimensionId));

        for (AssessmentDimension dim : ctx.getDimensions()) {
            ScoreMode mode = parseScoreMode(dim.getScoreMode());
            if (mode == null || mode == ScoreMode.CUSTOM_DSL || mode == ScoreMode.MAX || mode == ScoreMode.MIN) {
                // Custom DSL and MAX/MIN modes cannot be statically verified via linear sum.
                continue;
            }

            List<AssessmentDimensionItem> mappings = dimMappings.getOrDefault(dim.getDimensionId(), List.of());
            if (mappings.isEmpty()) {
                issues.add(warning("DIMENSION_NO_ITEMS", "dimension", dim.getDimensionCode(),
                        "Dimension '" + dim.getDimensionName() + "' (mode=" + mode.getCode() + ") has no item mappings."));
                continue;
            }

            // Collect per-item min/max scores.
            List<String> varNames = new ArrayList<>();
            List<Integer> mins = new ArrayList<>();
            List<Integer> maxs = new ArrayList<>();

            for (AssessmentDimensionItem di : mappings) {
                AssessmentItem item = itemById.get(di.getItemId());
                if (item == null) {
                    continue;
                }
                varNames.add("dim_" + sanitize(dim.getDimensionCode()) + "_" + sanitize(item.getItemCode()));
                mins.add(item.getMinScore() != null ? item.getMinScore().intValue() : 0);
                maxs.add(item.getMaxScore() != null ? item.getMaxScore().intValue() : DEFAULT_MAX_SCORE);
            }

            if (varNames.isEmpty()) {
                continue;
            }

            // Check that the maximum achievable sum is > 0 (at least one item can score positively).
            int totalMax = maxs.stream().mapToInt(Integer::intValue).sum();
            if (totalMax <= 0) {
                issues.add(warning("DIMENSION_ZERO_MAX_SCORE", "dimension", dim.getDimensionCode(),
                        "Dimension '" + dim.getDimensionName() + "' – all mapped items have max_score ≤ 0; "
                                + "the dimension will always score 0."));
                continue;
            }

            // Z3: verify items can be assigned scores within their ranges such that sum = totalMinSum.
            int totalMin = mins.stream().mapToInt(Integer::intValue).sum();
            int[] minsArr = mins.stream().mapToInt(Integer::intValue).toArray();
            int[] maxsArr = maxs.stream().mapToInt(Integer::intValue).toArray();

            ConstraintResult result = z3SolverService.checkLinearSum(varNames, totalMin, minsArr, maxsArr);
            if (!result.isSatisfiable()) {
                // This should only occur if individual ranges are already broken (caught by check 2),
                // but we report it here at the dimension level for clarity.
                issues.add(error("DIMENSION_SUM_INFEASIBLE", "dimension", dim.getDimensionCode(),
                        "Dimension '" + dim.getDimensionName() + "' – item score ranges cannot produce "
                                + "a valid sum (contradictory constraints)."));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 4: Reverse scoring map completeness
    // -------------------------------------------------------------------------

    /**
     * Checks reverse scoring configuration:
     * <ul>
     *   <li>TABLE mode – every option of the item must have a non-null
     *       {@code score_reverse_value}.</li>
     *   <li>FORMULA mode – the item must declare both {@code min_score} and
     *       {@code max_score} (needed to compute {@code max + min - raw}).</li>
     * </ul>
     */
    private void checkReverseScoring(VersionContext ctx, List<ValidationIssue> issues) {
        Map<Long, AssessmentItem> itemById = ctx.getItems().stream()
                .collect(Collectors.toMap(AssessmentItem::getItemId, i -> i, (a, b) -> a));

        // Build option lookup: itemId → list of options.
        Map<Long, List<AssessmentOption>> optionsByItem = ctx.getOptions().stream()
                .filter(o -> o.getItemId() != null)
                .collect(Collectors.groupingBy(AssessmentOption::getItemId));

        // Collect dimension-item mappings that specify a reverse mode.
        for (AssessmentDimensionItem di : ctx.getDimensionItems()) {
            ReverseMode mode = parseReverseMode(di.getReverseMode());
            if (mode == null || mode == ReverseMode.NONE) {
                continue;
            }

            AssessmentItem item = itemById.get(di.getItemId());
            if (item == null) {
                continue;
            }

            if (mode == ReverseMode.TABLE) {
                // All options must have score_reverse_value set.
                List<AssessmentOption> opts = optionsByItem.getOrDefault(item.getItemId(), List.of());
                if (opts.isEmpty()) {
                    issues.add(warning("REVERSE_TABLE_NO_OPTIONS", "item", item.getItemCode(),
                            "Item uses TABLE reverse scoring but has no options; "
                                    + "reverse score map is empty."));
                    continue;
                }
                for (AssessmentOption opt : opts) {
                    if (opt.getScoreReverseValue() == null) {
                        issues.add(error("REVERSE_TABLE_MISSING_VALUE", "item", item.getItemCode(),
                                "Option '" + opt.getOptionCode() + "' of item '"
                                        + item.getItemCode() + "' is missing score_reverse_value "
                                        + "(required for TABLE reverse scoring)."));
                    }
                }
            } else if (mode == ReverseMode.FORMULA) {
                // Item must have both min_score and max_score for formula: max + min - raw.
                if (item.getMaxScore() == null || item.getMinScore() == null) {
                    issues.add(error("REVERSE_FORMULA_MISSING_BOUNDS", "item", item.getItemCode(),
                            "Item uses FORMULA reverse scoring (score = max + min - raw) "
                                    + "but is missing min_score and/or max_score."));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 5: Rule target existence
    // -------------------------------------------------------------------------

    /**
     * Verifies that every active rule's {@code target_code} refers to an existing
     * item/section/dimension code within the version.
     */
    private void checkRuleTargets(VersionContext ctx, List<ValidationIssue> issues) {
        Set<String> itemCodes = ctx.getItems().stream()
                .map(AssessmentItem::getItemCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> sectionCodes = ctx.getSections().stream()
                .map(AssessmentSection::getSectionCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> dimensionCodes = ctx.getDimensions().stream()
                .map(AssessmentDimension::getDimensionCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (AssessmentRule rule : ctx.getRules()) {
            if (!Boolean.TRUE.equals(rule.getIsActive())) {
                continue;
            }
            String targetType = rule.getTargetType();
            String targetCode = rule.getTargetCode();
            if (targetCode == null || targetCode.isBlank()) {
                continue;
            }

            boolean missing = switch (targetType != null ? targetType : "") {
                case "item" -> !itemCodes.contains(targetCode);
                case "section" -> !sectionCodes.contains(targetCode);
                case "dimension" -> !dimensionCodes.contains(targetCode);
                default -> false;
            };

            if (missing) {
                issues.add(error("RULE_TARGET_NOT_FOUND", "rule", rule.getRuleCode(),
                        "Rule '" + rule.getRuleCode() + "' references " + targetType + " '"
                                + targetCode + "' which does not exist in this version."));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 6: Section structure – required sections must have items
    // -------------------------------------------------------------------------

    /**
     * Verifies that every required section contains at least one item.
     */
    private void checkSectionStructure(VersionContext ctx, List<ValidationIssue> issues) {
        Set<Long> sectionIdsWithItems = ctx.getItems().stream()
                .map(AssessmentItem::getSectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (AssessmentSection section : ctx.getSections()) {
            if (!Boolean.TRUE.equals(section.getIsRequired())) {
                continue;
            }
            if (!sectionIdsWithItems.contains(section.getSectionId())) {
                issues.add(error("SECTION_EMPTY", "section", section.getSectionCode(),
                        "Required section '" + section.getSectionCode() + "' contains no items."));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 7: Anchor item consistency
    // -------------------------------------------------------------------------

    /**
     * Verifies that any section with an {@code anchor_code} has at least one item
     * where {@code is_anchor = true}.  Also warns when randomization is active on a
     * section that contains anchor items (randomizing anchor positions can corrupt
     * cross-form equating).
     */
    private void checkAnchorConsistency(VersionContext ctx, List<ValidationIssue> issues) {
        // Group items by sectionId.
        Map<Long, List<AssessmentItem>> itemsBySection = ctx.getItems().stream()
                .filter(i -> i.getSectionId() != null)
                .collect(Collectors.groupingBy(AssessmentItem::getSectionId));

        for (AssessmentSection section : ctx.getSections()) {
            if (section.getAnchorCode() == null || section.getAnchorCode().isBlank()) {
                continue;
            }
            List<AssessmentItem> sectionItems = itemsBySection.getOrDefault(section.getSectionId(), List.of());
            boolean hasAnchorItem = sectionItems.stream().anyMatch(i -> Boolean.TRUE.equals(i.getIsAnchor()));
            if (!hasAnchorItem) {
                issues.add(warning("ANCHOR_SECTION_NO_ANCHOR_ITEM", "section", section.getSectionCode(),
                        "Section '" + section.getSectionCode() + "' declares anchor_code='"
                                + section.getAnchorCode() + "' but contains no items marked is_anchor=true."));
            }

            // Warn if anchor section is randomized (item randomization disrupts anchor validity).
            String rand = section.getRandomStrategy();
            if (rand != null && !"none".equalsIgnoreCase(rand) && hasAnchorItem) {
                issues.add(warning("ANCHOR_SECTION_RANDOMIZED", "section", section.getSectionCode(),
                        "Section '" + section.getSectionCode() + "' contains anchor items but has "
                                + "randomization strategy '" + rand + "'; this may invalidate equating."));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 8: Publish time window
    // -------------------------------------------------------------------------

    /**
     * Verifies that when a publish instance defines both {@code start_time} and
     * {@code end_time}, the window is positive ({@code start < end}).
     */
    private void checkPublishTimeWindow(VersionContext ctx, List<ValidationIssue> issues) {
        AssessmentPublish publish = ctx.getPublish();
        if (publish == null || publish.getStartTime() == null || publish.getEndTime() == null) {
            return;
        }
        if (!publish.getStartTime().isBefore(publish.getEndTime())) {
            issues.add(error("PUBLISH_TIME_WINDOW_INVALID", "publish",
                    String.valueOf(publish.getPublishId()),
                    "Publish start_time (" + publish.getStartTime() + ") must be before end_time ("
                            + publish.getEndTime() + ")."));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ValidationIssue error(String code, String targetType, String targetCode, String message) {
        return new ValidationIssue(ValidationIssue.Severity.ERROR, code, message, targetType, targetCode);
    }

    private static ValidationIssue warning(String code, String targetType, String targetCode, String message) {
        return new ValidationIssue(ValidationIssue.Severity.WARNING, code, message, targetType, targetCode);
    }

    /** Parses a score-mode string to enum; returns {@code null} on unrecognised values. */
    private static ScoreMode parseScoreMode(String raw) {
        if (raw == null) {
            return null;
        }
        for (ScoreMode m : ScoreMode.values()) {
            if (m.getCode().equalsIgnoreCase(raw)) {
                return m;
            }
        }
        return null;
    }

    /** Parses a reverse-mode string to enum; returns {@code null} on unrecognised values. */
    private static ReverseMode parseReverseMode(String raw) {
        if (raw == null) {
            return null;
        }
        for (ReverseMode m : ReverseMode.values()) {
            if (m.getCode().equalsIgnoreCase(raw)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Sanitises a code string for use as a Z3 variable name (Z3 names must not
     * contain characters that are special in the SMT-LIB grammar).
     */
    private static String sanitize(String code) {
        if (code == null) {
            return "null";
        }
        return code.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
