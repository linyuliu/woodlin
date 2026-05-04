package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.assessment.enums.PublicationStatus;
import com.mumu.woodlin.assessment.enums.RandomStrategy;
import com.mumu.woodlin.assessment.enums.SessionStatus;
import com.mumu.woodlin.assessment.mapper.*;
import com.mumu.woodlin.assessment.model.dto.AnswerItemDTO;
import com.mumu.woodlin.assessment.model.dto.SaveSnapshotDTO;
import com.mumu.woodlin.assessment.model.dto.StartSessionDTO;
import com.mumu.woodlin.assessment.model.dto.SubmitAnswersDTO;
import com.mumu.woodlin.assessment.model.entity.*;
import com.mumu.woodlin.assessment.model.vo.*;
import com.mumu.woodlin.assessment.service.IAssessmentRuntimeService;
import com.mumu.woodlin.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final AssessmentDemographicProfileMapper demographicProfileMapper;
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
        insertSnapshot(session, null, null, null, 0);
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
                    .setDisplayOrder(assembly.itemDisplayOrders.get(item.getItemCode()))
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

        Map<String, AnswerItemDTO> answerMap = toAnswerMap(dto.getAnswers());
        persistDemographicProfile(session, itemMap, answerMap);

        LocalDateTime completedAt = LocalDateTime.now();
        AssessmentSession update = new AssessmentSession()
                .setSessionId(session.getSessionId())
                .setStatus(SessionStatus.COMPLETED.getCode())
                .setCompletedAt(completedAt)
                .setElapsedSeconds(dto.getElapsedSeconds() != null ? dto.getElapsedSeconds() : session.getElapsedSeconds());
        sessionMapper.updateById(update);

        insertSnapshot(session, null, null, buildAnsweredCache(answerMap), update.getElapsedSeconds());
    }

    private RuntimePayloadVO buildRuntimePayload(AssessmentPublish publish, AssessmentSession session) {
        RuntimeAssembly assembly = assembleRuntime(publish, session);
        AssessmentSessionSnapshot latestSnapshot = assembly.latestSnapshot;
        Map<String, AnswerItemDTO> answerSnapshot = parseAnswerSnapshot(latestSnapshot != null
            ? latestSnapshot.getAnsweredCache()
            : null);

        RuntimeSessionVO sessionVO = new RuntimeSessionVO()
                .setSessionId(session.getSessionId())
                .setPublishId(session.getPublishId())
                .setFormId(session.getFormId())
                .setVersionId(session.getVersionId())
                .setStatus(session.getStatus())
                .setDisplaySeed(session.getDisplaySeed())
                .setStartedAt(session.getStartedAt())
            .setElapsedSeconds(latestSnapshot != null && latestSnapshot.getElapsedSeconds() != null
                ? latestSnapshot.getElapsedSeconds()
                : session.getElapsedSeconds())
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
            .setTotalItems(assembly.totalItems)
            .setAnswerSnapshot(answerSnapshot);
    }

    private RuntimeAssembly assembleRuntime(AssessmentPublish publish, AssessmentSession session) {
        RuntimeAssembly assembly = new RuntimeAssembly();
        assembly.latestSnapshot = getLatestSnapshot(session.getSessionId());
        List<String> snapshotItemOrder = parseStringList(assembly.latestSnapshot != null
            ? assembly.latestSnapshot.getItemOrderSnapshot()
            : null);
        Map<String, Integer> snapshotItemOrderIndex = new LinkedHashMap<>();
        for (int index = 0; index < snapshotItemOrder.size(); index++) {
            snapshotItemOrderIndex.put(snapshotItemOrder.get(index), index);
        }
        Map<String, List<String>> snapshotOptionOrders = parseOptionOrderSnapshot(assembly.latestSnapshot != null
            ? assembly.latestSnapshot.getOptionOrderSnapshot()
            : null);

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
            if (!snapshotItemOrderIndex.isEmpty()) {
                items.sort(Comparator.comparing(item -> snapshotItemOrderIndex.getOrDefault(item.getItemCode(), Integer.MAX_VALUE)));
            } else if (shouldShuffleItems(strategy)) {
                items = shuffleItemsKeepAnchors(items, new Random(seed + sectionIndex));
            }

            SectionAssembly sectionAssembly = new SectionAssembly();
            sectionAssembly.section = section;
            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                AssessmentItem item = items.get(itemIndex);
                List<AssessmentOption> options = new ArrayList<>(optionMapper.selectList(new LambdaQueryWrapper<AssessmentOption>()
                        .eq(AssessmentOption::getItemId, item.getItemId())
                        .orderByAsc(AssessmentOption::getSortOrder, AssessmentOption::getOptionId)));
                if (snapshotOptionOrders.containsKey(item.getItemCode())) {
                    options = reorderOptionsBySnapshot(options, snapshotOptionOrders.get(item.getItemCode()));
                } else if (shouldShuffleOptions(strategy)) {
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

    private List<AssessmentOption> reorderOptionsBySnapshot(List<AssessmentOption> options, List<String> optionCodes) {
        if (CollectionUtils.isEmpty(options) || CollectionUtils.isEmpty(optionCodes)) {
            return options;
        }
        Map<String, Integer> orderIndex = new LinkedHashMap<>();
        for (int index = 0; index < optionCodes.size(); index++) {
            orderIndex.put(optionCodes.get(index), index);
        }
        return options.stream()
            .sorted(Comparator.comparing(option -> orderIndex.getOrDefault(option.getOptionCode(), Integer.MAX_VALUE)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<String, List<String>> parseOptionOrderSnapshot(String optionOrderSnapshot) {
        if (!StringUtils.hasText(optionOrderSnapshot)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(optionOrderSnapshot, new TypeReference<LinkedHashMap<String, List<String>>>() {
            });
        } catch (Exception exception) {
            log.warn("解析选项顺序快照失败，将退回种子乱序", exception);
            return new LinkedHashMap<>();
        }
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception exception) {
            log.warn("解析题目顺序快照失败，将退回种子乱序", exception);
            return List.of();
        }
    }

    private Map<String, AnswerItemDTO> parseAnswerSnapshot(String answeredCache) {
        if (!StringUtils.hasText(answeredCache)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, AnswerItemDTO> snapshot = objectMapper.readValue(
                answeredCache,
                new TypeReference<LinkedHashMap<String, AnswerItemDTO>>() {
                });
            snapshot.values().forEach(answer -> {
                if (!StringUtils.hasText(answer.getItemCode())) {
                    answer.setItemCode(resolveAnswerItemCode(snapshot, answer));
                }
            });
            return snapshot;
        } catch (Exception exception) {
            log.warn("解析答案快照失败，将返回空快照", exception);
            return new LinkedHashMap<>();
        }
    }

    private String resolveAnswerItemCode(Map<String, AnswerItemDTO> snapshot, AnswerItemDTO answer) {
        for (Map.Entry<String, AnswerItemDTO> entry : snapshot.entrySet()) {
            if (entry.getValue() == answer) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Map<String, AnswerItemDTO> toAnswerMap(List<AnswerItemDTO> answers) {
        Map<String, AnswerItemDTO> answerMap = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(answers)) {
            return answerMap;
        }
        for (AnswerItemDTO answer : answers) {
            if (!StringUtils.hasText(answer.getItemCode())) {
                continue;
            }
            answerMap.put(answer.getItemCode(), new AnswerItemDTO()
                .setItemCode(answer.getItemCode())
                .setRawAnswer(answer.getRawAnswer())
                .setSelectedOptionCodes(answer.getSelectedOptionCodes())
                .setTextAnswer(answer.getTextAnswer())
                .setTimeSpentSeconds(answer.getTimeSpentSeconds())
                .setIsSkipped(answer.getIsSkipped()));
        }
        return answerMap;
    }

    private void persistDemographicProfile(
        AssessmentSession session,
        Map<String, AssessmentItem> itemMap,
        Map<String, AnswerItemDTO> answerMap) {
        if (CollectionUtils.isEmpty(answerMap)) {
            return;
        }

        AssessmentDemographicProfile profile = new AssessmentDemographicProfile()
            .setSessionId(session.getSessionId())
            .setUserId(session.getUserId())
            .setTenantId(session.getTenantId())
            .setNormWeight(BigDecimal.ONE);
        Map<String, Object> extraFields = new LinkedHashMap<>();

        answerMap.forEach((itemCode, answer) -> {
            AssessmentItem item = itemMap.get(itemCode);
            if (item == null || !Boolean.TRUE.equals(item.getIsDemographic()) || Boolean.TRUE.equals(answer.getIsSkipped())) {
                return;
            }
            Object rawValue = extractAnswerValue(answer);
            if (rawValue == null) {
                return;
            }
            String field = normalizeDemographicField(item.getDemographicField(), itemCode);
            String textValue = rawValue instanceof List<?> list && !list.isEmpty() ? String.valueOf(list.get(0)) : String.valueOf(rawValue);
            switch (field) {
                case "gender" -> profile.setGender(textValue);
                case "birth_year" -> profile.setBirthYear(parseInteger(textValue));
                case "age" -> profile.setAge(parseInteger(textValue));
                case "age_group" -> profile.setAgeGroup(textValue);
                case "education_level" -> profile.setEducationLevel(textValue);
                case "occupation" -> profile.setOccupation(textValue);
                case "region_code" -> profile.setRegionCode(textValue);
                case "province_code" -> profile.setProvinceCode(textValue);
                case "marital_status" -> profile.setMaritalStatus(textValue);
                case "ethnicity" -> profile.setEthnicity(textValue);
                default -> extraFields.put(field, rawValue);
            }
        });

        if (!hasDemographicContent(profile, extraFields)) {
            return;
        }
        profile.setExtraFields(toJsonOrNull(extraFields));
        demographicProfileMapper.delete(new LambdaQueryWrapper<AssessmentDemographicProfile>()
            .eq(AssessmentDemographicProfile::getSessionId, session.getSessionId()));
        demographicProfileMapper.insert(profile);
    }

    private boolean hasDemographicContent(AssessmentDemographicProfile profile, Map<String, Object> extraFields) {
        return StringUtils.hasText(profile.getGender())
            || profile.getBirthYear() != null
            || profile.getAge() != null
            || StringUtils.hasText(profile.getAgeGroup())
            || StringUtils.hasText(profile.getEducationLevel())
            || StringUtils.hasText(profile.getOccupation())
            || StringUtils.hasText(profile.getRegionCode())
            || StringUtils.hasText(profile.getProvinceCode())
            || StringUtils.hasText(profile.getMaritalStatus())
            || StringUtils.hasText(profile.getEthnicity())
            || !CollectionUtils.isEmpty(extraFields);
    }

    private Object extractAnswerValue(AnswerItemDTO answer) {
        if (!CollectionUtils.isEmpty(answer.getSelectedOptionCodes())) {
            return answer.getSelectedOptionCodes().size() == 1
                ? answer.getSelectedOptionCodes().get(0)
                : answer.getSelectedOptionCodes();
        }
        if (StringUtils.hasText(answer.getTextAnswer())) {
            return answer.getTextAnswer();
        }
        if (!StringUtils.hasText(answer.getRawAnswer()) || "null".equals(answer.getRawAnswer())) {
            return null;
        }
        try {
            return objectMapper.readValue(answer.getRawAnswer(), Object.class);
        } catch (Exception exception) {
            return answer.getRawAnswer();
        }
    }

    private String normalizeDemographicField(String demographicField, String itemCode) {
        if (!StringUtils.hasText(demographicField)) {
            return itemCode;
        }
        return switch (demographicField.toLowerCase()) {
            case "education" -> "education_level";
            case "region" -> "region_code";
            case "province" -> "province_code";
            default -> demographicField.toLowerCase();
        };
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String buildAnsweredCache(Map<String, AnswerItemDTO> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return null;
        }
        return toJsonOrNull(answers);
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
