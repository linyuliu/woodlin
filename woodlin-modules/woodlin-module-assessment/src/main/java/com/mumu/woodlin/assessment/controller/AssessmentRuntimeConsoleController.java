package com.mumu.woodlin.assessment.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mumu.woodlin.assessment.controller.AssessmentSchemaConsoleController.CompatDimension;
import com.mumu.woodlin.assessment.controller.AssessmentSchemaConsoleController.CompatIndicator;
import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaAggregateDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaDimensionBindingDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaDimensionDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaItemDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaSectionDTO;
import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.service.IAssessmentFormService;
import com.mumu.woodlin.assessment.service.IAssessmentPublishService;
import com.mumu.woodlin.assessment.service.IAssessmentSchemaService;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测评控制台兼容接口：发布/实例页
 *
 * @author yulin
 * @since 2026-05
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/assessment/runtime")
@Tag(name = "测评实例控制台", description = "兼容前端实例管理页的发布实例接口")
public class AssessmentRuntimeConsoleController {

    private final IAssessmentPublishService publishService;
    private final IAssessmentFormService formService;
    private final IAssessmentSchemaService schemaService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "分页查询测评实例")
    public R<PageResult<CompatRuntime>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long schemaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {
        List<AssessmentPublish> publishes = publishService.list(
                new LambdaQueryWrapper<AssessmentPublish>()
                        .eq(schemaId != null, AssessmentPublish::getFormId, schemaId)
                        .eq(status != null, AssessmentPublish::getStatus, toBackendRuntimeStatus(status))
                        .orderByDesc(AssessmentPublish::getCreateTime));
        List<CompatRuntime> records = publishes.stream()
                .map(this::toRuntimeSummary)
                .filter(item -> targetType == null || targetType.equals(item.getTargetType()))
                .toList();
        int pageNum = page == null ? 1 : page;
        int pageSize = size == null ? 10 : size;
        int fromIndex = Math.min((pageNum - 1) * pageSize, records.size());
        int toIndex = Math.min(fromIndex + pageSize, records.size());
        return R.ok(PageResult.success(
                (long) pageNum,
                (long) pageSize,
                (long) records.size(),
                records.subList(fromIndex, toIndex)
        ));
    }

    @GetMapping("/{publishId}")
    @Operation(summary = "查询测评实例详情")
    public R<CompatRuntime> detail(@PathVariable Long publishId) {
        return R.ok(toRuntimeDetail(requirePublish(publishId)));
    }

    @PostMapping
    @Operation(summary = "新增测评实例")
    public R<CompatRuntime> create(@Valid @RequestBody CompatRuntime request) {
        AssessmentForm form = requireForm(request.getSchemaId());
        if (form.getCurrentVersionId() == null) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, "测评方案尚未初始化版本");
        }
        AssessmentPublish publish = new AssessmentPublish();
        publish.setFormId(form.getFormId());
        publish.setVersionId(form.getCurrentVersionId());
        publish.setPublishCode("PUB_" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999));
        publish.setPublishName(form.getFormName() + "-" + request.getTargetType() + "-" + request.getTargetId());
        publish.setStatus("draft");
        publish.setStartTime(parseDateTime(request.getStartTime()));
        publish.setEndTime(parseDateTime(request.getEndTime()));
        publish.setAllowAnonymous(Boolean.FALSE);
        publish.setAllowResume(Boolean.TRUE);
        publish.setRandomStrategy("none");
        publish.setResultVisibility("self");
        publish.setAccessPolicy(writeTargetInfo(request.getTargetType(), request.getTargetId()));
        publish.setRemark(request.getRemark());
        ensureSuccess(publishService.save(publish), "新增测评实例失败");
        return R.ok(toRuntimeDetail(requirePublish(publish.getPublishId())));
    }

    @PutMapping("/{publishId}/submit")
    @Operation(summary = "提交测评实例")
    public R<Void> submit(@PathVariable Long publishId) {
        AssessmentPublish publish = requirePublish(publishId);
        publish.setStatus("published");
        ensureSuccess(publishService.updateById(publish), "提交测评实例失败");
        return R.ok();
    }

    @DeleteMapping("/{publishId}")
    @Operation(summary = "删除测评实例")
    public R<Void> delete(@PathVariable Long publishId) {
        ensureSuccess(publishService.removeById(publishId), "删除测评实例失败");
        return R.ok();
    }

    private CompatRuntime toRuntimeSummary(AssessmentPublish publish) {
        AssessmentForm form = formService.getById(publish.getFormId());
        Map<String, String> targetInfo = readTargetInfo(publish.getAccessPolicy());
        CompatRuntime runtime = new CompatRuntime();
        runtime.setId(publish.getPublishId());
        runtime.setSchemaId(publish.getFormId());
        runtime.setSchemaName(form == null ? null : form.getFormName());
        runtime.setTargetId(targetInfo.getOrDefault("targetId", ""));
        runtime.setTargetType(targetInfo.getOrDefault("targetType", "user"));
        runtime.setStatus(toFrontRuntimeStatus(publish.getStatus()));
        runtime.setStartTime(publish.getStartTime() == null ? null : publish.getStartTime().toString().replace("T", " "));
        runtime.setEndTime(publish.getEndTime() == null ? null : publish.getEndTime().toString().replace("T", " "));
        runtime.setRemark(publish.getRemark());
        runtime.setDimensions(List.of());
        return runtime;
    }

    private CompatRuntime toRuntimeDetail(AssessmentPublish publish) {
        CompatRuntime runtime = toRuntimeSummary(publish);
        if (publish.getVersionId() != null) {
            AssessmentSchemaAggregateDTO aggregate = schemaService.getAggregate(publish.getVersionId());
            runtime.setDimensions(toCompatDimensions(aggregate));
        }
        return runtime;
    }

    private List<CompatDimension> toCompatDimensions(AssessmentSchemaAggregateDTO aggregate) {
        Map<String, CompatDimension> dimensions = new LinkedHashMap<>();
        AtomicInteger dimensionIndex = new AtomicInteger(1);
        for (SchemaDimensionDTO item : aggregate.getDimensions()) {
            CompatDimension dimension = new CompatDimension();
            dimension.setId((long) dimensionIndex.getAndIncrement());
            dimension.setDimName(item.getDimensionName());
            dimension.setWeight(item.getSortOrder() == null ? 0D : item.getSortOrder().doubleValue());
            dimension.setIndicators(new ArrayList<>());
            dimensions.put(item.getDimensionCode(), dimension);
        }
        for (SchemaSectionDTO section : aggregate.getSections()) {
            for (SchemaItemDTO item : section.getItems()) {
                for (SchemaDimensionBindingDTO binding : item.getDimensionBindings()) {
                    CompatDimension dimension = dimensions.get(binding.getDimensionCode());
                    if (dimension == null) {
                        continue;
                    }
                    CompatIndicator indicator = new CompatIndicator();
                    indicator.setIndName(item.getStem());
                    indicator.setIndDesc(item.getHelpText());
                    indicator.setWeight(binding.getWeight() == null ? 0D : binding.getWeight().doubleValue());
                    indicator.setScoreType("rating".equalsIgnoreCase(item.getItemType()) ? "100" : "custom");
                    indicator.setScore(item.getMaxScore() == null ? null : item.getMaxScore().doubleValue());
                    dimension.getIndicators().add(indicator);
                }
            }
        }
        return new ArrayList<>(dimensions.values());
    }

    private String writeTargetInfo(String targetType, String targetId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "targetType", targetType == null ? "user" : targetType,
                    "targetId", targetId == null ? "" : targetId
            ));
        } catch (Exception exception) {
            return "{\"targetType\":\"user\",\"targetId\":\"\"}";
        }
    }

    private Map<String, String> readTargetInfo(String accessPolicy) {
        if (accessPolicy == null || accessPolicy.isBlank()) {
            return Map.of("targetType", "user", "targetId", "");
        }
        try {
            return objectMapper.readValue(accessPolicy, new TypeReference<>() {
            });
        } catch (Exception exception) {
            return Map.of("targetType", "user", "targetId", "");
        }
    }

    private String toBackendRuntimeStatus(String status) {
        if ("2".equals(status)) {
            return "closed";
        }
        if ("1".equals(status)) {
            return "published";
        }
        return "draft";
    }

    private String toFrontRuntimeStatus(String status) {
        if ("closed".equalsIgnoreCase(status) || "archived".equalsIgnoreCase(status)) {
            return "2";
        }
        if ("published".equalsIgnoreCase(status)
                || "paused".equalsIgnoreCase(status)
                || "under_review".equalsIgnoreCase(status)) {
            return "1";
        }
        return "0";
    }

    private AssessmentPublish requirePublish(Long publishId) {
        AssessmentPublish publish = publishService.getById(publishId);
        if (publish == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "测评实例不存在");
        }
        return publish;
    }

    private AssessmentForm requireForm(Long formId) {
        AssessmentForm form = formService.getById(formId);
        if (form == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "测评方案不存在");
        }
        return form;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.replace(" ", "T"));
    }

    private void ensureSuccess(boolean result, String failureMessage) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, failureMessage);
        }
    }

    @Data
    public static class CompatRuntime {
        private Long id;
        private Long schemaId;
        private String schemaName;
        private String targetId;
        private String targetType;
        private String status;
        private String startTime;
        private String endTime;
        private Double totalScore;
        private String remark;
        private List<CompatDimension> dimensions;
    }
}
