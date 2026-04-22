package com.mumu.woodlin.assessment.validation;

import com.mumu.woodlin.assessment.model.entity.*;
import com.mumu.woodlin.z3.service.Z3SolverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AssessmentPublishValidator}.
 *
 * <p>All tests instantiate {@link Z3SolverService} directly (no Spring context)
 * and construct minimal {@link VersionContext} snapshots in-process.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
@DisplayName("AssessmentPublishValidator")
class AssessmentPublishValidatorTest {

    private AssessmentPublishValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AssessmentPublishValidator(new Z3SolverService());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static AssessmentFormVersion compiledVersion(Long versionId) {
        AssessmentFormVersion v = new AssessmentFormVersion();
        v.setVersionId(versionId);
        v.setVersionNo("1.0.0");
        v.setStatus("compiled");
        return v;
    }

    private static AssessmentFormVersion draftVersion(Long versionId) {
        AssessmentFormVersion v = new AssessmentFormVersion();
        v.setVersionId(versionId);
        v.setVersionNo("0.1.0");
        v.setStatus("draft");
        return v;
    }

    private static AssessmentSchema cleanSchema() {
        AssessmentSchema s = new AssessmentSchema();
        s.setSchemaId(1L);
        s.setCompileError(null);
        return s;
    }

    private static AssessmentSchema errorSchema(String error) {
        AssessmentSchema s = new AssessmentSchema();
        s.setSchemaId(1L);
        s.setCompileError(error);
        return s;
    }

    private static AssessmentItem scoredItem(Long id, String code, int min, int max) {
        AssessmentItem item = new AssessmentItem();
        item.setItemId(id);
        item.setItemCode(code);
        item.setIsScored(true);
        item.setMinScore(BigDecimal.valueOf(min));
        item.setMaxScore(BigDecimal.valueOf(max));
        return item;
    }

    private static AssessmentItem unscoredItem(Long id, String code) {
        AssessmentItem item = new AssessmentItem();
        item.setItemId(id);
        item.setItemCode(code);
        item.setIsScored(false);
        return item;
    }

    private static VersionContext minimalContext(Long versionId) {
        return new VersionContext.Builder()
                .versionId(versionId)
                .version(compiledVersion(versionId))
                .schema(cleanSchema())
                .items(List.of())
                .options(List.of())
                .dimensions(List.of())
                .dimensionItems(List.of())
                .sections(List.of())
                .rules(List.of())
                .build();
    }

    // -------------------------------------------------------------------------
    // Basic contract
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("minimal valid version produces no issues")
    void emptyVersionIsValid() {
        ValidationReport report = validator.validate(minimalContext(1L));
        assertNotNull(report);
        assertTrue(report.isValid());
        assertEquals(0, report.getErrorCount());
    }

    // -------------------------------------------------------------------------
    // Version status
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("version status checks")
    class VersionStatusTests {

        @Test
        @DisplayName("draft version produces VERSION_DRAFT error")
        void draftVersionIsInvalid() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L)
                    .version(draftVersion(1L))
                    .schema(cleanSchema())
                    .items(List.of()).options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "VERSION_DRAFT"));
        }

        @Test
        @DisplayName("missing version produces VERSION_NOT_FOUND error")
        void missingVersionIsInvalid() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(99L)
                    .version(null)
                    .schema(null)
                    .items(List.of()).options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "VERSION_NOT_FOUND"));
        }
    }

    // -------------------------------------------------------------------------
    // Schema compilation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("schema compilation checks")
    class SchemaCompilationTests {

        @Test
        @DisplayName("schema with compile error blocks publishing")
        void schemaCompileError() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L)
                    .version(compiledVersion(1L))
                    .schema(errorSchema("SyntaxError at line 5"))
                    .items(List.of()).options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "SCHEMA_COMPILE_ERROR"));
        }

        @Test
        @DisplayName("null schema produces SCHEMA_MISSING warning (not error)")
        void missingSchemaWarning() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L)
                    .version(compiledVersion(1L))
                    .schema(null)
                    .items(List.of()).options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();

            ValidationReport report = validator.validate(ctx);
            // Warning only – still "valid" from an error perspective
            assertTrue(report.isValid());
            assertTrue(hasCode(report, "SCHEMA_MISSING"));
        }
    }

    // -------------------------------------------------------------------------
    // Item score range (Z3)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("item score range checks (Z3)")
    class ItemScoreRangeTests {

        @Test
        @DisplayName("valid score range [0,100] passes")
        void validRange() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(scoredItem(1L, "Q01", 0, 100)))
                    .options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("inverted range [10,5] produces ITEM_SCORE_RANGE_INVALID error")
        void invertedRange() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(scoredItem(1L, "Q01", 10, 5)))
                    .options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();
            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "ITEM_SCORE_RANGE_INVALID"));
        }

        @Test
        @DisplayName("equal min/max [5,5] is satisfiable")
        void equalMinMax() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(scoredItem(1L, "Q01", 5, 5)))
                    .options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("unscored items are not validated for score range")
        void unscoredItemSkipped() {
            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(unscoredItem(1L, "Q01")))
                    .options(List.of()).dimensions(List.of())
                    .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                    .build();
            assertTrue(validator.validate(ctx).isValid());
        }
    }

    // -------------------------------------------------------------------------
    // Dimension sum feasibility (Z3)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dimension sum feasibility checks (Z3)")
    class DimensionSumTests {

        @Test
        @DisplayName("SUM dimension with positive-max items passes")
        void sumDimensionValid() {
            AssessmentDimension dim = new AssessmentDimension();
            dim.setDimensionId(10L);
            dim.setDimensionCode("D1");
            dim.setDimensionName("Cognition");
            dim.setScoreMode("sum");

            AssessmentItem item = scoredItem(1L, "Q01", 0, 5);
            AssessmentDimensionItem di = new AssessmentDimensionItem();
            di.setDimensionId(10L);
            di.setItemId(1L);
            di.setVersionId(1L);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item))
                    .options(List.of())
                    .dimensions(List.of(dim))
                    .dimensionItems(List.of(di))
                    .sections(List.of()).rules(List.of())
                    .build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("SUM dimension with no items produces DIMENSION_NO_ITEMS warning")
        void sumDimensionNoItems() {
            AssessmentDimension dim = new AssessmentDimension();
            dim.setDimensionId(10L);
            dim.setDimensionCode("D1");
            dim.setDimensionName("Cognition");
            dim.setScoreMode("sum");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of())
                    .options(List.of())
                    .dimensions(List.of(dim))
                    .dimensionItems(List.of())
                    .sections(List.of()).rules(List.of())
                    .build();
            ValidationReport report = validator.validate(ctx);
            assertTrue(report.isValid()); // warning only
            assertTrue(hasCode(report, "DIMENSION_NO_ITEMS"));
        }

        @Test
        @DisplayName("SUM dimension where all items have maxScore=0 produces DIMENSION_ZERO_MAX_SCORE warning")
        void sumDimensionZeroMax() {
            AssessmentDimension dim = new AssessmentDimension();
            dim.setDimensionId(10L);
            dim.setDimensionCode("D1");
            dim.setDimensionName("Cognition");
            dim.setScoreMode("sum");

            AssessmentItem item = scoredItem(1L, "Q01", 0, 0);
            AssessmentDimensionItem di = new AssessmentDimensionItem();
            di.setDimensionId(10L);
            di.setItemId(1L);
            di.setVersionId(1L);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item))
                    .options(List.of())
                    .dimensions(List.of(dim))
                    .dimensionItems(List.of(di))
                    .sections(List.of()).rules(List.of())
                    .build();
            ValidationReport report = validator.validate(ctx);
            assertTrue(report.isValid()); // warning only
            assertTrue(hasCode(report, "DIMENSION_ZERO_MAX_SCORE"));
        }
    }

    // -------------------------------------------------------------------------
    // Reverse scoring
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reverse scoring checks")
    class ReverseScoringTests {

        @Test
        @DisplayName("TABLE reverse mode with all options filled passes")
        void tableReverseComplete() {
            AssessmentItem item = scoredItem(1L, "Q01", 1, 5);
            item.setIsReverse(true);

            AssessmentOption opt1 = option(1L, 1L, "A", 1, 5);
            AssessmentOption opt2 = option(2L, 1L, "B", 2, 4);

            AssessmentDimensionItem di = new AssessmentDimensionItem();
            di.setItemId(1L);
            di.setDimensionId(10L);
            di.setVersionId(1L);
            di.setReverseMode("table");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of(opt1, opt2))
                    .dimensions(List.of()).dimensionItems(List.of(di))
                    .sections(List.of()).rules(List.of()).build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("TABLE reverse mode with missing score_reverse_value produces error")
        void tableReverseMissingValue() {
            AssessmentItem item = scoredItem(1L, "Q01", 1, 5);
            item.setIsReverse(true);

            AssessmentOption opt = new AssessmentOption();
            opt.setOptionId(1L);
            opt.setItemId(1L);
            opt.setOptionCode("A");
            opt.setScoreValue(BigDecimal.ONE);
            opt.setScoreReverseValue(null); // missing!

            AssessmentDimensionItem di = new AssessmentDimensionItem();
            di.setItemId(1L);
            di.setDimensionId(10L);
            di.setVersionId(1L);
            di.setReverseMode("table");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of(opt))
                    .dimensions(List.of()).dimensionItems(List.of(di))
                    .sections(List.of()).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "REVERSE_TABLE_MISSING_VALUE"));
        }

        @Test
        @DisplayName("FORMULA reverse mode without min/max produces error")
        void formulaReverseMissingBounds() {
            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("Q01");
            item.setIsScored(true);
            item.setIsReverse(true);
            // Intentionally no minScore / maxScore

            AssessmentDimensionItem di = new AssessmentDimensionItem();
            di.setItemId(1L);
            di.setDimensionId(10L);
            di.setVersionId(1L);
            di.setReverseMode("formula");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of(di))
                    .sections(List.of()).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "REVERSE_FORMULA_MISSING_BOUNDS"));
        }
    }

    // -------------------------------------------------------------------------
    // Rule target existence
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("rule target checks")
    class RuleTargetTests {

        @Test
        @DisplayName("active rule referencing existing item passes")
        void ruleWithValidTarget() {
            AssessmentItem item = scoredItem(1L, "Q01", 0, 5);

            AssessmentRule rule = new AssessmentRule();
            rule.setRuleId(1L);
            rule.setRuleCode("R01");
            rule.setIsActive(true);
            rule.setTargetType("item");
            rule.setTargetCode("Q01");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of()).rules(List.of(rule)).build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("active rule referencing missing item produces error")
        void ruleWithMissingTarget() {
            AssessmentRule rule = new AssessmentRule();
            rule.setRuleId(1L);
            rule.setRuleCode("R01");
            rule.setIsActive(true);
            rule.setTargetType("item");
            rule.setTargetCode("Q_MISSING");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of()).rules(List.of(rule)).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "RULE_TARGET_NOT_FOUND"));
        }

        @Test
        @DisplayName("inactive rules are not validated for target existence")
        void inactiveRuleIgnored() {
            AssessmentRule rule = new AssessmentRule();
            rule.setRuleId(1L);
            rule.setRuleCode("R01");
            rule.setIsActive(false);
            rule.setTargetType("item");
            rule.setTargetCode("Q_MISSING");

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of()).rules(List.of(rule)).build();
            assertTrue(validator.validate(ctx).isValid());
        }
    }

    // -------------------------------------------------------------------------
    // Section structure
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("section structure checks")
    class SectionStructureTests {

        @Test
        @DisplayName("required section with items passes")
        void requiredSectionWithItems() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setIsRequired(true);

            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("Q01");
            item.setSectionId(1L);
            item.setIsScored(false);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of(section)).rules(List.of()).build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("required section with no items produces SECTION_EMPTY error")
        void requiredSectionEmpty() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setIsRequired(true);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of(section)).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "SECTION_EMPTY"));
        }

        @Test
        @DisplayName("optional empty section is allowed")
        void optionalSectionEmpty() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setIsRequired(false);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of(section)).rules(List.of()).build();
            assertTrue(validator.validate(ctx).isValid());
        }
    }

    // -------------------------------------------------------------------------
    // Demographic constraints
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("demographic section checks")
    class DemographicTests {

        @Test
        @DisplayName("demographic item outside DEMOGRAPHIC section produces error")
        void demographicItemOutsideDemographicSection() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setSortOrder(2);

            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("D01");
            item.setSectionId(1L);
            item.setIsDemographic(true);
            item.setIsScored(false);

            VersionContext ctx = new VersionContext.Builder()
                .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                .items(List.of(item)).options(List.of())
                .dimensions(List.of()).dimensionItems(List.of())
                .sections(List.of(section)).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "DEMOGRAPHIC_ITEM_WRONG_SECTION"));
        }

        @Test
        @DisplayName("randomized DEMOGRAPHIC section produces blocking error")
        void demographicSectionCannotRandomize() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("DEMOGRAPHIC");
            section.setSortOrder(1);
            section.setRandomStrategy("random_items");

            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("D01");
            item.setSectionId(1L);
            item.setIsDemographic(true);
            item.setIsScored(false);

            VersionContext ctx = new VersionContext.Builder()
                .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                .items(List.of(item)).options(List.of())
                .dimensions(List.of()).dimensionItems(List.of())
                .sections(List.of(section)).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "DEMOGRAPHIC_SECTION_RANDOMIZED"));
        }
    }

    // -------------------------------------------------------------------------
    // Anchor consistency
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("anchor consistency checks")
    class AnchorTests {

        @Test
        @DisplayName("section with anchor_code and anchor item passes")
        void anchorSectionWithAnchorItem() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setAnchorCode("ANCHOR_01");
            section.setIsRequired(true);

            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("Q01");
            item.setSectionId(1L);
            item.setIsAnchor(true);
            item.setIsScored(false);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of(section)).rules(List.of()).build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("section with anchor_code but no anchor item produces warning")
        void anchorSectionMissingAnchorItem() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setAnchorCode("ANCHOR_01");
            section.setIsRequired(false);

            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("Q01");
            item.setSectionId(1L);
            item.setIsAnchor(false); // not an anchor!
            item.setIsScored(false);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of(section)).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertTrue(report.isValid()); // warning only
            assertTrue(hasCode(report, "ANCHOR_SECTION_NO_ANCHOR_ITEM"));
        }

        @Test
        @DisplayName("randomized anchor section produces ANCHOR_SECTION_RANDOMIZED warning")
        void anchorSectionRandomized() {
            AssessmentSection section = new AssessmentSection();
            section.setSectionId(1L);
            section.setSectionCode("S01");
            section.setAnchorCode("ANCHOR_01");
            section.setRandomStrategy("random_items");
            section.setIsRequired(false);

            AssessmentItem item = new AssessmentItem();
            item.setItemId(1L);
            item.setItemCode("Q01");
            item.setSectionId(1L);
            item.setIsAnchor(true);
            item.setIsScored(false);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of(item)).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of(section)).rules(List.of()).build();

            ValidationReport report = validator.validate(ctx);
            assertTrue(report.isValid()); // warning only
            assertTrue(hasCode(report, "ANCHOR_SECTION_RANDOMIZED"));
        }
    }

    // -------------------------------------------------------------------------
    // Publish time window
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("publish time window checks")
    class PublishTimeWindowTests {

        @Test
        @DisplayName("valid time window passes")
        void validTimeWindow() {
            AssessmentPublish publish = new AssessmentPublish();
            publish.setPublishId(1L);
            publish.setVersionId(1L);
            publish.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
            publish.setEndTime(LocalDateTime.of(2025, 6, 30, 18, 0));

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of()).rules(List.of())
                    .publish(publish).build();
            assertTrue(validator.validate(ctx).isValid());
        }

        @Test
        @DisplayName("inverted time window produces PUBLISH_TIME_WINDOW_INVALID error")
        void invertedTimeWindow() {
            AssessmentPublish publish = new AssessmentPublish();
            publish.setPublishId(1L);
            publish.setVersionId(1L);
            publish.setStartTime(LocalDateTime.of(2025, 6, 30, 18, 0));
            publish.setEndTime(LocalDateTime.of(2025, 1, 1, 9, 0)); // before start!

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of()).rules(List.of())
                    .publish(publish).build();

            ValidationReport report = validator.validate(ctx);
            assertFalse(report.isValid());
            assertTrue(hasCode(report, "PUBLISH_TIME_WINDOW_INVALID"));
        }

        @Test
        @DisplayName("publish with only start_time (no end_time) passes")
        void openEndedPublish() {
            AssessmentPublish publish = new AssessmentPublish();
            publish.setPublishId(1L);
            publish.setVersionId(1L);
            publish.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
            publish.setEndTime(null);

            VersionContext ctx = new VersionContext.Builder()
                    .versionId(1L).version(compiledVersion(1L)).schema(cleanSchema())
                    .items(List.of()).options(List.of())
                    .dimensions(List.of()).dimensionItems(List.of())
                    .sections(List.of()).rules(List.of())
                    .publish(publish).build();
            assertTrue(validator.validate(ctx).isValid());
        }
    }

    // -------------------------------------------------------------------------
    // Integration – multiple issues in one report
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("multiple issues are all collected in a single report")
    void multipleIssues() {
        // draft version + inverted score range
        VersionContext ctx = new VersionContext.Builder()
                .versionId(1L)
                .version(draftVersion(1L))
                .schema(cleanSchema())
                .items(List.of(scoredItem(1L, "Q01", 10, 5))) // inverted
                .options(List.of()).dimensions(List.of())
                .dimensionItems(List.of()).sections(List.of()).rules(List.of())
                .build();

        ValidationReport report = validator.validate(ctx);
        assertFalse(report.isValid());
        assertTrue(report.getErrorCount() >= 2, "Expected at least 2 errors");
        assertTrue(hasCode(report, "VERSION_DRAFT"));
        assertTrue(hasCode(report, "ITEM_SCORE_RANGE_INVALID"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static AssessmentOption option(Long optId, Long itemId, String code, int score, int reverseScore) {
        AssessmentOption opt = new AssessmentOption();
        opt.setOptionId(optId);
        opt.setItemId(itemId);
        opt.setOptionCode(code);
        opt.setScoreValue(BigDecimal.valueOf(score));
        opt.setScoreReverseValue(BigDecimal.valueOf(reverseScore));
        return opt;
    }

    private static boolean hasCode(ValidationReport report, String code) {
        return report.getIssues().stream().anyMatch(i -> code.equals(i.getCode()));
    }
}
