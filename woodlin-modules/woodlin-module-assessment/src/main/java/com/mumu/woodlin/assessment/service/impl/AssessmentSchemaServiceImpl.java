package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.assessment.mapper.*;
import com.mumu.woodlin.assessment.model.dto.schema.*;
import com.mumu.woodlin.assessment.model.entity.*;
import com.mumu.woodlin.assessment.service.IAssessmentPublishValidationService;
import com.mumu.woodlin.assessment.service.IAssessmentSchemaService;
import com.mumu.woodlin.assessment.validation.ValidationReport;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.dsl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 测评 Schema 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentSchemaServiceImpl extends ServiceImpl<AssessmentSchemaMapper, AssessmentSchema>
    implements IAssessmentSchemaService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_COMPILED = "compiled";
    private static final String DEMOGRAPHIC_SECTION_CODE = "DEMOGRAPHIC";
    private static final String MAIN_SECTION_CODE = "MAIN";

    private final AssessmentSchemaMapper schemaMapper;
    private final AssessmentFormVersionMapper versionMapper;
    private final AssessmentFormMapper formMapper;
    private final AssessmentSectionMapper sectionMapper;
    private final AssessmentItemMapper itemMapper;
    private final AssessmentOptionMapper optionMapper;
    private final AssessmentDimensionMapper dimensionMapper;
    private final AssessmentDimensionItemMapper dimensionItemMapper;
    private final AssessmentRuleMapper ruleMapper;
    private final IAssessmentPublishValidationService publishValidationService;
    private final ObjectMapper objectMapper;

    private static int defaultInteger(Integer value) {
        return value != null ? value : 0;
    }

    private static String defaultText(String value) {
        return value != null ? value : "";
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    private static String questionTypeToString(QuestionType type) {
        return switch (type) {
            case MULTIPLE_CHOICE -> "multiple_choice";
            case TEXT -> "text";
            case TEXTAREA -> "textarea";
            case NUMBER -> "number";
            case DATE -> "date";
            case RATING -> "rating";
            case MATRIX_SINGLE -> "matrix_single";
            case MATRIX_MULTIPLE -> "matrix_multiple";
            case FILL_BLANK -> "fill_blank";
            case SORT -> "sort";
            case SLIDER -> "slider";
            case DEMOGRAPHIC -> "demographic";
            case STATEMENT -> "statement";
            default -> "single_choice";
        };
    }

    private static AssessmentType parseAssessmentType(String assessmentType) {
        if (!StringUtils.hasText(assessmentType)) {
            return AssessmentType.SURVEY;
        }
        return switch (assessmentType.toLowerCase()) {
            case "scale" -> AssessmentType.SCALE;
            case "exam" -> AssessmentType.EXAM;
            default -> AssessmentType.SURVEY;
        };
    }

    private static RandomStrategy parseRandomStrategy(String strategy) {
        if (!StringUtils.hasText(strategy)) {
            return RandomStrategy.NONE;
        }
        return switch (strategy.toLowerCase()) {
            case "random_items" -> RandomStrategy.RANDOM_ITEMS;
            case "random_options" -> RandomStrategy.RANDOM_OPTIONS;
            case "random_both" -> RandomStrategy.RANDOM_BOTH;
            default -> RandomStrategy.NONE;
        };
    }

    private static ScoreMode parseScoreMode(String scoreMode) {
        return Objects.requireNonNullElse(parseNullableScoreMode(scoreMode), ScoreMode.SUM);
    }

    private static ScoreMode parseNullableScoreMode(String scoreMode) {
        if (!StringUtils.hasText(scoreMode)) {
            return null;
        }
        return switch (scoreMode.toLowerCase()) {
            case "mean" -> ScoreMode.MEAN;
            case "max" -> ScoreMode.MAX;
            case "min" -> ScoreMode.MIN;
            case "weighted_sum" -> ScoreMode.WEIGHTED_SUM;
            case "custom_dsl" -> ScoreMode.CUSTOM_DSL;
            default -> ScoreMode.SUM;
        };
    }

    private static ReverseMode parseReverseMode(String reverseMode) {
        return Objects.requireNonNullElse(parseNullableReverseMode(reverseMode), ReverseMode.NONE);
    }

    private static ReverseMode parseNullableReverseMode(String reverseMode) {
        if (!StringUtils.hasText(reverseMode)) {
            return null;
        }
        return switch (reverseMode.toLowerCase()) {
            case "formula" -> ReverseMode.FORMULA;
            case "table" -> ReverseMode.TABLE;
            default -> ReverseMode.NONE;
        };
    }

    private static QuestionType parseQuestionType(String itemType) {
        if (!StringUtils.hasText(itemType)) {
            return QuestionType.SINGLE_CHOICE;
        }
        return switch (itemType.toLowerCase()) {
            case "multiple_choice" -> QuestionType.MULTIPLE_CHOICE;
            case "text" -> QuestionType.TEXT;
            case "textarea", "short_text" -> QuestionType.TEXTAREA;
            case "number" -> QuestionType.NUMBER;
            case "date" -> QuestionType.DATE;
            case "rating" -> QuestionType.RATING;
            case "matrix_single" -> QuestionType.MATRIX_SINGLE;
            case "matrix_multiple" -> QuestionType.MATRIX_MULTIPLE;
            case "fill_blank" -> QuestionType.FILL_BLANK;
            case "sort" -> QuestionType.SORT;
            case "slider" -> QuestionType.SLIDER;
            case "demographic" -> QuestionType.DEMOGRAPHIC;
            case "statement" -> QuestionType.STATEMENT;
            default -> QuestionType.SINGLE_CHOICE;
        };
    }

    @Override
    public AssessmentSchemaAggregateDTO getAggregate(Long versionId) {
        AssessmentFormVersion version = getVersionOrThrow(versionId);
        AssessmentSchema schema = findSchema(versionId, version.getSchemaId());
        if (schema != null && StringUtils.hasText(schema.getCanonicalSchema())) {
            AssessmentSchemaAggregateDTO aggregate = readAggregate(schema.getCanonicalSchema());
            return enrichAggregate(version, schema, aggregate);
        }
        AssessmentSchemaAggregateDTO aggregate = buildAggregateFromProjection(version);
        return enrichAggregate(version, schema, aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentSchemaAggregateDTO saveAggregate(Long versionId, AssessmentSchemaAggregateDTO aggregate) {
        AssessmentFormVersion version = getVersionOrThrow(versionId);
        assertMutableVersion(version);

        AssessmentSchemaAggregateDTO normalized = normalizeAggregate(version, aggregate);
        String canonicalSchema = writeJson(normalized);
        String dslSource = buildDslSource(normalized);
        String dslHash = sha256Hex(dslSource);

        AssessmentSchema schema = upsertSchema(version, canonicalSchema, dslSource, null, null, STATUS_DRAFT);
        updateVersionAfterSave(version, schema, STATUS_DRAFT, schema.getSchemaHash(), dslHash);

        normalized.setSchemaId(schema.getSchemaId())
            .setStatus(schema.getStatus())
            .setCompiledSchema(null)
            .setCompileError(null)
            .setSchemaHash(schema.getSchemaHash())
            .setDslHash(dslHash)
            .setDslSource(dslSource);
        return normalized;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentSchemaCompileResultDTO compileVersion(Long versionId) {
        AssessmentFormVersion version = getVersionOrThrow(versionId);
        AssessmentSchema schema = findOrCreateSchema(version);
        AssessmentSchemaAggregateDTO sourceAggregate = getAggregate(versionId);

        try {
            AssessmentSchemaAggregateDTO normalized = normalizeAggregate(version, sourceAggregate);
            String canonicalSchema = writeJson(normalized);
            String dslSource = buildDslSource(normalized);
            String dslHash = sha256Hex(dslSource);
            String compiledSchema = writeJson(normalized);

            rebuildProjection(version, normalized);

            schema = upsertSchema(version, canonicalSchema, dslSource, compiledSchema, null, STATUS_COMPILED);
            updateVersionAfterSave(version, schema, STATUS_COMPILED, schema.getSchemaHash(), dslHash);

            return new AssessmentSchemaCompileResultDTO()
                .setVersionId(versionId)
                .setSchemaId(schema.getSchemaId())
                .setStatus(schema.getStatus())
                .setSchemaHash(schema.getSchemaHash())
                .setDslHash(dslHash)
                .setCompiledSchema(compiledSchema)
                .setCompileError(null);
        } catch (Exception exception) {
            String compileError = extractErrorMessage(exception);
            log.warn("测评 Schema 编译失败, versionId={}, error={}", versionId, compileError, exception);
            schema = upsertSchema(version, null, null, null, compileError, STATUS_DRAFT);
            updateVersionAfterSave(version, schema, STATUS_DRAFT, schema.getSchemaHash(), version.getDslHash());
            return new AssessmentSchemaCompileResultDTO()
                .setVersionId(versionId)
                .setSchemaId(schema.getSchemaId())
                .setStatus(schema.getStatus())
                .setSchemaHash(schema.getSchemaHash())
                .setDslHash(version.getDslHash())
                .setCompileError(compileError);
        }
    }

    @Override
    public ValidationReport validateVersion(Long versionId) {
        compileVersion(versionId);
        return publishValidationService.validateVersion(versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentSchemaAggregateDTO importDsl(Long versionId, String dslSource) {
        AssessmentFormVersion version = getVersionOrThrow(versionId);
        assertMutableVersion(version);
        if (!StringUtils.hasText(dslSource)) {
            throw BusinessException.of("DSL 内容不能为空");
        }

        AssessmentSchemaAggregateDTO aggregate = parseDslSource(version, dslSource);
        aggregate.setDslSource(dslSource);
        return saveAggregate(versionId, aggregate);
    }

    @Override
    public String exportDsl(Long versionId) {
        AssessmentSchemaAggregateDTO aggregate = getAggregate(versionId);
        if (StringUtils.hasText(aggregate.getDslSource())) {
            return aggregate.getDslSource();
        }
        return buildDslSource(aggregate);
    }

    private AssessmentSchemaAggregateDTO enrichAggregate(
        AssessmentFormVersion version,
        AssessmentSchema schema,
        AssessmentSchemaAggregateDTO aggregate) {
        aggregate.setVersionId(version.getVersionId())
            .setFormId(version.getFormId())
            .setSchemaId(schema != null ? schema.getSchemaId() : version.getSchemaId())
            .setStatus(schema != null ? schema.getStatus() : version.getStatus())
            .setSchemaHash(schema != null ? schema.getSchemaHash() : version.getSchemaHash())
            .setDslHash(version.getDslHash())
            .setCompileError(schema != null ? schema.getCompileError() : null)
            .setCompiledSchema(schema != null ? schema.getCompiledSchema() : null)
            .setDslSource(schema != null && StringUtils.hasText(schema.getDslSource())
                ? schema.getDslSource()
                : aggregate.getDslSource());
        if (!StringUtils.hasText(aggregate.getAssessmentType())) {
            AssessmentForm form = formMapper.selectById(version.getFormId());
            aggregate.setAssessmentType(form != null ? form.getAssessmentType() : "survey");
            if (!StringUtils.hasText(aggregate.getDescription()) && form != null) {
                aggregate.setDescription(form.getDescription());
            }
        }
        return normalizeAggregate(version, aggregate);
    }

    private AssessmentSchemaAggregateDTO buildAggregateFromProjection(AssessmentFormVersion version) {
        AssessmentForm form = formMapper.selectById(version.getFormId());
        List<AssessmentSection> sections = sectionMapper.selectList(new LambdaQueryWrapper<AssessmentSection>()
            .eq(AssessmentSection::getVersionId, version.getVersionId())
            .orderByAsc(AssessmentSection::getSortOrder, AssessmentSection::getSectionId));
        List<AssessmentItem> items = itemMapper.selectList(new LambdaQueryWrapper<AssessmentItem>()
            .eq(AssessmentItem::getVersionId, version.getVersionId())
            .orderByAsc(AssessmentItem::getSortOrder, AssessmentItem::getItemId));
        List<Long> itemIds = items.stream().map(AssessmentItem::getItemId).toList();
        List<AssessmentOption> options = itemIds.isEmpty()
            ? List.of()
            : optionMapper.selectList(new LambdaQueryWrapper<AssessmentOption>()
            .in(AssessmentOption::getItemId, itemIds)
            .orderByAsc(AssessmentOption::getSortOrder, AssessmentOption::getOptionId));
        List<AssessmentDimension> dimensions = dimensionMapper.selectList(new LambdaQueryWrapper<AssessmentDimension>()
            .eq(AssessmentDimension::getVersionId, version.getVersionId())
            .orderByAsc(AssessmentDimension::getSortOrder, AssessmentDimension::getDimensionId));
        List<AssessmentDimensionItem> dimensionItems = dimensionItemMapper.selectList(new LambdaQueryWrapper<AssessmentDimensionItem>()
            .eq(AssessmentDimensionItem::getVersionId, version.getVersionId()));
        List<AssessmentRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<AssessmentRule>()
            .eq(AssessmentRule::getVersionId, version.getVersionId())
            .orderByAsc(AssessmentRule::getPriority, AssessmentRule::getRuleId));

        Map<Long, List<AssessmentItem>> itemsBySectionId = items.stream()
            .filter(item -> item.getSectionId() != null)
            .collect(Collectors.groupingBy(AssessmentItem::getSectionId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<AssessmentOption>> optionsByItemId = options.stream()
            .collect(Collectors.groupingBy(AssessmentOption::getItemId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<AssessmentDimensionItem>> dimensionItemsByItemId = dimensionItems.stream()
            .collect(Collectors.groupingBy(AssessmentDimensionItem::getItemId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, AssessmentDimension> dimensionById = dimensions.stream()
            .collect(Collectors.toMap(AssessmentDimension::getDimensionId, dimension -> dimension, (left, right) -> left));

        AssessmentSchemaAggregateDTO aggregate = new AssessmentSchemaAggregateDTO()
            .setFormId(version.getFormId())
            .setVersionId(version.getVersionId())
            .setStatus(version.getStatus())
            .setAssessmentType(form != null ? form.getAssessmentType() : "survey")
            .setDescription(form != null ? form.getDescription() : null);

        List<SchemaSectionDTO> sectionDTOS = new ArrayList<>();
        for (AssessmentSection section : sections) {
            SchemaSectionDTO sectionDTO = new SchemaSectionDTO()
                .setSectionCode(section.getSectionCode())
                .setSectionTitle(section.getSectionTitle())
                .setSectionDesc(section.getSectionDesc())
                .setDisplayMode(section.getDisplayMode())
                .setRandomStrategy(section.getRandomStrategy())
                .setSortOrder(section.getSortOrder())
                .setIsRequired(section.getIsRequired())
                .setAnchorCode(section.getAnchorCode());

            List<SchemaItemDTO> itemDTOS = new ArrayList<>();
            for (AssessmentItem item : itemsBySectionId.getOrDefault(section.getSectionId(), List.of())) {
                SchemaItemDTO itemDTO = new SchemaItemDTO()
                    .setItemCode(item.getItemCode())
                    .setItemType(item.getItemType())
                    .setStem(item.getStem())
                    .setStemMediaUrl(item.getStemMediaUrl())
                    .setHelpText(item.getHelpText())
                    .setSortOrder(item.getSortOrder())
                    .setIsRequired(item.getIsRequired())
                    .setIsScored(item.getIsScored())
                    .setIsAnchor(item.getIsAnchor())
                    .setIsReverse(item.getIsReverse())
                    .setIsDemographic(item.getIsDemographic())
                    .setMaxScore(item.getMaxScore())
                    .setMinScore(item.getMinScore())
                    .setTimeLimitSeconds(item.getTimeLimitSeconds())
                    .setDemographicField(item.getDemographicField());

                List<SchemaOptionDTO> optionDTOS = optionsByItemId.getOrDefault(item.getItemId(), List.of()).stream()
                    .map(option -> new SchemaOptionDTO()
                        .setOptionCode(option.getOptionCode())
                        .setDisplayText(option.getDisplayText())
                        .setMediaUrl(option.getMediaUrl())
                        .setRawValue(option.getRawValue())
                        .setScoreValue(option.getScoreValue())
                        .setScoreReverseValue(option.getScoreReverseValue())
                        .setIsExclusive(option.getIsExclusive())
                        .setIsCorrect(option.getIsCorrect())
                        .setSortOrder(option.getSortOrder()))
                    .collect(Collectors.toCollection(ArrayList::new));
                itemDTO.setOptions(optionDTOS);

                List<SchemaDimensionBindingDTO> bindings = new ArrayList<>();
                for (AssessmentDimensionItem dimensionItem : dimensionItemsByItemId.getOrDefault(item.getItemId(), List.of())) {
                    AssessmentDimension dimension = dimensionById.get(dimensionItem.getDimensionId());
                    if (dimension == null) {
                        continue;
                    }
                    bindings.add(new SchemaDimensionBindingDTO()
                        .setDimensionCode(dimension.getDimensionCode())
                        .setWeight(dimensionItem.getWeight())
                        .setScoreMode(dimensionItem.getScoreMode())
                        .setReverseMode(dimensionItem.getReverseMode()));
                }
                itemDTO.setDimensionBindings(bindings);
                itemDTOS.add(itemDTO);
            }
            sectionDTO.setItems(itemDTOS);
            sectionDTOS.add(sectionDTO);
        }

        Map<Long, String> dimensionCodeById = dimensions.stream()
            .collect(Collectors.toMap(AssessmentDimension::getDimensionId, AssessmentDimension::getDimensionCode, (left, right) -> left));
        aggregate.setSections(sectionDTOS)
            .setDimensions(dimensions.stream()
                .map(dimension -> new SchemaDimensionDTO()
                    .setDimensionCode(dimension.getDimensionCode())
                    .setDimensionName(dimension.getDimensionName())
                    .setDimensionDesc(dimension.getDimensionDesc())
                    .setParentDimensionCode(dimensionCodeById.get(dimension.getParentDimensionId()))
                    .setScoreMode(dimension.getScoreMode())
                    .setScoreDsl(dimension.getScoreDsl())
                    .setNormSetId(dimension.getNormSetId())
                    .setSortOrder(dimension.getSortOrder()))
                .collect(Collectors.toCollection(ArrayList::new)))
            .setRules(rules.stream()
                .map(rule -> new SchemaRuleDTO()
                    .setRuleCode(rule.getRuleCode())
                    .setRuleName(rule.getRuleName())
                    .setRuleType(rule.getRuleType())
                    .setTargetType(rule.getTargetType())
                    .setTargetCode(rule.getTargetCode())
                    .setDslSource(rule.getDslSource())
                    .setCompiledRule(rule.getCompiledRule())
                    .setPriority(rule.getPriority())
                    .setIsActive(rule.getIsActive()))
                .collect(Collectors.toCollection(ArrayList::new)));
        return normalizeAggregate(version, aggregate);
    }

    private AssessmentSchemaAggregateDTO normalizeAggregate(AssessmentFormVersion version, AssessmentSchemaAggregateDTO aggregate) {
        AssessmentSchemaAggregateDTO normalized = deepCopy(aggregate != null ? aggregate : new AssessmentSchemaAggregateDTO());
        normalized.setFormId(version.getFormId()).setVersionId(version.getVersionId());
        if (!StringUtils.hasText(normalized.getAssessmentType())) {
            AssessmentForm form = formMapper.selectById(version.getFormId());
            normalized.setAssessmentType(form != null && StringUtils.hasText(form.getAssessmentType())
                ? form.getAssessmentType()
                : "survey");
        }
        if (!StringUtils.hasText(normalized.getRandomStrategy())) {
            normalized.setRandomStrategy("none");
        }
        normalized.setSections(new ArrayList<>(CollectionUtils.isEmpty(normalized.getSections())
            ? List.of()
            : normalized.getSections()));
        normalized.setDimensions(new ArrayList<>(CollectionUtils.isEmpty(normalized.getDimensions())
            ? List.of()
            : normalized.getDimensions()));
        normalized.setRules(new ArrayList<>(CollectionUtils.isEmpty(normalized.getRules())
            ? List.of()
            : normalized.getRules()));

        List<SchemaSectionDTO> sourceSections = normalized.getSections().stream()
            .sorted(Comparator.comparing(section -> defaultInteger(section.getSortOrder())))
            .collect(Collectors.toCollection(ArrayList::new));
        SchemaSectionDTO demographicSection = null;
        List<SchemaSectionDTO> regularSections = new ArrayList<>();
        List<SchemaItemDTO> demographicItems = new ArrayList<>();
        AtomicInteger sectionCodeSequence = new AtomicInteger(1);
        AtomicInteger itemCodeSequence = new AtomicInteger(1);

        for (SchemaSectionDTO sourceSection : sourceSections) {
            SchemaSectionDTO section = sourceSection != null ? sourceSection : new SchemaSectionDTO();
            section.setItems(new ArrayList<>(CollectionUtils.isEmpty(section.getItems()) ? List.of() : section.getItems()));
            if (DEMOGRAPHIC_SECTION_CODE.equalsIgnoreCase(section.getSectionCode())) {
                demographicSection = section;
            } else {
                if (!StringUtils.hasText(section.getSectionCode())) {
                    section.setSectionCode("SEC_" + String.format("%03d", sectionCodeSequence.getAndIncrement()));
                }
                regularSections.add(section);
            }
        }
        if (demographicSection == null) {
            demographicSection = new SchemaSectionDTO()
                .setSectionCode(DEMOGRAPHIC_SECTION_CODE)
                .setSectionTitle("人口学前置")
                .setDisplayMode("paged")
                .setRandomStrategy("none")
                .setIsRequired(Boolean.TRUE)
                .setItems(new ArrayList<>());
        }
        if (!StringUtils.hasText(demographicSection.getSectionTitle())) {
            demographicSection.setSectionTitle("人口学前置");
        }

        for (SchemaSectionDTO section : regularSections) {
            List<SchemaItemDTO> regularItems = new ArrayList<>();
            for (SchemaItemDTO sourceItem : section.getItems()) {
                SchemaItemDTO item = sourceItem != null ? sourceItem : new SchemaItemDTO();
                if (!StringUtils.hasText(item.getItemCode())) {
                    item.setItemCode("Q" + String.format("%03d", itemCodeSequence.getAndIncrement()));
                }
                if (Boolean.TRUE.equals(item.getIsDemographic())
                    || StringUtils.hasText(item.getDemographicField())) {
                    demographicItems.add(normalizeDemographicItem(item));
                } else {
                    regularItems.add(normalizeFormalItem(item));
                }
            }
            section.setItems(regularItems);
        }

        List<SchemaItemDTO> demographicSectionItems = new ArrayList<>();
        if (!CollectionUtils.isEmpty(demographicSection.getItems())) {
            for (SchemaItemDTO sourceItem : demographicSection.getItems()) {
                SchemaItemDTO item = sourceItem != null ? sourceItem : new SchemaItemDTO();
                if (!StringUtils.hasText(item.getItemCode())) {
                    item.setItemCode("D" + String.format("%03d", itemCodeSequence.getAndIncrement()));
                }
                demographicSectionItems.add(normalizeDemographicItem(item));
            }
        }
        demographicSectionItems.addAll(demographicItems);

        demographicSection.setSectionCode(DEMOGRAPHIC_SECTION_CODE)
            .setDisplayMode(StringUtils.hasText(demographicSection.getDisplayMode()) ? demographicSection.getDisplayMode() : "paged")
            .setRandomStrategy("none")
            .setItems(demographicSectionItems);

        List<SchemaSectionDTO> normalizedSections = new ArrayList<>();
        normalizedSections.add(demographicSection);
        normalizedSections.addAll(regularSections.stream()
            .map(this::normalizeSectionDefaults)
            .collect(Collectors.toList()));
        for (int sectionIndex = 0; sectionIndex < normalizedSections.size(); sectionIndex++) {
            SchemaSectionDTO section = normalizeSectionDefaults(normalizedSections.get(sectionIndex));
            section.setSortOrder(sectionIndex + 1);
            for (int itemIndex = 0; itemIndex < section.getItems().size(); itemIndex++) {
                SchemaItemDTO item = section.getItems().get(itemIndex);
                item.setSortOrder(itemIndex + 1);
                item.setOptions(new ArrayList<>(CollectionUtils.isEmpty(item.getOptions()) ? List.of() : item.getOptions()));
                item.setDimensionBindings(new ArrayList<>(CollectionUtils.isEmpty(item.getDimensionBindings())
                    ? List.of()
                    : item.getDimensionBindings()));
                for (int optionIndex = 0; optionIndex < item.getOptions().size(); optionIndex++) {
                    SchemaOptionDTO option = item.getOptions().get(optionIndex);
                    option.setSortOrder(optionIndex + 1);
                }
            }
            normalizedSections.set(sectionIndex, section);
        }
        normalized.setSections(normalizedSections);

        for (int index = 0; index < normalized.getDimensions().size(); index++) {
            SchemaDimensionDTO dimension = normalized.getDimensions().get(index);
            if (!StringUtils.hasText(dimension.getDimensionCode())) {
                throw BusinessException.of("维度编码不能为空");
            }
            dimension.setSortOrder(index + 1);
            if (!StringUtils.hasText(dimension.getScoreMode())) {
                dimension.setScoreMode("sum");
            }
        }
        for (int index = 0; index < normalized.getRules().size(); index++) {
            SchemaRuleDTO rule = normalized.getRules().get(index);
            if (!StringUtils.hasText(rule.getRuleCode())) {
                rule.setRuleCode("RULE_" + String.format("%03d", index + 1));
            }
            if (rule.getPriority() == null) {
                rule.setPriority(index + 1);
            }
            if (rule.getIsActive() == null) {
                rule.setIsActive(Boolean.TRUE);
            }
        }

        validateUniqueness(normalized);
        validateDimensionBindings(normalized);
        return normalized;
    }

    private SchemaSectionDTO normalizeSectionDefaults(SchemaSectionDTO section) {
        if (!StringUtils.hasText(section.getDisplayMode())) {
            section.setDisplayMode("paged");
        }
        if (!StringUtils.hasText(section.getRandomStrategy())) {
            section.setRandomStrategy("none");
        }
        if (section.getIsRequired() == null) {
            section.setIsRequired(Boolean.FALSE);
        }
        section.setItems(new ArrayList<>(CollectionUtils.isEmpty(section.getItems()) ? List.of() : section.getItems()));
        return section;
    }

    private SchemaItemDTO normalizeDemographicItem(SchemaItemDTO item) {
        item.setIsDemographic(Boolean.TRUE);
        item.setIsScored(Boolean.FALSE);
        item.setIsAnchor(Boolean.FALSE);
        item.setDimensionBindings(new ArrayList<>());
        return item;
    }

    private SchemaItemDTO normalizeFormalItem(SchemaItemDTO item) {
        if (item.getIsDemographic() == null) {
            item.setIsDemographic(Boolean.FALSE);
        }
        if (item.getIsScored() == null) {
            item.setIsScored(Boolean.TRUE);
        }
        if (item.getIsAnchor() == null) {
            item.setIsAnchor(Boolean.FALSE);
        }
        if (item.getIsReverse() == null) {
            item.setIsReverse(Boolean.FALSE);
        }
        item.setOptions(new ArrayList<>(CollectionUtils.isEmpty(item.getOptions()) ? List.of() : item.getOptions()));
        item.setDimensionBindings(new ArrayList<>(CollectionUtils.isEmpty(item.getDimensionBindings())
            ? List.of()
            : item.getDimensionBindings()));
        return item;
    }

    private void validateUniqueness(AssessmentSchemaAggregateDTO aggregate) {
        assertUnique("章节编码", aggregate.getSections().stream().map(SchemaSectionDTO::getSectionCode).toList());
        assertUnique("维度编码", aggregate.getDimensions().stream().map(SchemaDimensionDTO::getDimensionCode).toList());
        assertUnique("规则编码", aggregate.getRules().stream().map(SchemaRuleDTO::getRuleCode).toList());

        List<String> itemCodes = aggregate.getSections().stream()
            .flatMap(section -> section.getItems().stream())
            .map(SchemaItemDTO::getItemCode)
            .toList();
        assertUnique("题目编码", itemCodes);

        for (SchemaItemDTO item : aggregate.getSections().stream()
            .flatMap(section -> section.getItems().stream())
            .toList()) {
            assertUnique("选项编码(" + item.getItemCode() + ")",
                item.getOptions().stream().map(SchemaOptionDTO::getOptionCode).toList());
        }
    }

    private void validateDimensionBindings(AssessmentSchemaAggregateDTO aggregate) {
        Set<String> dimensionCodes = aggregate.getDimensions().stream()
            .map(SchemaDimensionDTO::getDimensionCode)
            .collect(Collectors.toSet());
        for (SchemaSectionDTO section : aggregate.getSections()) {
            for (SchemaItemDTO item : section.getItems()) {
                if (Boolean.TRUE.equals(item.getIsDemographic()) && !CollectionUtils.isEmpty(item.getDimensionBindings())) {
                    throw BusinessException.of("人口学题不能配置维度映射: " + item.getItemCode());
                }
                for (SchemaDimensionBindingDTO binding : item.getDimensionBindings()) {
                    if (!StringUtils.hasText(binding.getDimensionCode())) {
                        throw BusinessException.of("题目 " + item.getItemCode() + " 存在空维度映射");
                    }
                    if (!dimensionCodes.contains(binding.getDimensionCode())) {
                        throw BusinessException.of("题目 " + item.getItemCode() + " 引用了不存在的维度: " + binding.getDimensionCode());
                    }
                    if (binding.getWeight() == null) {
                        binding.setWeight(BigDecimal.ONE);
                    }
                }
            }
        }
    }

    private void rebuildProjection(AssessmentFormVersion version, AssessmentSchemaAggregateDTO aggregate) {
        clearProjection(version.getVersionId());

        Map<String, Long> dimensionIdByCode = new LinkedHashMap<>();
        Map<String, AssessmentDimension> dimensionEntityByCode = new LinkedHashMap<>();
        for (SchemaDimensionDTO dimensionDTO : aggregate.getDimensions()) {
            AssessmentDimension dimension = new AssessmentDimension()
                .setFormId(version.getFormId())
                .setVersionId(version.getVersionId())
                .setDimensionCode(dimensionDTO.getDimensionCode())
                .setDimensionName(dimensionDTO.getDimensionName())
                .setDimensionDesc(dimensionDTO.getDimensionDesc())
                .setScoreMode(dimensionDTO.getScoreMode())
                .setScoreDsl(dimensionDTO.getScoreDsl())
                .setNormSetId(dimensionDTO.getNormSetId())
                .setSortOrder(dimensionDTO.getSortOrder())
                .setTenantId(version.getTenantId());
            dimensionMapper.insert(dimension);
            dimensionIdByCode.put(dimensionDTO.getDimensionCode(), dimension.getDimensionId());
            dimensionEntityByCode.put(dimensionDTO.getDimensionCode(), dimension);
        }
        for (SchemaDimensionDTO dimensionDTO : aggregate.getDimensions()) {
            if (!StringUtils.hasText(dimensionDTO.getParentDimensionCode())) {
                continue;
            }
            AssessmentDimension dimension = dimensionEntityByCode.get(dimensionDTO.getDimensionCode());
            Long parentDimensionId = dimensionIdByCode.get(dimensionDTO.getParentDimensionCode());
            if (dimension != null && parentDimensionId != null) {
                AssessmentDimension update = new AssessmentDimension()
                    .setDimensionId(dimension.getDimensionId())
                    .setParentDimensionId(parentDimensionId);
                dimensionMapper.updateById(update);
            }
        }

        for (SchemaSectionDTO sectionDTO : aggregate.getSections()) {
            AssessmentSection section = new AssessmentSection()
                .setVersionId(version.getVersionId())
                .setFormId(version.getFormId())
                .setSectionCode(sectionDTO.getSectionCode())
                .setSectionTitle(sectionDTO.getSectionTitle())
                .setSectionDesc(sectionDTO.getSectionDesc())
                .setDisplayMode(sectionDTO.getDisplayMode())
                .setRandomStrategy(sectionDTO.getRandomStrategy())
                .setSortOrder(sectionDTO.getSortOrder())
                .setIsRequired(sectionDTO.getIsRequired())
                .setAnchorCode(sectionDTO.getAnchorCode())
                .setTenantId(version.getTenantId());
            sectionMapper.insert(section);

            for (SchemaItemDTO itemDTO : sectionDTO.getItems()) {
                AssessmentItem item = new AssessmentItem()
                    .setVersionId(version.getVersionId())
                    .setSectionId(section.getSectionId())
                    .setFormId(version.getFormId())
                    .setItemCode(itemDTO.getItemCode())
                    .setItemType(itemDTO.getItemType())
                    .setStem(itemDTO.getStem())
                    .setStemMediaUrl(itemDTO.getStemMediaUrl())
                    .setHelpText(itemDTO.getHelpText())
                    .setSortOrder(itemDTO.getSortOrder())
                    .setIsRequired(itemDTO.getIsRequired())
                    .setIsScored(itemDTO.getIsScored())
                    .setIsAnchor(itemDTO.getIsAnchor())
                    .setIsReverse(itemDTO.getIsReverse())
                    .setIsDemographic(itemDTO.getIsDemographic())
                    .setMaxScore(itemDTO.getMaxScore())
                    .setMinScore(itemDTO.getMinScore())
                    .setTimeLimitSeconds(itemDTO.getTimeLimitSeconds())
                    .setDemographicField(itemDTO.getDemographicField())
                    .setTenantId(version.getTenantId());
                itemMapper.insert(item);

                for (SchemaOptionDTO optionDTO : itemDTO.getOptions()) {
                    AssessmentOption option = new AssessmentOption()
                        .setItemId(item.getItemId())
                        .setOptionCode(optionDTO.getOptionCode())
                        .setDisplayText(optionDTO.getDisplayText())
                        .setMediaUrl(optionDTO.getMediaUrl())
                        .setRawValue(optionDTO.getRawValue())
                        .setScoreValue(optionDTO.getScoreValue())
                        .setScoreReverseValue(optionDTO.getScoreReverseValue())
                        .setIsExclusive(optionDTO.getIsExclusive())
                        .setIsCorrect(optionDTO.getIsCorrect())
                        .setSortOrder(optionDTO.getSortOrder())
                        .setTenantId(version.getTenantId());
                    optionMapper.insert(option);
                }

                for (SchemaDimensionBindingDTO binding : itemDTO.getDimensionBindings()) {
                    Long dimensionId = dimensionIdByCode.get(binding.getDimensionCode());
                    if (dimensionId == null) {
                        throw BusinessException.of("维度映射不存在: " + binding.getDimensionCode());
                    }
                    AssessmentDimensionItem dimensionItem = new AssessmentDimensionItem()
                        .setDimensionId(dimensionId)
                        .setItemId(item.getItemId())
                        .setVersionId(version.getVersionId())
                        .setWeight(binding.getWeight())
                        .setScoreMode(binding.getScoreMode())
                        .setReverseMode(binding.getReverseMode())
                        .setTenantId(version.getTenantId());
                    dimensionItemMapper.insert(dimensionItem);
                }
            }
        }

        for (SchemaRuleDTO ruleDTO : aggregate.getRules()) {
            AssessmentRule rule = new AssessmentRule()
                .setFormId(version.getFormId())
                .setVersionId(version.getVersionId())
                .setRuleCode(ruleDTO.getRuleCode())
                .setRuleName(ruleDTO.getRuleName())
                .setRuleType(ruleDTO.getRuleType())
                .setTargetType(ruleDTO.getTargetType())
                .setTargetCode(ruleDTO.getTargetCode())
                .setDslSource(ruleDTO.getDslSource())
                .setCompiledRule(ruleDTO.getCompiledRule())
                .setPriority(ruleDTO.getPriority())
                .setIsActive(ruleDTO.getIsActive())
                .setCompileError(null)
                .setTenantId(version.getTenantId());
            ruleMapper.insert(rule);
        }
    }

    private void clearProjection(Long versionId) {
        List<Long> itemIds = itemMapper.selectList(new LambdaQueryWrapper<AssessmentItem>()
                .eq(AssessmentItem::getVersionId, versionId))
            .stream()
            .map(AssessmentItem::getItemId)
            .toList();
        if (!itemIds.isEmpty()) {
            optionMapper.delete(new LambdaQueryWrapper<AssessmentOption>()
                .in(AssessmentOption::getItemId, itemIds));
        }
        dimensionItemMapper.delete(new LambdaQueryWrapper<AssessmentDimensionItem>()
            .eq(AssessmentDimensionItem::getVersionId, versionId));
        itemMapper.delete(new LambdaQueryWrapper<AssessmentItem>()
            .eq(AssessmentItem::getVersionId, versionId));
        sectionMapper.delete(new LambdaQueryWrapper<AssessmentSection>()
            .eq(AssessmentSection::getVersionId, versionId));
        dimensionMapper.delete(new LambdaQueryWrapper<AssessmentDimension>()
            .eq(AssessmentDimension::getVersionId, versionId));
        ruleMapper.delete(new LambdaQueryWrapper<AssessmentRule>()
            .eq(AssessmentRule::getVersionId, versionId));
    }

    private AssessmentSchema upsertSchema(
        AssessmentFormVersion version,
        String canonicalSchema,
        String dslSource,
        String compiledSchema,
        String compileError,
        String status) {
        AssessmentSchema schema = findOrCreateSchema(version);
        if (canonicalSchema != null) {
            schema.setCanonicalSchema(canonicalSchema);
            schema.setSchemaHash(sha256Hex(canonicalSchema));
        }
        if (dslSource != null) {
            schema.setDslSource(dslSource);
        }
        schema.setCompiledSchema(compiledSchema);
        schema.setCompileError(compileError);
        schema.setStatus(status);
        schema.setFormId(version.getFormId());
        schema.setVersionId(version.getVersionId());
        schema.setTenantId(version.getTenantId());
        if (schema.getSchemaId() == null) {
            schemaMapper.insert(schema);
        } else {
            schemaMapper.updateById(schema);
        }
        return schemaMapper.selectById(schema.getSchemaId());
    }

    private void updateVersionAfterSave(
        AssessmentFormVersion version,
        AssessmentSchema schema,
        String status,
        String schemaHash,
        String dslHash) {
        AssessmentFormVersion update = new AssessmentFormVersion()
            .setVersionId(version.getVersionId())
            .setSchemaId(schema.getSchemaId())
            .setSchemaHash(schemaHash)
            .setDslHash(dslHash)
            .setStatus(status);
        versionMapper.updateById(update);
    }

    private AssessmentSchema findOrCreateSchema(AssessmentFormVersion version) {
        AssessmentSchema schema = findSchema(version.getVersionId(), version.getSchemaId());
        if (schema != null) {
            return schema;
        }
        return new AssessmentSchema()
            .setFormId(version.getFormId())
            .setVersionId(version.getVersionId())
            .setStatus(STATUS_DRAFT)
            .setTenantId(version.getTenantId());
    }

    private AssessmentSchema findSchema(Long versionId, Long schemaId) {
        if (schemaId != null) {
            AssessmentSchema schema = schemaMapper.selectById(schemaId);
            if (schema != null) {
                return schema;
            }
        }
        return schemaMapper.selectOne(new LambdaQueryWrapper<AssessmentSchema>()
            .eq(AssessmentSchema::getVersionId, versionId)
            .orderByDesc(AssessmentSchema::getCreateTime, AssessmentSchema::getSchemaId)
            .last("limit 1"));
    }

    private AssessmentFormVersion getVersionOrThrow(Long versionId) {
        AssessmentFormVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw BusinessException.of("版本不存在: " + versionId);
        }
        return version;
    }

    private void assertMutableVersion(AssessmentFormVersion version) {
        if ("published".equalsIgnoreCase(version.getStatus())) {
            throw BusinessException.of("已发布版本不允许直接修改结构，请创建新版本");
        }
    }

    private AssessmentSchemaAggregateDTO parseDslSource(AssessmentFormVersion version, String dslSource) {
        try {
            JsonNode root = objectMapper.readTree(dslSource);
            if (root.has("sections") && root.path("sections").isArray()
                && root.path("sections").size() > 0
                && root.path("sections").get(0).has("questions")) {
                Questionnaire questionnaire = new QuestionnaireSerializer().fromJson(dslSource);
                return questionnaireToAggregate(version, questionnaire);
            }
            if (root.has("sections")) {
                AssessmentSchemaAggregateDTO aggregate = objectMapper.treeToValue(root, AssessmentSchemaAggregateDTO.class);
                aggregate.setVersionId(version.getVersionId()).setFormId(version.getFormId());
                return aggregate;
            }
        } catch (Exception ignored) {
            log.debug("DSL 导入内容不是 JSON，尝试兜底失败", ignored);
        }
        throw BusinessException.of("当前仅支持导入结构化 JSON DSL 或导出的 DSL JSON");
    }

    private AssessmentSchemaAggregateDTO questionnaireToAggregate(AssessmentFormVersion version, Questionnaire questionnaire) {
        AssessmentSchemaAggregateDTO aggregate = new AssessmentSchemaAggregateDTO()
            .setFormId(version.getFormId())
            .setVersionId(version.getVersionId())
            .setAssessmentType(questionnaire.getAssessmentType().name().toLowerCase())
            .setRandomStrategy(questionnaire.getRandomStrategy().name().toLowerCase())
            .setDescription(questionnaire.getDescription());

        List<SchemaDimensionDTO> dimensions = questionnaire.getDimensions().stream()
            .map(dimension -> new SchemaDimensionDTO()
                .setDimensionCode(dimension.getCode())
                .setDimensionName(dimension.getName())
                .setDimensionDesc(dimension.getDescription())
                .setParentDimensionCode(dimension.getParentCode())
                .setScoreMode(dimension.getScoreMode().name().toLowerCase())
                .setScoreDsl(dimension.getScoreDsl())
                .setSortOrder(dimension.getSortOrder()))
            .collect(Collectors.toCollection(ArrayList::new));
        aggregate.setDimensions(dimensions);

        AtomicInteger sectionSequence = new AtomicInteger(1);
        List<SchemaSectionDTO> sections = questionnaire.getSections().stream()
            .map(section -> {
                boolean demographicOnly = section.getQuestions().stream().allMatch(Question::isDemographic);
                String sectionCode = demographicOnly ? DEMOGRAPHIC_SECTION_CODE : "SEC_" + String.format("%03d", sectionSequence.getAndIncrement());
                SchemaSectionDTO sectionDTO = new SchemaSectionDTO()
                    .setSectionCode(sectionCode)
                    .setSectionTitle(section.getTitle())
                    .setSectionDesc(section.getDescription())
                    .setDisplayMode("paged")
                    .setRandomStrategy("none")
                    .setSortOrder(section.getOrder())
                    .setIsRequired(Boolean.TRUE);
                List<SchemaItemDTO> items = section.getQuestions().stream()
                    .map(this::questionToItem)
                    .collect(Collectors.toCollection(ArrayList::new));
                sectionDTO.setItems(items);
                return sectionDTO;
            })
            .collect(Collectors.toCollection(ArrayList::new));
        aggregate.setSections(sections);

        List<SchemaRuleDTO> rules = new ArrayList<>();
        AtomicInteger ruleSequence = new AtomicInteger(1);
        questionnaire.getRules().forEach(rule -> rules.add(new SchemaRuleDTO()
            .setRuleCode("RULE_" + String.format("%03d", ruleSequence.getAndIncrement()))
            .setRuleName(StringUtils.hasText(rule.getName()) ? rule.getName() : "全局校验规则")
            .setRuleType("validation")
            .setTargetType("form")
            .setTargetCode("FORM")
            .setDslSource(rule.getExpression())
            .setPriority(ruleSequence.get())
            .setIsActive(Boolean.TRUE)));
        aggregate.setRules(rules);
        return aggregate;
    }

    private SchemaItemDTO questionToItem(Question question) {
        SchemaItemDTO item = new SchemaItemDTO()
            .setItemCode(question.getId())
            .setItemType(questionTypeToString(question.getType()))
            .setStem(question.getTitle())
            .setHelpText(question.getDescription())
            .setIsRequired(question.getRequired())
            .setIsScored(question.isScored())
            .setIsAnchor(question.isAnchor())
            .setIsReverse(question.isReverse())
            .setIsDemographic(question.isDemographic())
            .setTimeLimitSeconds(question.getTimeLimitSeconds())
            .setDemographicField(question.getDemographicField())
            .setMaxScore(toBigDecimal(question.getMaxScore()))
            .setMinScore(toBigDecimal(question.getMinScore()));

        List<SchemaOptionDTO> options = question.getOptions().stream()
            .map(option -> new SchemaOptionDTO()
                .setOptionCode(option.getValue())
                .setDisplayText(option.getText())
                .setRawValue(option.getValue())
                .setScoreValue(toBigDecimal(option.getScoreValue()))
                .setScoreReverseValue(toBigDecimal(option.getScoreReverseValue()))
                .setIsExclusive(option.getExclusive())
                .setIsCorrect(option.isCorrect()))
            .collect(Collectors.toCollection(ArrayList::new));
        item.setOptions(options);

        List<SchemaDimensionBindingDTO> bindings = new ArrayList<>();
        if (!CollectionUtils.isEmpty(question.getDimensionBindings())) {
            question.getDimensionBindings().forEach(binding -> bindings.add(new SchemaDimensionBindingDTO()
                .setDimensionCode(binding.getDimensionCode())
                .setWeight(toBigDecimal(binding.getWeight()))
                .setScoreMode(binding.getScoreModeOverride() != null
                    ? binding.getScoreModeOverride().name().toLowerCase()
                    : null)
                .setReverseMode(binding.getReverseModeOverride() != null
                    ? binding.getReverseModeOverride().name().toLowerCase()
                    : null)));
        } else if (StringUtils.hasText(question.getDimensionCode())) {
            bindings.add(new SchemaDimensionBindingDTO()
                .setDimensionCode(question.getDimensionCode())
                .setWeight(toBigDecimal(question.getItemWeight()))
                .setReverseMode(question.getReverseMode().name().toLowerCase()));
        }
        item.setDimensionBindings(bindings);
        return item;
    }

    private String buildDslSource(AssessmentSchemaAggregateDTO aggregate) {
        Questionnaire questionnaire = aggregateToQuestionnaire(aggregate);
        return new QuestionnaireSerializer().toJson(questionnaire);
    }

    private Questionnaire aggregateToQuestionnaire(AssessmentSchemaAggregateDTO aggregate) {
        Questionnaire questionnaire = new Questionnaire(
            String.valueOf(aggregate.getFormId() != null ? aggregate.getFormId() : aggregate.getVersionId()),
            "assessment-" + aggregate.getVersionId());
        questionnaire.setDescription(aggregate.getDescription() != null ? aggregate.getDescription() : "");
        questionnaire.setAssessmentType(parseAssessmentType(aggregate.getAssessmentType()));
        questionnaire.setRandomStrategy(parseRandomStrategy(aggregate.getRandomStrategy()));

        for (SchemaDimensionDTO dimensionDTO : aggregate.getDimensions()) {
            Dimension dimension = new Dimension(dimensionDTO.getDimensionCode(), dimensionDTO.getDimensionName());
            dimension.setDescription(defaultText(dimensionDTO.getDimensionDesc()));
            dimension.setParentCode(dimensionDTO.getParentDimensionCode());
            dimension.setScoreMode(parseScoreMode(dimensionDTO.getScoreMode()));
            dimension.setScoreDsl(dimensionDTO.getScoreDsl());
            dimension.setSortOrder(defaultInteger(dimensionDTO.getSortOrder()));
            questionnaire.getDimensions().add(dimension);
        }

        for (SchemaRuleDTO ruleDTO : aggregate.getRules()) {
            if (!"validation".equalsIgnoreCase(ruleDTO.getRuleType()) || !StringUtils.hasText(ruleDTO.getDslSource())) {
                continue;
            }
            com.mumu.woodlin.dsl.ValidationRule validationRule = new com.mumu.woodlin.dsl.ValidationRule();
            validationRule.setName(defaultText(ruleDTO.getRuleName()));
            validationRule.setMessage(defaultText(ruleDTO.getRuleName()));
            validationRule.setExpression(ruleDTO.getDslSource());
            questionnaire.getRules().add(validationRule);
        }

        for (SchemaSectionDTO sectionDTO : aggregate.getSections()) {
            Section section = new Section(sectionDTO.getSectionTitle());
            section.setDescription(defaultText(sectionDTO.getSectionDesc()));
            section.setOrder(defaultInteger(sectionDTO.getSortOrder()));
            for (SchemaItemDTO itemDTO : sectionDTO.getItems()) {
                Question question = new Question();
                question.setId(itemDTO.getItemCode());
                question.setType(parseQuestionType(itemDTO.getItemType()));
                question.setTitle(defaultText(itemDTO.getStem()));
                question.setDescription(defaultText(itemDTO.getHelpText()));
                question.setRequired(Boolean.TRUE.equals(itemDTO.getIsRequired()));
                question.setScored(Boolean.TRUE.equals(itemDTO.getIsScored()));
                question.setAnchor(Boolean.TRUE.equals(itemDTO.getIsAnchor()));
                question.setReverse(Boolean.TRUE.equals(itemDTO.getIsReverse()));
                question.setReverseMode(parseReverseMode(resolveQuestionReverseMode(itemDTO)));
                question.setMaxScore(itemDTO.getMaxScore() != null ? itemDTO.getMaxScore().doubleValue() : null);
                question.setMinScore(itemDTO.getMinScore() != null ? itemDTO.getMinScore().doubleValue() : null);
                question.setTimeLimitSeconds(defaultInteger(itemDTO.getTimeLimitSeconds()));
                question.setDemographic(Boolean.TRUE.equals(itemDTO.getIsDemographic()));
                question.setDemographicField(itemDTO.getDemographicField());

                if (!CollectionUtils.isEmpty(itemDTO.getDimensionBindings())) {
                    itemDTO.getDimensionBindings().forEach(binding -> {
                        com.mumu.woodlin.dsl.DimensionBinding dimensionBinding =
                            new com.mumu.woodlin.dsl.DimensionBinding(binding.getDimensionCode());
                        dimensionBinding.setWeight(binding.getWeight() != null ? binding.getWeight().doubleValue() : 1.0);
                        dimensionBinding.setScoreModeOverride(parseNullableScoreMode(binding.getScoreMode()));
                        dimensionBinding.setReverseModeOverride(parseNullableReverseMode(binding.getReverseMode()));
                        question.getDimensionBindings().add(dimensionBinding);
                    });
                } else if (Boolean.TRUE.equals(itemDTO.getIsScored()) && !Boolean.TRUE.equals(itemDTO.getIsDemographic())) {
                    question.setDimensionCode(null);
                }

                if (!CollectionUtils.isEmpty(itemDTO.getDimensionBindings())) {
                    SchemaDimensionBindingDTO firstBinding = itemDTO.getDimensionBindings().get(0);
                    question.setDimensionCode(firstBinding.getDimensionCode());
                    question.setItemWeight(firstBinding.getWeight() != null ? firstBinding.getWeight().doubleValue() : 1.0);
                }

                for (SchemaOptionDTO optionDTO : itemDTO.getOptions()) {
                    Option option = new Option(
                        defaultText(optionDTO.getDisplayText()),
                        optionDTO.getOptionCode(),
                        defaultInteger(optionDTO.getSortOrder()),
                        Boolean.TRUE.equals(optionDTO.getIsExclusive()),
                        optionDTO.getScoreValue() != null ? optionDTO.getScoreValue().doubleValue() : null,
                        optionDTO.getScoreReverseValue() != null ? optionDTO.getScoreReverseValue().doubleValue() : null,
                        Boolean.TRUE.equals(optionDTO.getIsCorrect()));
                    question.getOptions().add(option);
                }
                section.getQuestions().add(question);
            }
            questionnaire.getSections().add(section);
        }
        return questionnaire;
    }

    private String resolveQuestionReverseMode(SchemaItemDTO itemDTO) {
        if (!CollectionUtils.isEmpty(itemDTO.getDimensionBindings())) {
            for (SchemaDimensionBindingDTO binding : itemDTO.getDimensionBindings()) {
                if (StringUtils.hasText(binding.getReverseMode())) {
                    return binding.getReverseMode();
                }
            }
        }
        return Boolean.TRUE.equals(itemDTO.getIsReverse()) ? "formula" : "none";
    }

    private AssessmentSchemaAggregateDTO readAggregate(String canonicalSchema) {
        try {
            return objectMapper.readValue(canonicalSchema, AssessmentSchemaAggregateDTO.class);
        } catch (JsonProcessingException exception) {
            log.warn("canonical_schema 解析失败，尝试按 DSL JSON 导入");
            AssessmentFormVersion fakeVersion = new AssessmentFormVersion();
            fakeVersion.setVersionId(-1L);
            fakeVersion.setFormId(-1L);
            return parseDslSource(fakeVersion, canonicalSchema);
        }
    }

    private AssessmentSchemaAggregateDTO deepCopy(AssessmentSchemaAggregateDTO aggregate) {
        return objectMapper.convertValue(aggregate, AssessmentSchemaAggregateDTO.class);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw BusinessException.of("Schema JSON 序列化失败", exception);
        }
    }

    private void assertUnique(String fieldName, List<String> values) {
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                throw BusinessException.of(fieldName + "不能为空");
            }
            if (!seen.add(value)) {
                throw BusinessException.of(fieldName + "重复: " + value);
            }
        }
    }

    private String extractErrorMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return StringUtils.hasText(current.getMessage()) ? current.getMessage() : exception.getClass().getSimpleName();
    }

    private String sha256Hex(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw BusinessException.of("SHA-256 算法不可用", exception);
        }
    }
}
