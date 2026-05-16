package com.mumu.woodlin.assessment.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaAggregateDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaDimensionBindingDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaDimensionDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaItemDTO;
import com.mumu.woodlin.assessment.model.dto.schema.SchemaSectionDTO;
import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.model.query.AssessmentFormQuery;
import com.mumu.woodlin.assessment.service.IAssessmentFormService;
import com.mumu.woodlin.assessment.service.IAssessmentFormVersionService;
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
import org.springframework.util.StringUtils;

/**
 * 测评控制台兼容接口：方案页
 *
 * @author yulin
 * @since 2026-05
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/assessment/schema")
@Tag(name = "测评方案控制台", description = "兼容前端方案管理页的测评方案接口")
public class AssessmentSchemaConsoleController {

    private final IAssessmentFormService formService;
    private final IAssessmentFormVersionService versionService;
    private final IAssessmentSchemaService schemaService;

    @GetMapping
    @Operation(summary = "分页查询测评方案")
    public R<PageResult<CompatSchema>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String schemaName,
            @RequestParam(required = false) String status) {
        AssessmentFormQuery query = new AssessmentFormQuery();
        query.setPageNum(page == null ? 1 : page);
        query.setPageSize(size == null ? 10 : size);
        query.setFormName(schemaName);
        query.setStatus(toBackendFormStatus(status));

        PageResult<AssessmentForm> result = formService.pageList(query);
        List<CompatSchema> records = result.getData().stream().map(this::toSchemaSummary).toList();
        return R.ok(PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    @GetMapping("/{formId}")
    @Operation(summary = "查询测评方案详情")
    public R<CompatSchema> detail(@PathVariable Long formId) {
        return R.ok(toSchemaDetail(requireForm(formId)));
    }

    @PostMapping
    @Operation(summary = "新增测评方案")
    public R<CompatSchema> create(@Valid @RequestBody CompatSchema request) {
        AssessmentForm form = new AssessmentForm();
        fillForm(form, request);
        ensureSuccess(formService.save(form), "新增测评方案失败");

        AssessmentFormVersion version = createVersion(form.getFormId(), "1.0.0", "初稿");
        form.setCurrentVersionId(version.getVersionId());
        ensureSuccess(formService.updateById(form), "初始化测评版本失败");

        AssessmentSchemaAggregateDTO aggregate = buildAggregate(request, form.getFormId(), version.getVersionId());
        schemaService.saveAggregate(version.getVersionId(), aggregate);
        return R.ok(toSchemaDetail(requireForm(form.getFormId())));
    }

    @PutMapping("/{formId}")
    @Operation(summary = "修改测评方案")
    public R<CompatSchema> update(@PathVariable Long formId, @Valid @RequestBody CompatSchema request) {
        AssessmentForm form = requireForm(formId);
        fillForm(form, request);
        ensureSuccess(formService.updateById(form), "修改测评方案失败");

        AssessmentFormVersion version = ensureCurrentVersion(form);
        AssessmentSchemaAggregateDTO aggregate = buildAggregate(request, formId, version.getVersionId());
        schemaService.saveAggregate(version.getVersionId(), aggregate);
        return R.ok(toSchemaDetail(requireForm(formId)));
    }

    @DeleteMapping("/{formId}")
    @Operation(summary = "删除测评方案")
    public R<Void> delete(@PathVariable Long formId) {
        requireForm(formId);
        List<AssessmentFormVersion> versions = versionService.list(
                new LambdaQueryWrapper<AssessmentFormVersion>().eq(AssessmentFormVersion::getFormId, formId));
        List<Long> versionIds = versions.stream().map(AssessmentFormVersion::getVersionId).toList();
        if (!versionIds.isEmpty()) {
            schemaService.remove(new LambdaQueryWrapper<com.mumu.woodlin.assessment.model.entity.AssessmentSchema>()
                    .in(com.mumu.woodlin.assessment.model.entity.AssessmentSchema::getVersionId, versionIds));
        }
        versionService.remove(new LambdaQueryWrapper<AssessmentFormVersion>().eq(AssessmentFormVersion::getFormId, formId));
        ensureSuccess(formService.removeById(formId), "删除测评方案失败");
        return R.ok();
    }

    private CompatSchema toSchemaSummary(AssessmentForm form) {
        CompatSchema schema = new CompatSchema();
        schema.setId(form.getFormId());
        schema.setSchemaName(form.getFormName());
        schema.setSchemaDesc(form.getDescription());
        schema.setStatus(toFrontFormStatus(form.getStatus()));
        schema.setCreateTime(form.getCreateTime() == null ? null : form.getCreateTime().toString());
        schema.setDimensionCount(countDimensions(form.getCurrentVersionId()));
        schema.setDimensions(List.of());
        return schema;
    }

    private CompatSchema toSchemaDetail(AssessmentForm form) {
        CompatSchema schema = toSchemaSummary(form);
        AssessmentFormVersion version = ensureCurrentVersion(form);
        AssessmentSchemaAggregateDTO aggregate = schemaService.getAggregate(version.getVersionId());
        schema.setDimensions(toCompatDimensions(aggregate));
        schema.setDimensionCount(schema.getDimensions().size());
        return schema;
    }

    private List<CompatDimension> toCompatDimensions(AssessmentSchemaAggregateDTO aggregate) {
        Map<String, CompatDimension> dimensions = new LinkedHashMap<>();
        AtomicInteger dimensionIndex = new AtomicInteger(1);
        for (SchemaDimensionDTO item : aggregate.getDimensions()) {
            String code = item.getDimensionCode();
            CompatDimension dimension = new CompatDimension();
            dimension.setId((long) dimensionIndex.getAndIncrement());
            dimension.setDimName(item.getDimensionName());
            dimension.setWeight(item.getSortOrder() == null ? 0D : item.getSortOrder().doubleValue());
            dimension.setIndicators(new ArrayList<>());
            dimensions.put(code, dimension);
        }
        for (SchemaSectionDTO section : aggregate.getSections()) {
            for (SchemaItemDTO item : section.getItems()) {
                List<SchemaDimensionBindingDTO> bindings = item.getDimensionBindings();
                if (bindings == null || bindings.isEmpty()) {
                    CompatDimension dimension = dimensions.computeIfAbsent("DEFAULT", key -> {
                        CompatDimension created = new CompatDimension();
                        created.setId((long) dimensionIndex.getAndIncrement());
                        created.setDimName("默认维度");
                        created.setWeight(0D);
                        created.setIndicators(new ArrayList<>());
                        return created;
                    });
                    dimension.getIndicators().add(toCompatIndicator(item, null));
                    continue;
                }
                for (SchemaDimensionBindingDTO binding : bindings) {
                    CompatDimension dimension = dimensions.computeIfAbsent(binding.getDimensionCode(), key -> {
                        CompatDimension created = new CompatDimension();
                        created.setId((long) dimensionIndex.getAndIncrement());
                        created.setDimName(binding.getDimensionCode());
                        created.setWeight(0D);
                        created.setIndicators(new ArrayList<>());
                        return created;
                    });
                    dimension.getIndicators().add(toCompatIndicator(item, binding));
                }
            }
        }
        return new ArrayList<>(dimensions.values());
    }

    private CompatIndicator toCompatIndicator(SchemaItemDTO item, SchemaDimensionBindingDTO binding) {
        CompatIndicator indicator = new CompatIndicator();
        indicator.setIndName(item.getStem());
        indicator.setIndDesc(item.getHelpText());
        indicator.setWeight(binding != null && binding.getWeight() != null ? binding.getWeight().doubleValue() : 0D);
        indicator.setScoreType(mapScoreType(item.getItemType()));
        indicator.setScore(item.getMaxScore() == null ? null : item.getMaxScore().doubleValue());
        return indicator;
    }

    private AssessmentSchemaAggregateDTO buildAggregate(CompatSchema request, Long formId, Long versionId) {
        AssessmentSchemaAggregateDTO aggregate = new AssessmentSchemaAggregateDTO();
        aggregate.setFormId(formId);
        aggregate.setVersionId(versionId);
        aggregate.setAssessmentType("survey");
        aggregate.setDescription(request.getSchemaDesc());
        aggregate.setDimensions(new ArrayList<>());

        SchemaSectionDTO section = new SchemaSectionDTO();
        section.setSectionCode("MAIN");
        section.setSectionTitle(request.getSchemaName());
        section.setSortOrder(1);
        section.setItems(new ArrayList<>());

        AtomicInteger dimensionIndex = new AtomicInteger(1);
        AtomicInteger itemIndex = new AtomicInteger(1);
        for (CompatDimension compatDimension : defaultDimensions(request.getDimensions())) {
            String dimensionCode = "DIM_" + dimensionIndex.getAndIncrement();
            SchemaDimensionDTO dimension = new SchemaDimensionDTO();
            dimension.setDimensionCode(dimensionCode);
            dimension.setDimensionName(compatDimension.getDimName());
            dimension.setDimensionDesc(compatDimension.getDimName());
            dimension.setSortOrder(compatDimension.getWeight() == null ? 0 : compatDimension.getWeight().intValue());
            dimension.setScoreMode("sum");
            aggregate.getDimensions().add(dimension);

            for (CompatIndicator compatIndicator : compatDimension.getIndicators()) {
                SchemaItemDTO item = new SchemaItemDTO();
                item.setItemCode("ITEM_" + itemIndex.getAndIncrement());
                item.setItemType("rating");
                item.setStem(compatIndicator.getIndName());
                item.setHelpText(compatIndicator.getIndDesc());
                item.setSortOrder(itemIndex.get());
                item.setIsRequired(Boolean.FALSE);
                item.setIsScored(Boolean.TRUE);
                item.setMaxScore(compatIndicator.getScore() == null ? null : BigDecimal.valueOf(compatIndicator.getScore()));
                item.setDimensionBindings(List.of(new SchemaDimensionBindingDTO()
                        .setDimensionCode(dimensionCode)
                        .setWeight(BigDecimal.valueOf(compatIndicator.getWeight() == null ? 0D : compatIndicator.getWeight()))));
                section.getItems().add(item);
            }
        }
        aggregate.setSections(List.of(section));
        return aggregate;
    }

    private List<CompatDimension> defaultDimensions(List<CompatDimension> dimensions) {
        if (dimensions != null && !dimensions.isEmpty()) {
            return dimensions;
        }
        CompatDimension dimension = new CompatDimension();
        dimension.setDimName("默认维度");
        dimension.setWeight(0D);
        dimension.setIndicators(new ArrayList<>());
        return List.of(dimension);
    }

    private AssessmentFormVersion ensureCurrentVersion(AssessmentForm form) {
        if (form.getCurrentVersionId() != null) {
            AssessmentFormVersion version = versionService.getById(form.getCurrentVersionId());
            if (version != null) {
                return version;
            }
        }
        AssessmentFormVersion version = createVersion(form.getFormId(), "1.0." + ThreadLocalRandom.current().nextInt(1, 1000), "自动补齐");
        form.setCurrentVersionId(version.getVersionId());
        ensureSuccess(formService.updateById(form), "补齐测评版本失败");
        return version;
    }

    private AssessmentFormVersion createVersion(Long formId, String versionNo, String versionTag) {
        AssessmentFormVersion version = new AssessmentFormVersion();
        version.setFormId(formId);
        version.setVersionNo(versionNo);
        version.setVersionTag(versionTag);
        version.setStatus("draft");
        ensureSuccess(versionService.save(version), "创建测评版本失败");
        return version;
    }

    private Integer countDimensions(Long versionId) {
        if (versionId == null) {
            return 0;
        }
        try {
            return schemaService.getAggregate(versionId).getDimensions().size();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private AssessmentForm requireForm(Long formId) {
        AssessmentForm form = formService.getById(formId);
        if (form == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND, "测评方案不存在");
        }
        return form;
    }

    private void fillForm(AssessmentForm form, CompatSchema request) {
        form.setFormName(request.getSchemaName());
        form.setDescription(request.getSchemaDesc());
        form.setStatus(toBackendFormStatus(request.getStatus()));
        form.setAssessmentType("survey");
        form.setFormCode(StringUtils.hasText(form.getFormCode())
                ? form.getFormCode()
                : "FORM_" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999));
        form.setSortOrder(0);
    }

    private int toBackendFormStatus(String status) {
        return "1".equals(status) ? 0 : 1;
    }

    private String toFrontFormStatus(Integer status) {
        return status != null && status == 1 ? "0" : "1";
    }

    private String mapScoreType(String itemType) {
        if ("rating".equalsIgnoreCase(itemType)) {
            return "100";
        }
        return "custom";
    }

    private void ensureSuccess(boolean result, String failureMessage) {
        if (!result) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR, failureMessage);
        }
    }

    @Data
    public static class CompatSchema {
        private Long id;
        private String schemaName;
        private String schemaDesc;
        private String status;
        private List<CompatDimension> dimensions;
        private Integer dimensionCount;
        private String createTime;
    }

    @Data
    public static class CompatDimension {
        private Long id;
        private String dimName;
        private Double weight;
        private List<CompatIndicator> indicators;
        private Double score;
    }

    @Data
    public static class CompatIndicator {
        private String indName;
        private String indDesc;
        private Double weight;
        private String scoreType;
        private Double score;
    }
}
