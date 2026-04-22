package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.assessment.mapper.*;
import com.mumu.woodlin.assessment.model.dto.ScoringRequest;
import com.mumu.woodlin.assessment.model.entity.*;
import com.mumu.woodlin.assessment.model.vo.DimensionScoreVO;
import com.mumu.woodlin.assessment.model.vo.ScoringResultVO;
import com.mumu.woodlin.assessment.service.IAssessmentScoringService;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.dsl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 计分服务实现
 *
 * <p>将数据库中的作答记录、题目元数据和常模数据转换为计分引擎输入，
 * 运行 DSL {@code ScoringEngine} 完成计算，并通过 {@code NormMatchEngine}
 * 进行常模匹配，最终持久化结果到 {@code sys_assessment_result} /
 * {@code sys_assessment_result_dimension}。
 *
 * <h3>常模匹配降级链（明确可审计）</h3>
 * <ol>
 *   <li>精确匹配：性别 + 年龄 + 学历 + 地区前缀</li>
 *   <li>忽略地区：性别 + 年龄 + 学历</li>
 *   <li>性别 + 年龄</li>
 *   <li>仅性别</li>
 *   <li>默认 segment（无过滤条件）</li>
 *   <li>最后兜底：优先级最高的任意 segment</li>
 *   <li>无常模集时跳过转换，返回 null 的 standardScore / percentile</li>
 * </ol>
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentScoringServiceImpl implements IAssessmentScoringService {

    private final AssessmentSessionMapper sessionMapper;
    private final AssessmentResponseMapper responseMapper;
    private final AssessmentItemMapper itemMapper;
    private final AssessmentOptionMapper optionMapper;
    private final AssessmentDimensionMapper dimensionMapper;
    private final AssessmentDimensionItemMapper dimensionItemMapper;
    private final AssessmentNormSetMapper normSetMapper;
    private final AssessmentNormSegmentMapper normSegmentMapper;
    private final AssessmentNormConversionMapper normConversionMapper;
    private final AssessmentDemographicProfileMapper demographicProfileMapper;
    private final AssessmentResultMapper resultMapper;
    private final AssessmentResultDimensionMapper resultDimensionMapper;
    private final ObjectMapper objectMapper;

    /** 内部用于携带常模转换结果。 */
    private record NormApplyResult(
            Long segmentId,
            BigDecimal standardScore,
            String normScoreType,
            BigDecimal percentile,
            String gradeLabel,
            String matchPath
    ) {}

    @Override
    @Transactional
    public ScoringResultVO scoreSession(ScoringRequest request) {
        Long sessionId = request.getSessionId();

        // 1. Load session
        AssessmentSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("作答会话不存在: " + sessionId);
        }

        // 2. Return cached result unless force rescore
        if (!request.isForceRescore()) {
            AssessmentResult existing = resultMapper.selectOne(
                    new LambdaQueryWrapper<AssessmentResult>()
                            .eq(AssessmentResult::getSessionId, sessionId)
                            .last("LIMIT 1"));
            if (existing != null) {
                log.info("Session {} already scored, returning result_id={}", sessionId, existing.getResultId());
                return buildVOFromExisting(existing, sessionId);
            }
        }

        Long versionId = session.getVersionId();
        Long formId = session.getFormId();

        // 3. Load all responses for this session
        List<AssessmentResponse> responses = responseMapper.selectList(
                new LambdaQueryWrapper<AssessmentResponse>()
                        .eq(AssessmentResponse::getSessionId, sessionId));

        // 4. Load all items for the version
        List<AssessmentItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<AssessmentItem>()
                        .eq(AssessmentItem::getVersionId, versionId));
        if (items.isEmpty()) {
            throw new BusinessException("版本 " + versionId + " 下没有题目，无法计分");
        }

        // 5. Batch-load options for all items
        List<Long> itemIds = items.stream().map(AssessmentItem::getItemId).collect(Collectors.toList());
        List<AssessmentOption> allOptions = optionMapper.selectList(
                new LambdaQueryWrapper<AssessmentOption>()
                        .in(AssessmentOption::getItemId, itemIds));
        Map<Long, List<AssessmentOption>> optionsByItemId = allOptions.stream()
                .collect(Collectors.groupingBy(AssessmentOption::getItemId));

        // 6. Load dimensions and dimension-item mappings
        List<AssessmentDimension> dimensions = dimensionMapper.selectList(
                new LambdaQueryWrapper<AssessmentDimension>()
                        .eq(AssessmentDimension::getVersionId, versionId));
        List<AssessmentDimensionItem> dimItems = dimensionItemMapper.selectList(
                new LambdaQueryWrapper<AssessmentDimensionItem>()
                        .eq(AssessmentDimensionItem::getVersionId, versionId));
        Map<Long, List<AssessmentDimensionItem>> dimItemsByItemId = dimItems.stream()
                .collect(Collectors.groupingBy(AssessmentDimensionItem::getItemId));

        // 7. Build DSL Questionnaire for the scoring engine
        Questionnaire questionnaire = buildQuestionnaire(
                formId.toString(), items, optionsByItemId, dimensions, dimItemsByItemId);

        // 8. Build answers map from response records
        Map<String, Object> answersMap = buildAnswersMap(responses, items);

        // 9. Run DSL ScoringEngine
        ScoringEngine engine = new ScoringEngine(questionnaire);
        ScoringEngine.ScoringResult engineResult = engine.score(answersMap);
        log.debug("Session {} scored: totalRaw={}, totalWeighted={}, dimensions={}",
                sessionId, engineResult.getTotalRawScore(), engineResult.getTotalWeightedScore(),
                engineResult.getDimensionScores().keySet());

        // 10. Load demographic profile for norm matching
        AssessmentDemographicProfile demographics = demographicProfileMapper.selectOne(
                new LambdaQueryWrapper<AssessmentDemographicProfile>()
                        .eq(AssessmentDemographicProfile::getSessionId, sessionId)
                        .last("LIMIT 1"));
        NormMatchEngine.DemographicContext demoCtx = buildDemoContext(demographics);

        // 11. Find default norm set for this form
        AssessmentNormSet normSet = normSetMapper.selectOne(
                new LambdaQueryWrapper<AssessmentNormSet>()
                        .eq(AssessmentNormSet::getFormId, formId)
                        .eq(AssessmentNormSet::getIsDefault, true)
                        .eq(AssessmentNormSet::getStatus, 1)
                        .last("LIMIT 1"));

        // 12. Apply norm to total score
        NormApplyResult totalNorm = null;
        if (normSet != null) {
            List<AssessmentNormSegment> segments = loadSegments(normSet.getNormSetId());
            totalNorm = applyNorm(engineResult.getTotalRawScore(), null, segments, demoCtx);
            if (totalNorm != null) {
                log.debug("Session {} total norm matched via path={}", sessionId, totalNorm.matchPath());
            }
        } else {
            log.debug("Session {} has no default norm set for form {}", sessionId, formId);
        }

        // 13. Persist AssessmentResult
        int scoredItemCount = (int) items.stream().filter(i -> Boolean.TRUE.equals(i.getIsScored())).count();
        AssessmentResult resultEntity = buildResultEntity(
                session, engineResult, totalNorm, normSet, responses.size(), scoredItemCount);
        resultMapper.insert(resultEntity);
        Long resultId = resultEntity.getResultId();

        // 14. Persist dimension results and build VO
        List<DimensionScoreVO> dimensionScoreVOs = persistDimensionResults(
                resultId, session, dimensions, engineResult, normSet, demoCtx);

        return ScoringResultVO.builder()
                .resultId(resultId)
                .sessionId(sessionId)
                .rawTotalScore(resultEntity.getRawTotalScore())
                .weightedTotalScore(resultEntity.getWeightedTotalScore())
                .standardScore(resultEntity.getStandardScore())
                .percentile(resultEntity.getPercentile())
                .gradeLabel(resultEntity.getGradeLabel())
                .normSetId(resultEntity.getNormSetId())
                .normSegmentId(resultEntity.getNormSegmentId())
                .normMatchPath(totalNorm != null ? totalNorm.matchPath() : null)
                .answeredCount(resultEntity.getAnsweredCount())
                .totalItemCount(resultEntity.getTotalItemCount())
                .dimensionScores(dimensionScoreVOs)
                .build();
    }

    // ---------------------------------------------------------------------------
    // DSL Questionnaire construction
    // ---------------------------------------------------------------------------

    /**
     * 从数据库实体构建 DSL {@code Questionnaire} 供 {@code ScoringEngine} 使用。
     */
    private Questionnaire buildQuestionnaire(
            String formId,
            List<AssessmentItem> items,
            Map<Long, List<AssessmentOption>> optionsByItemId,
            List<AssessmentDimension> dimensions,
            Map<Long, List<AssessmentDimensionItem>> dimItemsByItemId) {

        Questionnaire q = new Questionnaire(formId, formId);

        // Register dimensions
        for (AssessmentDimension dim : dimensions) {
            Dimension dslDim = new Dimension(dim.getDimensionCode(), dim.getDimensionName());
            dslDim.setScoreMode(parseScoreMode(dim.getScoreMode()));
            dslDim.setScoreDsl(dim.getScoreDsl());
            q.getDimensions().add(dslDim);
        }

        // Single section containing all items
        Section section = new Section("main");

        for (AssessmentItem item : items) {
            Question question = new Question();
            question.setId(item.getItemCode());
            question.setType(parseQuestionType(item.getItemType()));
            question.setScored(Boolean.TRUE.equals(item.getIsScored()));
            question.setDemographic(Boolean.TRUE.equals(item.getIsDemographic()));
            question.setReverse(Boolean.TRUE.equals(item.getIsReverse()));
            question.setAnchor(Boolean.TRUE.equals(item.getIsAnchor()));
            if (item.getMaxScore() != null) {
                question.setMaxScore(item.getMaxScore().doubleValue());
            }
            if (item.getMinScore() != null) {
                question.setMinScore(item.getMinScore().doubleValue());
            }
            if (Boolean.TRUE.equals(item.getIsReverse())) {
                question.setReverseMode(ReverseMode.FORMULA);
            }

            List<AssessmentDimensionItem> mappings = dimItemsByItemId.getOrDefault(item.getItemId(), List.of());
            if (!mappings.isEmpty()) {
                for (int mappingIndex = 0; mappingIndex < mappings.size(); mappingIndex++) {
                    AssessmentDimensionItem mapping = mappings.get(mappingIndex);
                    if (mappingIndex == 0) {
                        if (mapping.getWeight() != null) {
                            question.setItemWeight(mapping.getWeight().doubleValue());
                        }
                        if (mapping.getReverseMode() != null) {
                            question.setReverseMode(parseReverseMode(mapping.getReverseMode()));
                        }
                    }
                    AssessmentDimension mappedDimension = dimensions.stream()
                        .filter(d -> d.getDimensionId().equals(mapping.getDimensionId()))
                        .findFirst()
                        .orElse(null);
                    if (mappedDimension == null) {
                        continue;
                    }
                    DimensionBinding binding = new DimensionBinding(mappedDimension.getDimensionCode());
                    binding.setWeight(mapping.getWeight() != null ? mapping.getWeight().doubleValue() : 1.0D);
                    binding.setScoreModeOverride(parseNullableScoreMode(mapping.getScoreMode()));
                    binding.setReverseModeOverride(parseNullableReverseMode(mapping.getReverseMode()));
                    question.getDimensionBindings().add(binding);

                    if (mappingIndex == 0) {
                        question.setDimensionCode(mappedDimension.getDimensionCode());
                        question.setItemWeight(binding.getWeight());
                        question.setReverseMode(Objects.requireNonNullElse(
                            binding.getReverseModeOverride(),
                            Boolean.TRUE.equals(item.getIsReverse()) ? ReverseMode.FORMULA : ReverseMode.NONE));
                    }
                }
            }

            // Add options
            List<AssessmentOption> opts = optionsByItemId.getOrDefault(item.getItemId(), List.of());
            for (AssessmentOption opt : opts) {
                String text = opt.getDisplayText() != null ? opt.getDisplayText() : opt.getOptionCode();
                Double sv = opt.getScoreValue() != null ? opt.getScoreValue().doubleValue() : null;
                Double rsv = opt.getScoreReverseValue() != null ? opt.getScoreReverseValue().doubleValue() : null;
                boolean exclusive = Boolean.TRUE.equals(opt.getIsExclusive());
                boolean correct = Boolean.TRUE.equals(opt.getIsCorrect());
                Option dslOpt = new Option(text, opt.getOptionCode(), 0, exclusive, sv, rsv, correct);
                question.getOptions().add(dslOpt);
            }

            section.getQuestions().add(question);
        }

        q.getSections().add(section);
        return q;
    }

    // ---------------------------------------------------------------------------
    // Answers map construction
    // ---------------------------------------------------------------------------

    /**
     * 将 {@code AssessmentResponse} 列表转换为 {@code ScoringEngine} 期望的答案 map。
     *
     * <ul>
     *   <li>单选题：答案为 {@code String}（选项编码）</li>
     *   <li>多选题：答案为 {@code List<String>}（选项编码列表）</li>
     *   <li>文本题：答案为 {@code String}</li>
     * </ul>
     */
    private Map<String, Object> buildAnswersMap(List<AssessmentResponse> responses, List<AssessmentItem> items) {
        // Build itemId -> itemCode lookup
        Map<String, String> itemCodeById = items.stream()
                .collect(Collectors.toMap(
                        i -> i.getItemId().toString(),
                        AssessmentItem::getItemCode,
                        (a, b) -> a));

        Map<String, Object> answers = new HashMap<>();
        for (AssessmentResponse resp : responses) {
            if (Boolean.TRUE.equals(resp.getIsSkipped())) {
                continue;
            }

            String itemCode = resolveItemCode(resp, itemCodeById);
            if (itemCode == null) {
                log.warn("Cannot resolve itemCode for response {}, skipping", resp.getResponseId());
                continue;
            }

            // selectedOptionCodes: JSON array like ["A"] or ["A","B","C"]
            String selectedJson = resp.getSelectedOptionCodes();
            if (selectedJson != null && !selectedJson.isBlank()) {
                try {
                    List<String> codes = objectMapper.readValue(selectedJson,
                            new TypeReference<List<String>>() {});
                    if (!codes.isEmpty()) {
                        // Single selected option -> String; multiple -> List<String>
                        answers.put(itemCode, codes.size() == 1 ? codes.get(0) : codes);
                        continue;
                    }
                } catch (Exception e) {
                    log.debug("Could not parse selectedOptionCodes for item {}: {}", itemCode, e.getMessage());
                }
            }

            // Fallback: rawAnswer or textAnswer
            if (resp.getRawAnswer() != null && !resp.getRawAnswer().isBlank()) {
                answers.put(itemCode, resp.getRawAnswer());
            } else if (resp.getTextAnswer() != null) {
                answers.put(itemCode, resp.getTextAnswer());
            }
        }
        return answers;
    }

    private String resolveItemCode(AssessmentResponse resp, Map<String, String> itemCodeById) {
        if (resp.getItemCode() != null && !resp.getItemCode().isBlank()) {
            return resp.getItemCode();
        }
        if (resp.getItemId() != null) {
            return itemCodeById.get(resp.getItemId().toString());
        }
        return null;
    }

    // ---------------------------------------------------------------------------
    // Norm matching and conversion
    // ---------------------------------------------------------------------------

    private List<AssessmentNormSegment> loadSegments(Long normSetId) {
        return normSegmentMapper.selectList(
                new LambdaQueryWrapper<AssessmentNormSegment>()
                        .eq(AssessmentNormSegment::getNormSetId, normSetId)
                        .orderByAsc(AssessmentNormSegment::getSortPriority));
    }

    /**
     * 为给定原始分执行常模匹配 + 转换。
     *
     * <p>流程：
     * <ol>
     *   <li>将 DB segments 映射为 {@code NormMatchEngine.SegmentDescriptor}</li>
     *   <li>调用 {@code NormMatchEngine.match()} 选定最佳分层</li>
     *   <li>在 {@code NormConversion} 表中按原始分区间/精确值查找转换记录</li>
     *   <li>若未找到转换记录，记录警告并返回含 segmentId 但无 standardScore 的结果</li>
     * </ol>
     *
     * @param rawScore    待转换的原始分
     * @param dimensionId 维度ID（null 表示总分转换）
     * @param segments    候选分层列表
     * @param demoCtx     人口学上下文
     * @return 转换结果，若无 segments 则返回 null
     */
    private NormApplyResult applyNorm(
            double rawScore,
            Long dimensionId,
            List<AssessmentNormSegment> segments,
            NormMatchEngine.DemographicContext demoCtx) {

        if (segments.isEmpty()) {
            return null;
        }

        List<NormMatchEngine.SegmentDescriptor> descriptors = segments.stream()
                .map(s -> new NormMatchEngine.SegmentDescriptor(
                        s.getSegmentId(),
                        s.getSegmentCode(),
                        s.getSegmentName() != null ? s.getSegmentName() : s.getSegmentCode(),
                        s.getGenderFilter(),
                        s.getAgeMin(),
                        s.getAgeMax(),
                        s.getEducationFilter(),
                        s.getRegionCodeFilter(),
                        s.getSortPriority() != null ? s.getSortPriority() : 0))
                .collect(Collectors.toList());

        NormMatchEngine.MatchResult matchResult = NormMatchEngine.match(demoCtx, descriptors);
        if (matchResult == null) {
            return null;
        }

        if (matchResult.isFallback()) {
            log.info("Norm matching used fallback path '{}' for dimensionId={}, rawScore={}",
                    matchResult.getMatchPath(), dimensionId, rawScore);
        }

        Long segmentId = matchResult.getSegment().getSegmentId();

        // Look up conversion table entry
        LambdaQueryWrapper<AssessmentNormConversion> convQuery =
                new LambdaQueryWrapper<AssessmentNormConversion>()
                        .eq(AssessmentNormConversion::getSegmentId, segmentId)
                        .orderByAsc(AssessmentNormConversion::getSortOrder);

        if (dimensionId != null) {
            convQuery.eq(AssessmentNormConversion::getDimensionId, dimensionId);
        } else {
            convQuery.isNull(AssessmentNormConversion::getDimensionId);
        }

        List<AssessmentNormConversion> conversions = normConversionMapper.selectList(convQuery);
        if (conversions.isEmpty()) {
            log.warn("Segment {} has no conversion entries for dimensionId={}", segmentId, dimensionId);
            return new NormApplyResult(segmentId, null, null, null, null, matchResult.getMatchPath());
        }

        BigDecimal rawBD = BigDecimal.valueOf(rawScore).setScale(4, RoundingMode.HALF_UP);
        AssessmentNormConversion matched = conversions.stream()
                .filter(c -> conversionEntryMatches(c, rawBD))
                .findFirst()
                .orElse(null);

        if (matched == null) {
            log.warn("No conversion entry found for rawScore={} in segment {} dimensionId={}",
                    rawScore, segmentId, dimensionId);
            return new NormApplyResult(segmentId, null, null, null, null, matchResult.getMatchPath());
        }

        return new NormApplyResult(
                segmentId,
                matched.getStandardScore(),
                matched.getNormScoreType(),
                matched.getPercentile(),
                matched.getGradeLabel(),
                matchResult.getMatchPath()
        );
    }

    /** Returns true when a conversion entry matches the given raw score (exact or range). */
    private boolean conversionEntryMatches(AssessmentNormConversion c, BigDecimal rawBD) {
        if (c.getRawScoreExact() != null) {
            return c.getRawScoreExact().setScale(4, RoundingMode.HALF_UP).compareTo(rawBD) == 0;
        }
        boolean minOk = c.getRawScoreMin() == null
                || rawBD.compareTo(c.getRawScoreMin()) >= 0;
        boolean maxOk = c.getRawScoreMax() == null
                || rawBD.compareTo(c.getRawScoreMax()) <= 0;
        return minOk && maxOk;
    }

    // ---------------------------------------------------------------------------
    // Persistence helpers
    // ---------------------------------------------------------------------------

    private AssessmentResult buildResultEntity(
            AssessmentSession session,
            ScoringEngine.ScoringResult engineResult,
            NormApplyResult totalNorm,
            AssessmentNormSet normSet,
            int answeredCount,
            int scoredItemCount) {

        AssessmentResult entity = new AssessmentResult();
        double weightedTotalScore = engineResult.getDimensionScores().isEmpty()
                ? engineResult.getTotalRawScore()
                : engineResult.getTotalWeightedScore();
        entity.setSessionId(session.getSessionId());
        entity.setFormId(session.getFormId());
        entity.setPublishId(session.getPublishId());
        entity.setUserId(session.getUserId());
        entity.setRawTotalScore(BigDecimal.valueOf(engineResult.getTotalRawScore())
                .setScale(4, RoundingMode.HALF_UP));
        entity.setWeightedTotalScore(BigDecimal.valueOf(weightedTotalScore)
                .setScale(4, RoundingMode.HALF_UP));
        entity.setAnsweredCount(answeredCount);
        entity.setTotalItemCount(scoredItemCount);
        entity.setTenantId(session.getTenantId());
        entity.setScoreTraceJson(buildScoreTraceJson(engineResult, totalNorm));

        if (totalNorm != null) {
            if (normSet != null) {
                entity.setNormSetId(normSet.getNormSetId());
            }
            entity.setNormSegmentId(totalNorm.segmentId());
            entity.setStandardScore(totalNorm.standardScore());
            entity.setNormScoreType(totalNorm.normScoreType());
            entity.setPercentile(totalNorm.percentile());
            entity.setGradeLabel(totalNorm.gradeLabel());
        }

        return entity;
    }

    private List<DimensionScoreVO> persistDimensionResults(
            Long resultId,
            AssessmentSession session,
            List<AssessmentDimension> dimensions,
            ScoringEngine.ScoringResult engineResult,
            AssessmentNormSet normSet,
            NormMatchEngine.DemographicContext demoCtx) {

        List<DimensionScoreVO> vos = new ArrayList<>();

        for (Map.Entry<String, ScoringEngine.DimensionScore> entry :
                engineResult.getDimensionScores().entrySet()) {

            String dimCode = entry.getKey();
            ScoringEngine.DimensionScore dimScore = entry.getValue();

            AssessmentDimension dimEntity = dimensions.stream()
                    .filter(d -> dimCode.equals(d.getDimensionCode()))
                    .findFirst().orElse(null);

            // Determine which norm set to use for this dimension
            Long dimNormSetId = normSet != null ? normSet.getNormSetId() : null;
            if (dimEntity != null && dimEntity.getNormSetId() != null) {
                // Dimension has its own norm set override
                dimNormSetId = dimEntity.getNormSetId();
            }

            Long dimIdForConversion = dimEntity != null ? dimEntity.getDimensionId() : null;
            NormApplyResult dimNorm = null;
            if (dimNormSetId != null) {
                List<AssessmentNormSegment> dimSegments = loadSegments(dimNormSetId);
                dimNorm = applyNorm(dimScore.getRawScore(), dimIdForConversion, dimSegments, demoCtx);
            }

            AssessmentResultDimension rdEntity = new AssessmentResultDimension();
            rdEntity.setResultId(resultId);
            rdEntity.setSessionId(session.getSessionId());
            rdEntity.setDimensionCode(dimCode);
            rdEntity.setRawScore(BigDecimal.valueOf(dimScore.getRawScore()).setScale(4, RoundingMode.HALF_UP));
            rdEntity.setItemCount(dimScore.getItemCount());
            if (dimScore.getItemCount() > 0) {
                rdEntity.setMeanScore(BigDecimal.valueOf(dimScore.getRawScore() / dimScore.getItemCount())
                        .setScale(4, RoundingMode.HALF_UP));
            }
            rdEntity.setTenantId(session.getTenantId());

            if (dimEntity != null) {
                rdEntity.setDimensionId(dimEntity.getDimensionId());
            }
            if (dimNorm != null) {
                rdEntity.setNormSegmentId(dimNorm.segmentId());
                rdEntity.setStandardScore(dimNorm.standardScore());
                rdEntity.setPercentile(dimNorm.percentile());
                rdEntity.setGradeLabel(dimNorm.gradeLabel());
            }

            resultDimensionMapper.insert(rdEntity);

            vos.add(DimensionScoreVO.builder()
                    .dimensionId(dimEntity != null ? dimEntity.getDimensionId() : null)
                    .dimensionCode(dimCode)
                    .dimensionName(dimScore.getDimensionName())
                    .rawScore(rdEntity.getRawScore())
                    .meanScore(rdEntity.getMeanScore())
                    .itemCount(dimScore.getItemCount())
                    .standardScore(rdEntity.getStandardScore())
                    .percentile(rdEntity.getPercentile())
                    .gradeLabel(rdEntity.getGradeLabel())
                    .normSegmentId(rdEntity.getNormSegmentId())
                    .normMatchPath(dimNorm != null ? dimNorm.matchPath() : null)
                    .build());
        }

        return vos;
    }

    private ScoringResultVO buildVOFromExisting(AssessmentResult existing, Long sessionId) {
        List<AssessmentResultDimension> dimResults = resultDimensionMapper.selectList(
                new LambdaQueryWrapper<AssessmentResultDimension>()
                        .eq(AssessmentResultDimension::getResultId, existing.getResultId()));

        List<DimensionScoreVO> dimVOs = dimResults.stream()
                .map(rd -> DimensionScoreVO.builder()
                        .dimensionId(rd.getDimensionId())
                        .dimensionCode(rd.getDimensionCode())
                        .rawScore(rd.getRawScore())
                        .meanScore(rd.getMeanScore())
                        .itemCount(rd.getItemCount())
                        .standardScore(rd.getStandardScore())
                        .percentile(rd.getPercentile())
                        .gradeLabel(rd.getGradeLabel())
                        .normSegmentId(rd.getNormSegmentId())
                        .build())
                .collect(Collectors.toList());

        return ScoringResultVO.builder()
                .resultId(existing.getResultId())
                .sessionId(sessionId)
                .rawTotalScore(existing.getRawTotalScore())
                .weightedTotalScore(existing.getWeightedTotalScore())
                .standardScore(existing.getStandardScore())
                .percentile(existing.getPercentile())
                .gradeLabel(existing.getGradeLabel())
                .normSetId(existing.getNormSetId())
                .normSegmentId(existing.getNormSegmentId())
                .answeredCount(existing.getAnsweredCount())
                .totalItemCount(existing.getTotalItemCount())
                .dimensionScores(dimVOs)
                .build();
    }

    // ---------------------------------------------------------------------------
    // Enum parsers
    // ---------------------------------------------------------------------------

    private NormMatchEngine.DemographicContext buildDemoContext(AssessmentDemographicProfile p) {
        if (p == null) {
            return new NormMatchEngine.DemographicContext(null, null, null, null, null);
        }
        return new NormMatchEngine.DemographicContext(
                p.getGender(), p.getAge(), p.getEducationLevel(),
                p.getRegionCode(), p.getProvinceCode());
    }

    private ScoreMode parseScoreMode(String mode) {
        if (mode == null) return ScoreMode.SUM;
        return switch (mode.toLowerCase()) {
            case "mean" -> ScoreMode.MEAN;
            case "max" -> ScoreMode.MAX;
            case "min" -> ScoreMode.MIN;
            case "weighted_sum" -> ScoreMode.WEIGHTED_SUM;
            case "custom_dsl" -> ScoreMode.CUSTOM_DSL;
            default -> ScoreMode.SUM;
        };
    }

    private ScoreMode parseNullableScoreMode(String mode) {
        if (mode == null) {
            return null;
        }
        return parseScoreMode(mode);
    }

    private ReverseMode parseReverseMode(String mode) {
        if (mode == null) return ReverseMode.NONE;
        return switch (mode.toLowerCase()) {
            case "formula" -> ReverseMode.FORMULA;
            case "table" -> ReverseMode.TABLE;
            default -> ReverseMode.NONE;
        };
    }

    private ReverseMode parseNullableReverseMode(String mode) {
        if (mode == null) {
            return null;
        }
        return parseReverseMode(mode);
    }

    private QuestionType parseQuestionType(String type) {
        if (type == null) return QuestionType.SINGLE_CHOICE;
        return switch (type.toLowerCase()) {
            case "multiple_choice" -> QuestionType.MULTIPLE_CHOICE;
            case "rating" -> QuestionType.RATING;
            case "number" -> QuestionType.NUMBER;
            case "matrix_single" -> QuestionType.MATRIX_SINGLE;
            case "matrix_multiple" -> QuestionType.MATRIX_MULTIPLE;
            case "text", "short_text", "textarea" -> QuestionType.TEXT;
            case "demographic" -> QuestionType.DEMOGRAPHIC;
            case "statement" -> QuestionType.STATEMENT;
            default -> QuestionType.SINGLE_CHOICE;
        };
    }

    private String buildScoreTraceJson(
        ScoringEngine.ScoringResult engineResult,
        NormApplyResult totalNorm) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("totalRawScore", engineResult.getTotalRawScore());
        trace.put("totalWeightedScore", engineResult.getTotalWeightedScore());
        trace.put("itemScores", engineResult.getItemScores());
        trace.put("dimensionContributions", engineResult.getDimensionContributions());
        trace.put("dimensionScores", engineResult.getDimensionScores());
        if (totalNorm != null) {
            Map<String, Object> normTrace = new LinkedHashMap<>();
            normTrace.put("segmentId", totalNorm.segmentId());
            normTrace.put("standardScore", totalNorm.standardScore());
            normTrace.put("normScoreType", totalNorm.normScoreType());
            normTrace.put("percentile", totalNorm.percentile());
            normTrace.put("gradeLabel", totalNorm.gradeLabel());
            normTrace.put("matchPath", totalNorm.matchPath());
            trace.put("totalNorm", normTrace);
        }
        try {
            return objectMapper.writeValueAsString(trace);
        } catch (JsonProcessingException exception) {
            log.warn("计分轨迹序列化失败，将忽略 score_trace_json", exception);
            return null;
        }
    }
}
