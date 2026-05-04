package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentOptionMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentSchemaMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentDimension;
import com.mumu.woodlin.assessment.model.entity.AssessmentDimensionItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.model.entity.AssessmentItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentOption;
import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.entity.AssessmentRule;
import com.mumu.woodlin.assessment.model.entity.AssessmentSchema;
import com.mumu.woodlin.assessment.model.entity.AssessmentSection;
import com.mumu.woodlin.assessment.service.IAssessmentDimensionItemService;
import com.mumu.woodlin.assessment.service.IAssessmentDimensionService;
import com.mumu.woodlin.assessment.service.IAssessmentFormVersionService;
import com.mumu.woodlin.assessment.service.IAssessmentItemService;
import com.mumu.woodlin.assessment.service.IAssessmentPublishService;
import com.mumu.woodlin.assessment.service.IAssessmentPublishValidationService;
import com.mumu.woodlin.assessment.service.IAssessmentRuleService;
import com.mumu.woodlin.assessment.service.IAssessmentSectionService;
import com.mumu.woodlin.assessment.validation.AssessmentPublishValidator;
import com.mumu.woodlin.assessment.validation.ValidationReport;
import com.mumu.woodlin.assessment.validation.VersionContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link IAssessmentPublishValidationService}.
 *
 * <p>Loads all domain data from the respective services/mappers and builds a
 * {@link VersionContext} before delegating to {@link AssessmentPublishValidator}.</p>
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentPublishValidationServiceImpl implements IAssessmentPublishValidationService {

    private final AssessmentPublishValidator validator;
    private final IAssessmentFormVersionService versionService;
    private final AssessmentSchemaMapper schemaMapper;
    private final IAssessmentItemService itemService;
    private final AssessmentOptionMapper optionMapper;
    private final IAssessmentDimensionService dimensionService;
    private final IAssessmentDimensionItemService dimensionItemService;
    private final IAssessmentSectionService sectionService;
    private final IAssessmentRuleService ruleService;
    private final IAssessmentPublishService publishService;

    @Override
    public ValidationReport validateVersion(Long versionId) {
        VersionContext ctx = buildContext(versionId, null);
        return validator.validate(ctx);
    }

    @Override
    public ValidationReport validatePublish(Long publishId) {
        AssessmentPublish publish = publishService.getById(publishId);
        if (publish == null) {
            log.warn("Publish instance not found: publishId={}", publishId);
            // Return an error report without a full context load.
            return ValidationReport.of(null, List.of(
                    new com.mumu.woodlin.assessment.validation.ValidationIssue(
                            com.mumu.woodlin.assessment.validation.ValidationIssue.Severity.ERROR,
                            "PUBLISH_NOT_FOUND",
                            "Publish instance " + publishId + " could not be found.",
                            "publish", String.valueOf(publishId))));
        }
        VersionContext ctx = buildContext(publish.getVersionId(), publish);
        return validator.validate(ctx);
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    /**
     * Builds a complete {@link VersionContext} for the given version (and optional publish).
     */
    private VersionContext buildContext(Long versionId, AssessmentPublish publish) {
        AssessmentFormVersion version = versionService.getById(versionId);

        // Schema (nullable – version might not be compiled yet).
        AssessmentSchema schema = version != null && version.getSchemaId() != null
                ? schemaMapper.selectById(version.getSchemaId())
                : null;

        // Items.
        List<AssessmentItem> items = itemService.list(
                new LambdaQueryWrapper<AssessmentItem>().eq(AssessmentItem::getVersionId, versionId));

        // Options – fetched by item IDs belonging to this version.
        List<AssessmentOption> options = loadOptionsForItems(items);

        // Dimensions.
        List<AssessmentDimension> dimensions = dimensionService.list(
                new LambdaQueryWrapper<AssessmentDimension>().eq(AssessmentDimension::getVersionId, versionId));

        // Dimension-item mappings.
        List<AssessmentDimensionItem> dimensionItems = dimensionItemService.list(
                new LambdaQueryWrapper<AssessmentDimensionItem>().eq(AssessmentDimensionItem::getVersionId, versionId));

        // Sections.
        List<AssessmentSection> sections = sectionService.list(
                new LambdaQueryWrapper<AssessmentSection>().eq(AssessmentSection::getVersionId, versionId));

        // Rules.
        List<AssessmentRule> rules = ruleService.list(
                new LambdaQueryWrapper<AssessmentRule>().eq(AssessmentRule::getVersionId, versionId));

        return new VersionContext.Builder()
                .versionId(versionId)
                .version(version)
                .schema(schema)
                .items(items)
                .options(options)
                .dimensions(dimensions)
                .dimensionItems(dimensionItems)
                .sections(sections)
                .rules(rules)
                .publish(publish)
                .build();
    }

    /**
     * Loads all options for the given items in a single query.
     *
     * @param items items whose options should be loaded
     * @return options list (empty when items is empty)
     */
    private List<AssessmentOption> loadOptionsForItems(List<AssessmentItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Set<Long> itemIds = items.stream()
                .map(AssessmentItem::getItemId)
                .collect(Collectors.toSet());
        return optionMapper.selectList(
                new LambdaQueryWrapper<AssessmentOption>().in(AssessmentOption::getItemId, itemIds));
    }
}
