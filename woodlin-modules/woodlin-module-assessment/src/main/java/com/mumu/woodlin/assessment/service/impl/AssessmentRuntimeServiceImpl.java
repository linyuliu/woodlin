package com.mumu.woodlin.assessment.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mumu.woodlin.assessment.enums.PublicationStatus;
import com.mumu.woodlin.assessment.enums.RandomStrategy;
import com.mumu.woodlin.assessment.enums.SessionStatus;
import com.mumu.woodlin.assessment.mapper.AssessmentItemMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentOptionMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentPublishMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentResponseMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentSectionMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentSessionMapper;
import com.mumu.woodlin.assessment.mapper.AssessmentSessionSnapshotMapper;
import com.mumu.woodlin.assessment.model.dto.AnswerItemDTO;
import com.mumu.woodlin.assessment.model.dto.SaveSnapshotDTO;
import com.mumu.woodlin.assessment.model.dto.StartSessionDTO;
import com.mumu.woodlin.assessment.model.dto.SubmitAnswersDTO;
import com.mumu.woodlin.assessment.model.entity.AssessmentItem;
import com.mumu.woodlin.assessment.model.entity.AssessmentOption;
import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.entity.AssessmentResponse;
import com.mumu.woodlin.assessment.model.entity.AssessmentSection;
import com.mumu.woodlin.assessment.model.entity.AssessmentSession;
import com.mumu.woodlin.assessment.model.entity.AssessmentSessionSnapshot;
import com.mumu.woodlin.assessment.model.vo.RuntimeItemVO;
import com.mumu.woodlin.assessment.model.vo.RuntimeOptionVO;
import com.mumu.woodlin.assessment.model.vo.RuntimePayloadVO;
import com.mumu.woodlin.assessment.model.vo.RuntimePublishVO;
import com.mumu.woodlin.assessment.model.vo.RuntimeSectionVO;
import com.mumu.woodlin.assessment.model.vo.RuntimeSessionVO;
import com.mumu.woodlin.assessment.service.IAssessmentRuntimeService;
import com.mumu.woodlin.common.exception.BusinessException;

/**
 * 作答运行时服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentRuntimeServiceImpl implements IAssessmentRuntimeService {

    private final AssessmentPublishMapper publishMapper;
    private final AssessmentSessionMapper sessionMapper;
    private final AssessmentSessionSnapshotMapper sessionSnapshotMapper;
    private final AssessmentSectionMapper sectionMapper;
    private final AssessmentItemMapper itemMapper;
    private final AssessmentOptionMapper optionMapper;
    private final AssessmentResponseMapper responseMapper;
    private final ObjectMapper objectMapper;

    @Override
    public RuntimePublishVO getPublishInfo(Long publishId) {
        AssessmentPublish publish = getPublishOrThrow(publishId);
        validatePublished(publish);
        return toPublishVO(publish);
    }

    @Override
    public RuntimePayloadVO startOrResumeSession(StartSessionDTO dto, Long userId) {
        AssessmentPublish publish = getPublishOrThrow(dto.getPublishId());
        validatePublishActive(publish);
        validateAccessPolicy(publish, userId);

        if (Boolean.TRUE.equals(publish.getAllowResume())) {
            AssessmentSession existingSession = findInProgressSession(publish.getPublishId(), userId, dto.getAnonymousToken());
            if (existingSession != null) {
                return buildRuntimePayload(publish, existingSession);
            }
        }

        int attemptCount = countAttempts(publish.getPublishId(), userId, dto.getAnonymousToken());
        if (publish.getMaxAttempts() != null && publish.getMaxAttempts() > 0 && attemptCount >= publish.getMaxAttempts()) {
            throw BusinessException.of("已超过最大作答次数");
        }

        AssessmentSession session = new AssessmentSession()
                .setPublishId(publish.getPublishId())
                .setFormId(publish.getFormId())
                .setVersionId(publish.getVersionId())
                .setUserId(userId)
                .setAnonymousToken(normalizeText(dto.getAnonymousToken()))
                .setStatus(SessionStatus.IN_PROGRESS.getCode())
                .setDisplaySeed(System.currentTimeMillis())
                .setStartedAt(LocalDateTime.now())
                .setElapsedSeconds(0)
                .setClientIp(dto.getClientIp())
                .setUserAgent(dto.getUserAgent())
                .setDeviceType(dto.getDeviceType())
                .setAttemptNumber(attemptCount + 1)
                .setTenantId(publish.getTenantId());
        sessionMapper.insert(session);
        return buildRuntimePayload(publish, session);
    }

    @Override
    public RuntimePayloadVO loadSessionPayload(Long sessionId, Long userId) {
        AssessmentSession session = getSessionOrThrow(sessionId);
        validateSessionOwnership(session, userId);
        AssessmentPublish publish = getPublishOrThrow(session.getPublishId());
        return buildRuntimePayload(publish, session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSnapshot(SaveSnapshotDTO dto, Long userId) {
        AssessmentSession session = getSessionOrThrow(dto.getSessionId());
        validateSessionOwnership(session, userId);
        validateSessionInProgress(session);

        Integer elapsedSeconds = dto.getElapsedSeconds() != null ? dto.getElapsedSeconds() : session.getElapsedSeconds();
        insertSnapshot(session, dto.getCurrentSectionCode(), dto.getCurrentItemCode(), dto.getAnsweredCache(), elapsedSeconds);
        updateSessionElapsedSeconds(session.getSessionId(), dto.getElapsedSeconds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitSession(SubmitAnswersDTO dto, Long userId) {
        AssessmentSession session = getSessionOrThrow(dto.getSessionId());
        validateSessionOwnership(session, userId);
        validateSessionInProgress(session);

        AssessmentPublish publish = getPublishOrThrow(session.getPublishId());
        RuntimeAssembly assembly = assembleRuntime(publish, session);
        Map<String, AssessmentItem> itemMap = assembly.sections.stream()
                .flatMap(section -> section.items.stream())
                .map(item -> item.item)
                .collect(Collectors.toMap(AssessmentItem::getItemCode, item -> item, (left, right) -> left, LinkedHashMap::new));

        LocalDateTime answeredAt = LocalDateTime.now();
        if (!CollectionUtils.isEmpty(dto.getAnswers())) {
            for (AnswerItemDTO answer : dto.getAnswers()) {
                AssessmentItem item = itemMap.get(answer.getItemCode());
                if (item == null) {
                    throw BusinessException.of("题目不存在: " + answer.getItemCode());
                }

                responseMapper.delete(new LambdaQueryWrapper<AssessmentResponse>()
                        .eq(AssessmentResponse::getSessionId, session.getSessionId())
                        .eq(AssessmentResponse::getItemCode, answer.getItemCode()));

                AssessmentResponse response = new AssessmentResponse()
                        .setSessionId(session.getSessionId())
                        .setFormId(session.getFormId())
                        .setItemId(item.getItemId())
                        .setItemCode(item.getItemCode())
                        .setDisplayOrder(answer.getDisplayOrder() != null
                                ? answer.getDisplayOrder()
                                : assembly.itemDisplayOrders.get(item.getItemCode()))
                        .setRawAnswer(answer.getRawAnswer())
                        .setSelectedOptionCodes(toJsonOrNull(answer.getSelectedOptionCodes()))
                        .setSelectedOptionDisplayOrders(toJsonOrNull(resolveOptionDisplayOrders(
                                answer.getItemCode(), answer.getSelectedOptionCodes(), assembly.optionDisplayOrders)))
                        .setTextAnswer(answer.getTextAnswer())
                        .setAnsweredAt(answeredAt)
                        .setTimeSpentSeconds(answer.getTimeSpentSeconds())
                        .setIsSkipped(Boolean.TRUE.equals(answer.getIsSkipped()))
                        .setTenantId(session.getTenantId());
                responseMapper.insert(response);
            }
        }

        LocalDateTime completedAt = LocalDateTime.now();
        AssessmentSession update = new AssessmentSession()
                .setSessionId(session.getSessionId())
                .setStatus(SessionStatus.COMPLETED.getCode())
                .setCompletedAt(completedAt)
                .setElapsedSeconds(dto.getElapsedSeconds() != null ? dto.getElapsedSeconds() : session.getElapsedSeconds());
        sessionMapper.updateById(update);

        insertSnapshot(session, null, null, buildAnsweredCache(dto.getAnswers()), update.getElapsedSeconds());
    }

    private RuntimePayloadVO buildRuntimePayload(AssessmentPublish publish, AssessmentSession session) {
        RuntimeAssembly assembly = assembleRuntime(publish, session);
        AssessmentSessionSnapshot latestSnapshot = assembly.latestSnapshot;

        RuntimeSessionVO sessionVO = new RuntimeSessionVO()
                .setSessionId(session.getSessionId())
                .setPublishId(session.getPublishId())
                .setFormId(session.getFormId())
                .setVersionId(session.getVersionId())
                .setStatus(session.getStatus())
                .setDisplaySeed(session.getDisplaySeed())
                .setStartedAt(session.getStartedAt())
                .setElapsedSeconds(session.getElapsedSeconds())
                .setAttemptNumber(session.getAttemptNumber())
                .setCurrentSectionCode(latestSnapshot != null ? latestSnapshot.getCurrentSectionCode() : null)
                .setCurrentItemCode(latestSnapshot != null ? latestSnapshot.getCurrentItemCode() : null);

        List<RuntimeSectionVO> sectionVOS = new ArrayList<>();
        for (SectionAssembly sectionAssembly : assembly.sections) {
            List<RuntimeItemVO> itemVOS = new ArrayList<>();
            for (ItemAssembly itemAssembly : sectionAssembly.items) {
                List<RuntimeOptionVO> optionVOS = new ArrayList<>();
                for (int optionIndex = 0; optionIndex < itemAssembly.options.size(); optionIndex++) {
                    AssessmentOption option = itemAssembly.options.get(optionIndex);
                    optionVOS.add(new RuntimeOptionVO()
                            .setOptionCode(option.getOptionCode())
                            .setDisplayText(option.getDisplayText())
                            .setMediaUrl(option.getMediaUrl())
                            .setRawValue(option.getRawValue())
                            .setIsExclusive(option.getIsExclusive())
                            .setSortOrder(optionIndex + 1));
                }

                AssessmentItem item = itemAssembly.item;
                itemVOS.add(new RuntimeItemVO()
                        .setItemCode(item.getItemCode())
                        .setItemType(item.getItemType())
                        .setStem(item.getStem())
                        .setStemMediaUrl(item.getStemMediaUrl())
                        .setHelpText(item.getHelpText())
                        .setSortOrder(itemAssembly.displayOrder)
                        .setIsRequired(item.getIsRequired())
                        .setIsScored(item.getIsScored())
                        .setIsAnchor(item.getIsAnchor())
                        .setIsReverse(item.getIsReverse())
                        .setIsDemographic(item.getIsDemographic())
                        .setTimeLimitSeconds(item.getTimeLimitSeconds())
                        .setDemographicField(item.getDemographicField())
                        .setOptions(optionVOS));
            }

            AssessmentSection section = sectionAssembly.section;
            sectionVOS.add(new RuntimeSectionVO()
                    .setSectionCode(section.getSectionCode())
                    .setSectionTitle(section.getSectionTitle())
                    .setSectionDesc(section.getSectionDesc())
                    .setDisplayMode(section.getDisplayMode())
                    .setSortOrder(section.getSortOrder())
                    .setIsRequired(section.getIsRequired())
                    .setAnchorCode(section.getAnchorCode())
                    .setItems(itemVOS));
        }

        return new RuntimePayloadVO()
                .setPublish(toPublishVO(publish))
                .setSession(sessionVO)
                .setSections(sectionVOS)
                .setTotalItems(assembly.totalItems);
    }

    private RuntimeAssembly assembleRuntime(AssessmentPublish publish, AssessmentSession session) {
        RuntimeAssembly assembly = new RuntimeAssembly();
        assembly.latestSnapshot = getLatestSnapshot(session.getSessionId());

        List<AssessmentSection> sections = sectionMapper.selectList(new LambdaQueryWrapper<AssessmentSection>()
                .eq(AssessmentSection::getVersionId, session.getVersionId())
                .orderByAsc(AssessmentSection::getSortOrder, AssessmentSection::getSectionId));
        long seed = session.getDisplaySeed() != null ? session.getDisplaySeed() : 0L;

        for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++) {
            AssessmentSection section = sections.get(sectionIndex);
            String strategy = resolveRandomStrategy(section.getRandomStrategy(), publish.getRandomStrategy());
            List<AssessmentItem> items = new ArrayList<>(itemMapper.selectList(new LambdaQueryWrapper<AssessmentItem>()
                    .eq(AssessmentItem::getSectionId, section.getSectionId())
                    .orderByAsc(AssessmentItem::getSortOrder, AssessmentItem::getItemId)));
            if (shouldShuffleItems(strategy)) {
                items = shuffleItemsKeepAnchors(items, new Random(seed + sectionIndex));
            }

            SectionAssembly sectionAssembly = new SectionAssembly();
            sectionAssembly.section = section;
            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                AssessmentItem item = items.get(itemIndex);
                List<AssessmentOption> options = new ArrayList<>(optionMapper.selectList(new LambdaQueryWrapper<AssessmentOption>()
                        .eq(AssessmentOption::getItemId, item.getItemId())
                        .orderByAsc(AssessmentOption::getSortOrder, AssessmentOption::getOptionId)));
                if (shouldShuffleOptions(strategy)) {
                    options = shuffleList(options, new Random(seed + (sectionIndex + 1L) * 1000L + itemIndex));
                }

                ItemAssembly itemAssembly = new ItemAssembly();
                itemAssembly.item = item;
                itemAssembly.options = options;
                itemAssembly.displayOrder = assembly.totalItems + 1;
                sectionAssembly.items.add(itemAssembly);

                assembly.itemOrder.add(item.getItemCode());
                assembly.itemDisplayOrders.put(item.getItemCode(), itemAssembly.displayOrder);
                assembly.optionOrderCodes.put(item.getItemCode(), options.stream()
                        .map(AssessmentOption::getOptionCode)
                        .collect(Collectors.toCollection(ArrayList::new)));

                Map<String, Integer> optionDisplayOrderMap = new LinkedHashMap<>();
                for (int optionIndex = 0; optionIndex < options.size(); optionIndex++) {
                    optionDisplayOrderMap.put(options.get(optionIndex).getOptionCode(), optionIndex + 1);
                }
                assembly.optionDisplayOrders.put(item.getItemCode(), optionDisplayOrderMap);
                assembly.totalItems++;
            }
            assembly.sections.add(sectionAssembly);
        }
        return assembly;
    }

    private void insertSnapshot(
            AssessmentSession session,
            String currentSectionCode,
            String currentItemCode,
            String answeredCache,
            Integer elapsedSeconds) {
        AssessmentPublish publish = getPublishOrThrow(session.getPublishId());
        RuntimeAssembly assembly = assembleRuntime(publish, session);
        AssessmentSessionSnapshot latestSnapshot = assembly.latestSnapshot;

        AssessmentSessionSnapshot snapshot = new AssessmentSessionSnapshot()
                .setSessionId(session.getSessionId())
                .setCurrentSectionCode(StringUtils.hasText(currentSectionCode)
                        ? currentSectionCode
                        : latestSnapshot != null ? latestSnapshot.getCurrentSectionCode() : null)
                .setCurrentItemCode(StringUtils.hasText(currentItemCode)
                        ? currentItemCode
                        : latestSnapshot != null ? latestSnapshot.getCurrentItemCode() : null)
                .setItemOrderSnapshot(toJsonOrNull(assembly.itemOrder))
                .setOptionOrderSnapshot(toJsonOrNull(assembly.optionOrderCodes))
                .setAnsweredCache(StringUtils.hasText(answeredCache)
                        ? answeredCache
                        : latestSnapshot != null ? latestSnapshot.getAnsweredCache() : null)
                .setElapsedSeconds(elapsedSeconds)
                .setSnapshotAt(LocalDateTime.now())
                .setTenantId(session.getTenantId());
        sessionSnapshotMapper.insert(snapshot);
    }

    private AssessmentPublish getPublishOrThrow(Long publishId) {
        AssessmentPublish publish = publishMapper.selectById(publishId);
        if (publish == null) {
            throw BusinessException.of("发布不存在");
        }
        return publish;
    }

    private AssessmentSession getSessionOrThrow(Long sessionId) {
        AssessmentSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BusinessException.of("会话不存在");
        }
        return session;
    }

    private void validatePublished(AssessmentPublish publish) {
        if (!PublicationStatus.PUBLISHED.getCode().equals(publish.getStatus())) {
            throw BusinessException.of("发布未处于可作答状态");
        }
    }

    private void validatePublishActive(AssessmentPublish publish) {
        validatePublished(publish);
        LocalDateTime now = LocalDateTime.now();
        if (publish.getStartTime() != null && now.isBefore(publish.getStartTime())) {
            throw BusinessException.of("发布尚未开始");
        }
        if (publish.getEndTime() != null && now.isAfter(publish.getEndTime())) {
            throw BusinessException.of("发布已结束");
        }
    }

    private void validateAccessPolicy(AssessmentPublish publish, Long userId) {
        if (userId == null && !Boolean.TRUE.equals(publish.getAllowAnonymous())) {
            throw BusinessException.of("当前发布不允许匿名作答");
        }
    }

    private void validateSessionOwnership(AssessmentSession session, Long userId) {
        if (session.getUserId() != null && !Objects.equals(session.getUserId(), userId)) {
            throw BusinessException.of("无权访问当前会话");
        }
    }

    private void validateSessionInProgress(AssessmentSession session) {
        if (!SessionStatus.IN_PROGRESS.getCode().equals(session.getStatus())) {
            throw BusinessException.of("当前会话不允许执行该操作");
        }
    }

    private int countAttempts(Long publishId, Long userId, String anonymousToken) {
        if (userId == null && !StringUtils.hasText(anonymousToken)) {
            return 0;
        }

        LambdaQueryWrapper<AssessmentSession> wrapper = new LambdaQueryWrapper<AssessmentSession>()
                .eq(AssessmentSession::getPublishId, publishId);
        if (userId != null) {
            wrapper.eq(AssessmentSession::getUserId, userId);
        } else {
            wrapper.eq(AssessmentSession::getAnonymousToken, anonymousToken);
        }
        Long count = sessionMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    private AssessmentSession findInProgressSession(Long publishId, Long userId, String anonymousToken) {
        if (userId == null && !StringUtils.hasText(anonymousToken)) {
            return null;
        }

        LambdaQueryWrapper<AssessmentSession> wrapper = new LambdaQueryWrapper<AssessmentSession>()
                .eq(AssessmentSession::getPublishId, publishId)
                .eq(AssessmentSession::getStatus, SessionStatus.IN_PROGRESS.getCode())
                .orderByDesc(AssessmentSession::getStartedAt, AssessmentSession::getSessionId)
                .last("limit 1");
        if (userId != null) {
            wrapper.eq(AssessmentSession::getUserId, userId);
        } else {
            wrapper.eq(AssessmentSession::getAnonymousToken, anonymousToken);
        }
        return sessionMapper.selectOne(wrapper);
    }

    private AssessmentSessionSnapshot getLatestSnapshot(Long sessionId) {
        return sessionSnapshotMapper.selectOne(new LambdaQueryWrapper<AssessmentSessionSnapshot>()
                .eq(AssessmentSessionSnapshot::getSessionId, sessionId)
                .orderByDesc(AssessmentSessionSnapshot::getSnapshotAt, AssessmentSessionSnapshot::getSnapshotId)
                .last("limit 1"));
    }

    private void updateSessionElapsedSeconds(Long sessionId, Integer elapsedSeconds) {
        if (elapsedSeconds == null) {
            return;
        }
        AssessmentSession update = new AssessmentSession()
                .setSessionId(sessionId)
                .setElapsedSeconds(elapsedSeconds);
        sessionMapper.updateById(update);
    }

    private RuntimePublishVO toPublishVO(AssessmentPublish publish) {
        return new RuntimePublishVO()
                .setPublishId(publish.getPublishId())
                .setFormId(publish.getFormId())
                .setVersionId(publish.getVersionId())
                .setPublishCode(publish.getPublishCode())
                .setPublishName(publish.getPublishName())
                .setStatus(publish.getStatus())
                .setStartTime(publish.getStartTime())
                .setEndTime(publish.getEndTime())
                .setTimeLimitMinutes(publish.getTimeLimitMinutes())
                .setMaxAttempts(publish.getMaxAttempts())
                .setAllowAnonymous(publish.getAllowAnonymous())
                .setAllowResume(publish.getAllowResume())
                .setRandomStrategy(publish.getRandomStrategy())
                .setShowResultImmediately(publish.getShowResultImmediately());
    }

    private String resolveRandomStrategy(String sectionRandomStrategy, String publishRandomStrategy) {
        if (StringUtils.hasText(sectionRandomStrategy)) {
            return sectionRandomStrategy;
        }
        if (StringUtils.hasText(publishRandomStrategy)) {
            return publishRandomStrategy;
        }
        return RandomStrategy.NONE.getCode();
    }

    private boolean shouldShuffleItems(String randomStrategy) {
        return RandomStrategy.RANDOM_ITEMS.getCode().equals(randomStrategy)
                || RandomStrategy.RANDOM_BOTH.getCode().equals(randomStrategy);
    }

    private boolean shouldShuffleOptions(String randomStrategy) {
        return RandomStrategy.RANDOM_OPTIONS.getCode().equals(randomStrategy)
                || RandomStrategy.RANDOM_BOTH.getCode().equals(randomStrategy);
    }

    private List<AssessmentItem> shuffleItemsKeepAnchors(List<AssessmentItem> items, Random random) {
        if (CollectionUtils.isEmpty(items)) {
            return items;
        }

        List<AssessmentItem> movableItems = items.stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsAnchor()))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(movableItems, random);

        List<AssessmentItem> result = new ArrayList<>(items);
        Iterator<AssessmentItem> iterator = movableItems.iterator();
        for (int index = 0; index < items.size(); index++) {
            if (!Boolean.TRUE.equals(items.get(index).getIsAnchor()) && iterator.hasNext()) {
                result.set(index, iterator.next());
            }
        }
        return result;
    }

    private <T> List<T> shuffleList(List<T> source, Random random) {
        List<T> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }

    private List<Integer> resolveOptionDisplayOrders(
            String itemCode,
            List<String> selectedOptionCodes,
            Map<String, Map<String, Integer>> optionDisplayOrders) {
        if (CollectionUtils.isEmpty(selectedOptionCodes)) {
            return null;
        }

        Map<String, Integer> displayOrderMap = optionDisplayOrders.get(itemCode);
        if (CollectionUtils.isEmpty(displayOrderMap)) {
            return null;
        }

        List<Integer> displayOrders = new ArrayList<>();
        for (String optionCode : selectedOptionCodes) {
            Integer displayOrder = displayOrderMap.get(optionCode);
            if (displayOrder != null) {
                displayOrders.add(displayOrder);
            }
        }
        return displayOrders;
    }

    private String buildAnsweredCache(List<AnswerItemDTO> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return null;
        }

        Map<String, Object> answeredCache = new LinkedHashMap<>();
        for (AnswerItemDTO answer : answers) {
            if (StringUtils.hasText(answer.getRawAnswer())) {
                answeredCache.put(answer.getItemCode(), answer.getRawAnswer());
            } else if (!CollectionUtils.isEmpty(answer.getSelectedOptionCodes())) {
                answeredCache.put(answer.getItemCode(), answer.getSelectedOptionCodes());
            } else if (StringUtils.hasText(answer.getTextAnswer())) {
                answeredCache.put(answer.getItemCode(), answer.getTextAnswer());
            } else {
                answeredCache.put(answer.getItemCode(), null);
            }
        }
        return toJsonOrNull(answeredCache);
    }

    private String toJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return null;
        }
        if (value instanceof List<?> list && list.isEmpty()) {
            return null;
        }
        if (value instanceof Map<?, ?> map && map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("序列化运行时数据失败", e);
            throw BusinessException.of("运行时数据序列化失败", e);
        }
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private static class RuntimeAssembly {
        private final List<SectionAssembly> sections = new ArrayList<>();
        private final List<String> itemOrder = new ArrayList<>();
        private final Map<String, List<String>> optionOrderCodes = new LinkedHashMap<>();
        private final Map<String, Integer> itemDisplayOrders = new LinkedHashMap<>();
        private final Map<String, Map<String, Integer>> optionDisplayOrders = new LinkedHashMap<>();
        private AssessmentSessionSnapshot latestSnapshot;
        private int totalItems;
    }

    private static class SectionAssembly {
        private AssessmentSection section;
        private final List<ItemAssembly> items = new ArrayList<>();
    }

    private static class ItemAssembly {
        private AssessmentItem item;
        private List<AssessmentOption> options = new ArrayList<>();
        private Integer displayOrder;
    }
}
