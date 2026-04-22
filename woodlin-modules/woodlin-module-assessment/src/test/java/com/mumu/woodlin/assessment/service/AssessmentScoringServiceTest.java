package com.mumu.woodlin.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.assessment.mapper.*;
import com.mumu.woodlin.assessment.model.dto.ScoringRequest;
import com.mumu.woodlin.assessment.model.entity.*;
import com.mumu.woodlin.assessment.model.vo.DimensionScoreVO;
import com.mumu.woodlin.assessment.model.vo.ScoringResultVO;
import com.mumu.woodlin.assessment.service.impl.AssessmentScoringServiceImpl;
import com.mumu.woodlin.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AssessmentScoringServiceImpl 单元测试
 *
 * <p>使用 Mockito 模拟所有 DB mapper，无需 Spring 上下文，运行速度快。
 *
 * @author mumu
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class AssessmentScoringServiceTest {

    @Mock AssessmentSessionMapper sessionMapper;
    @Mock AssessmentResponseMapper responseMapper;
    @Mock AssessmentItemMapper itemMapper;
    @Mock AssessmentOptionMapper optionMapper;
    @Mock AssessmentDimensionMapper dimensionMapper;
    @Mock AssessmentDimensionItemMapper dimensionItemMapper;
    @Mock AssessmentNormSetMapper normSetMapper;
    @Mock AssessmentNormSegmentMapper normSegmentMapper;
    @Mock AssessmentNormConversionMapper normConversionMapper;
    @Mock AssessmentDemographicProfileMapper demographicProfileMapper;
    @Mock AssessmentResultMapper resultMapper;
    @Mock AssessmentResultDimensionMapper resultDimensionMapper;

    @InjectMocks
    AssessmentScoringServiceImpl service;

    private static final Long SESSION_ID = 100L;
    private static final Long FORM_ID = 1L;
    private static final Long VERSION_ID = 10L;

    @BeforeEach
    void setUp() {
        // ObjectMapper is a concrete class - inject real instance
        // @InjectMocks won't inject it because it's not @Mock; use reflection helper below
        injectObjectMapper();
    }

    /** Inject a real ObjectMapper via reflection (field name matches constructor param). */
    private void injectObjectMapper() {
        try {
            var field = AssessmentScoringServiceImpl.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(service, new ObjectMapper());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // Guard tests
    // -----------------------------------------------------------------------

    @Test
    void scoreSession_sessionNotFound_throwsBusinessException() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(null);
        ScoringRequest req = request(SESSION_ID);
        assertThatThrownBy(() -> service.scoreSession(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(SESSION_ID.toString());
    }

    @Test
    void scoreSession_noItems_throwsBusinessException() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(responseMapper.selectList(any())).thenReturn(List.of());
        when(itemMapper.selectList(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.scoreSession(request(SESSION_ID)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("没有题目");
    }

    @Test
    void scoreSession_alreadyScored_returnsCached() {
        AssessmentResult cached = new AssessmentResult();
        cached.setResultId(999L);
        cached.setSessionId(SESSION_ID);
        cached.setRawTotalScore(BigDecimal.valueOf(5.0));
        cached.setWeightedTotalScore(BigDecimal.valueOf(5.0));
        cached.setAnsweredCount(2);
        cached.setTotalItemCount(2);

        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(cached);
        when(resultDimensionMapper.selectList(any())).thenReturn(List.of());

        ScoringResultVO result = service.scoreSession(request(SESSION_ID));

        assertThat(result.getResultId()).isEqualTo(999L);
        assertThat(result.getRawTotalScore()).isEqualByComparingTo("5.0");
        verify(itemMapper, never()).selectList(any());
    }

    // -----------------------------------------------------------------------
    // Basic scoring: single-choice, no dimensions, no norms
    // -----------------------------------------------------------------------

    @Test
    void scoreSession_singleChoice_correctRawScore() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(responseMapper.selectList(any())).thenReturn(twoResponses());
        when(itemMapper.selectList(any())).thenReturn(twoItems());
        when(optionMapper.selectList(any())).thenReturn(optionsForTwoItems());
        when(dimensionMapper.selectList(any())).thenReturn(List.of());
        when(dimensionItemMapper.selectList(any())).thenReturn(List.of());
        when(normSetMapper.selectOne(any())).thenReturn(null);
        when(demographicProfileMapper.selectOne(any())).thenReturn(null);
        when(resultMapper.insert(any(AssessmentResult.class))).thenReturn(1);

        ScoringResultVO result = service.scoreSession(request(SESSION_ID));

        // q1 answered "B" (scoreValue=1.0) + q2 answered "C" (scoreValue=2.0) = 3.0
        assertThat(result.getRawTotalScore()).isEqualByComparingTo("3.0000");
        assertThat(result.getWeightedTotalScore()).isEqualByComparingTo("3.0000");
        assertThat(result.getDimensionScores()).isEmpty();
        assertThat(result.getNormSetId()).isNull();
        assertThat(result.getStandardScore()).isNull();
    }

    // -----------------------------------------------------------------------
    // Multi-choice scoring
    // -----------------------------------------------------------------------

    @Test
    void scoreSession_multiChoice_sumsSelectedOptionScores() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(responseMapper.selectList(any())).thenReturn(multiChoiceResponse());
        when(itemMapper.selectList(any())).thenReturn(multiChoiceItem());
        when(optionMapper.selectList(any())).thenReturn(multiChoiceOptions());
        when(dimensionMapper.selectList(any())).thenReturn(List.of());
        when(dimensionItemMapper.selectList(any())).thenReturn(List.of());
        when(normSetMapper.selectOne(any())).thenReturn(null);
        when(demographicProfileMapper.selectOne(any())).thenReturn(null);
        when(resultMapper.insert(any(AssessmentResult.class))).thenReturn(1);

        ScoringResultVO result = service.scoreSession(request(SESSION_ID));

        // Selected A (1.0) + C (3.0) = 4.0
        assertThat(result.getRawTotalScore()).isEqualByComparingTo("4.0000");
    }

    // -----------------------------------------------------------------------
    // Reverse scoring
    // -----------------------------------------------------------------------

    @Test
    void scoreSession_reverseFormula_appliesFormulaCorrectly() {
        // max=3, min=0, raw=1 -> effective = 3+0-1 = 2
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(responseMapper.selectList(any())).thenReturn(reverseResponse());
        when(itemMapper.selectList(any())).thenReturn(reverseItem());
        when(optionMapper.selectList(any())).thenReturn(reverseOptions());
        when(dimensionMapper.selectList(any())).thenReturn(List.of());
        when(dimensionItemMapper.selectList(any())).thenReturn(dimItemsForReverseItem());
        when(normSetMapper.selectOne(any())).thenReturn(null);
        when(demographicProfileMapper.selectOne(any())).thenReturn(null);
        when(resultMapper.insert(any(AssessmentResult.class))).thenReturn(1);

        ScoringResultVO result = service.scoreSession(request(SESSION_ID));

        // raw score for "1" option = 1.0; reverse formula max+min-raw = 3+0-1 = 2.0
        assertThat(result.getRawTotalScore()).isEqualByComparingTo("2.0000");
    }

    // -----------------------------------------------------------------------
    // Dimension aggregation
    // -----------------------------------------------------------------------

    @Test
    void scoreSession_withDimension_aggregatesDimensionScore() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(responseMapper.selectList(any())).thenReturn(twoResponses());
        when(itemMapper.selectList(any())).thenReturn(twoItems());
        when(optionMapper.selectList(any())).thenReturn(optionsForTwoItems());
        when(dimensionMapper.selectList(any())).thenReturn(oneDimension());
        when(dimensionItemMapper.selectList(any())).thenReturn(dimItemsForTwoItems());
        when(normSetMapper.selectOne(any())).thenReturn(null);
        when(demographicProfileMapper.selectOne(any())).thenReturn(null);
        when(resultMapper.insert(any(AssessmentResult.class))).thenReturn(1);
        when(resultDimensionMapper.insert(any(AssessmentResultDimension.class))).thenReturn(1);

        ScoringResultVO result = service.scoreSession(request(SESSION_ID));

        assertThat(result.getDimensionScores()).hasSize(1);
        assertThat(result.getDimensionScores().get(0).getDimensionCode()).isEqualTo("depression");
        // q1=1.0 + q2=2.0 = 3.0
        assertThat(result.getDimensionScores().get(0).getRawScore()).isEqualByComparingTo("3.0000");
        assertThat(result.getDimensionScores().get(0).getItemCount()).isEqualTo(2);
    }

    @Test
    void scoreSession_multiDimensionItem_contributesToEachDimensionAndWritesTrace() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(responseMapper.selectList(any())).thenReturn(twoResponses());
        when(itemMapper.selectList(any())).thenReturn(twoItems());
        when(optionMapper.selectList(any())).thenReturn(optionsForTwoItems());
        when(dimensionMapper.selectList(any())).thenReturn(twoDimensions());
        when(dimensionItemMapper.selectList(any())).thenReturn(dimItemsForMultiDimension());
        when(normSetMapper.selectOne(any())).thenReturn(null);
        when(demographicProfileMapper.selectOne(any())).thenReturn(null);
        when(resultMapper.insert(any(AssessmentResult.class))).thenAnswer(invocation -> {
            AssessmentResult entity = invocation.getArgument(0);
            entity.setResultId(1000L);
            return 1;
        });
        when(resultDimensionMapper.insert(any(AssessmentResultDimension.class))).thenReturn(1);

        ScoringResultVO result = service.scoreSession(request(SESSION_ID));

        assertThat(result.getDimensionScores()).hasSize(2);
        assertThat(result.getDimensionScores())
            .extracting(DimensionScoreVO::getDimensionCode)
            .containsExactlyInAnyOrder("depression", "anxiety");
        assertThat(result.getDimensionScores())
            .filteredOn(score -> "anxiety".equals(score.getDimensionCode()))
            .first()
            .extracting(DimensionScoreVO::getRawScore)
            .isEqualTo(BigDecimal.valueOf(1.0).setScale(4));
        ArgumentCaptor<AssessmentResult> captor = ArgumentCaptor.forClass(AssessmentResult.class);
        verify(resultMapper).insert(captor.capture());
        assertThat(captor.getValue().getScoreTraceJson()).contains("dimensionContributions");
    }

    // -----------------------------------------------------------------------
    // Force rescore
    // -----------------------------------------------------------------------

    @Test
    void scoreSession_forceRescore_bypassesCache() {
        when(sessionMapper.selectById(SESSION_ID)).thenReturn(session());
        // There IS an existing result
        AssessmentResult existing = new AssessmentResult();
        existing.setResultId(777L);
        existing.setSessionId(SESSION_ID);
        existing.setRawTotalScore(BigDecimal.ZERO);
        existing.setWeightedTotalScore(BigDecimal.ZERO);

        when(responseMapper.selectList(any())).thenReturn(twoResponses());
        when(itemMapper.selectList(any())).thenReturn(twoItems());
        when(optionMapper.selectList(any())).thenReturn(optionsForTwoItems());
        when(dimensionMapper.selectList(any())).thenReturn(List.of());
        when(dimensionItemMapper.selectList(any())).thenReturn(List.of());
        when(normSetMapper.selectOne(any())).thenReturn(null);
        when(demographicProfileMapper.selectOne(any())).thenReturn(null);
        when(resultMapper.insert(any(AssessmentResult.class))).thenReturn(1);

        ScoringRequest req = request(SESSION_ID);
        req.setForceRescore(true);
        ScoringResultVO result = service.scoreSession(req);

        // Should NOT use cached; should recompute fresh score
        assertThat(result.getRawTotalScore()).isEqualByComparingTo("3.0000");
        // resultMapper.selectOne never called when forceRescore=true
        verify(resultMapper, never()).selectOne(any());
    }

    // -----------------------------------------------------------------------
    // Test data builders
    // -----------------------------------------------------------------------

    private ScoringRequest request(Long sessionId) {
        ScoringRequest r = new ScoringRequest();
        r.setSessionId(sessionId);
        return r;
    }

    private AssessmentSession session() {
        AssessmentSession s = new AssessmentSession();
        s.setSessionId(SESSION_ID);
        s.setFormId(FORM_ID);
        s.setVersionId(VERSION_ID);
        s.setPublishId(50L);
        s.setUserId(200L);
        s.setTenantId("default");
        return s;
    }

    /** Two single-choice items: q1 and q2. */
    private List<AssessmentItem> twoItems() {
        AssessmentItem q1 = new AssessmentItem();
        q1.setItemId(1L);
        q1.setVersionId(VERSION_ID);
        q1.setItemCode("q1");
        q1.setItemType("single_choice");
        q1.setIsScored(true);
        q1.setIsReverse(false);
        q1.setIsDemographic(false);

        AssessmentItem q2 = new AssessmentItem();
        q2.setItemId(2L);
        q2.setVersionId(VERSION_ID);
        q2.setItemCode("q2");
        q2.setItemType("single_choice");
        q2.setIsScored(true);
        q2.setIsReverse(false);
        q2.setIsDemographic(false);

        return List.of(q1, q2);
    }

    /** Options: A=0, B=1, C=2 for both q1 and q2. */
    private List<AssessmentOption> optionsForTwoItems() {
        return List.of(
                opt(1L, 1L, "A", 0.0), opt(2L, 1L, "B", 1.0), opt(3L, 1L, "C", 2.0),
                opt(4L, 2L, "A", 0.0), opt(5L, 2L, "B", 1.0), opt(6L, 2L, "C", 2.0)
        );
    }

    /** q1 answered "B", q2 answered "C". Total = 1 + 2 = 3. */
    private List<AssessmentResponse> twoResponses() {
        return List.of(
                response(1L, 1L, "q1", "[\"B\"]"),
                response(2L, 2L, "q2", "[\"C\"]")
        );
    }

    /** Single multi-choice item with 3 options. */
    private List<AssessmentItem> multiChoiceItem() {
        AssessmentItem q = new AssessmentItem();
        q.setItemId(1L);
        q.setVersionId(VERSION_ID);
        q.setItemCode("mc1");
        q.setItemType("multiple_choice");
        q.setIsScored(true);
        q.setIsReverse(false);
        q.setIsDemographic(false);
        return List.of(q);
    }

    private List<AssessmentOption> multiChoiceOptions() {
        return List.of(
                opt(1L, 1L, "A", 1.0), opt(2L, 1L, "B", 2.0), opt(3L, 1L, "C", 3.0)
        );
    }

    /** mc1 answered A + C. Total = 1 + 3 = 4. */
    private List<AssessmentResponse> multiChoiceResponse() {
        return List.of(response(1L, 1L, "mc1", "[\"A\",\"C\"]"));
    }

    /** Single reverse-scored item with FORMULA mode: max=3, min=0, option "1" scoreValue=1.0. */
    private List<AssessmentItem> reverseItem() {
        AssessmentItem q = new AssessmentItem();
        q.setItemId(1L);
        q.setVersionId(VERSION_ID);
        q.setItemCode("r1");
        q.setItemType("single_choice");
        q.setIsScored(true);
        q.setIsReverse(true);
        q.setIsDemographic(false);
        q.setMaxScore(BigDecimal.valueOf(3.0));
        q.setMinScore(BigDecimal.valueOf(0.0));
        return List.of(q);
    }

    private List<AssessmentOption> reverseOptions() {
        return List.of(opt(1L, 1L, "1", 1.0), opt(2L, 1L, "2", 2.0), opt(3L, 1L, "3", 3.0));
    }

    private List<AssessmentResponse> reverseResponse() {
        return List.of(response(1L, 1L, "r1", "[\"1\"]"));
    }

    /** Dimension-item mappings for r1 with FORMULA reverse mode. */
    private List<AssessmentDimensionItem> dimItemsForReverseItem() {
        AssessmentDimensionItem di = new AssessmentDimensionItem();
        di.setId(1L);
        di.setDimensionId(10L);
        di.setItemId(1L);
        di.setVersionId(VERSION_ID);
        di.setReverseMode("formula");
        di.setWeight(BigDecimal.ONE);
        return List.of(di);
    }

    private List<AssessmentDimension> oneDimension() {
        AssessmentDimension dim = new AssessmentDimension();
        dim.setDimensionId(10L);
        dim.setFormId(FORM_ID);
        dim.setVersionId(VERSION_ID);
        dim.setDimensionCode("depression");
        dim.setDimensionName("抑郁总分");
        dim.setScoreMode("sum");
        return List.of(dim);
    }

    private List<AssessmentDimension> twoDimensions() {
        AssessmentDimension dep = new AssessmentDimension();
        dep.setDimensionId(10L);
        dep.setFormId(FORM_ID);
        dep.setVersionId(VERSION_ID);
        dep.setDimensionCode("depression");
        dep.setDimensionName("抑郁");
        dep.setScoreMode("sum");

        AssessmentDimension anx = new AssessmentDimension();
        anx.setDimensionId(11L);
        anx.setFormId(FORM_ID);
        anx.setVersionId(VERSION_ID);
        anx.setDimensionCode("anxiety");
        anx.setDimensionName("焦虑");
        anx.setScoreMode("sum");
        return List.of(dep, anx);
    }

    /** Both q1 and q2 belong to dimension "depression". */
    private List<AssessmentDimensionItem> dimItemsForTwoItems() {
        AssessmentDimensionItem di1 = new AssessmentDimensionItem();
        di1.setId(1L);
        di1.setDimensionId(10L);
        di1.setItemId(1L);
        di1.setVersionId(VERSION_ID);
        di1.setReverseMode("none");
        di1.setWeight(BigDecimal.ONE);

        AssessmentDimensionItem di2 = new AssessmentDimensionItem();
        di2.setId(2L);
        di2.setDimensionId(10L);
        di2.setItemId(2L);
        di2.setVersionId(VERSION_ID);
        di2.setReverseMode("none");
        di2.setWeight(BigDecimal.ONE);

        return List.of(di1, di2);
    }

    private List<AssessmentDimensionItem> dimItemsForMultiDimension() {
        AssessmentDimensionItem di1 = new AssessmentDimensionItem();
        di1.setId(1L);
        di1.setDimensionId(10L);
        di1.setItemId(1L);
        di1.setVersionId(VERSION_ID);
        di1.setReverseMode("none");
        di1.setWeight(BigDecimal.ONE);

        AssessmentDimensionItem di2 = new AssessmentDimensionItem();
        di2.setId(2L);
        di2.setDimensionId(11L);
        di2.setItemId(1L);
        di2.setVersionId(VERSION_ID);
        di2.setReverseMode("none");
        di2.setWeight(BigDecimal.ONE);

        AssessmentDimensionItem di3 = new AssessmentDimensionItem();
        di3.setId(3L);
        di3.setDimensionId(10L);
        di3.setItemId(2L);
        di3.setVersionId(VERSION_ID);
        di3.setReverseMode("none");
        di3.setWeight(BigDecimal.ONE);
        return List.of(di1, di2, di3);
    }

    // ---------------------------------------------------------------------------
    // Factory helpers
    // ---------------------------------------------------------------------------

    private AssessmentOption opt(Long id, Long itemId, String code, double score) {
        AssessmentOption o = new AssessmentOption();
        o.setOptionId(id);
        o.setItemId(itemId);
        o.setOptionCode(code);
        o.setDisplayText(code);
        o.setScoreValue(BigDecimal.valueOf(score));
        return o;
    }

    private AssessmentResponse response(Long id, Long itemId, String itemCode, String selectedJson) {
        AssessmentResponse r = new AssessmentResponse();
        r.setResponseId(id);
        r.setSessionId(SESSION_ID);
        r.setItemId(itemId);
        r.setItemCode(itemCode);
        r.setSelectedOptionCodes(selectedJson);
        r.setIsSkipped(false);
        return r;
    }
}
