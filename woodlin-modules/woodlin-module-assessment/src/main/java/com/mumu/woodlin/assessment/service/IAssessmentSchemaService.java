package com.mumu.woodlin.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaAggregateDTO;
import com.mumu.woodlin.assessment.model.dto.schema.AssessmentSchemaCompileResultDTO;
import com.mumu.woodlin.assessment.model.entity.AssessmentSchema;
import com.mumu.woodlin.assessment.validation.ValidationReport;

/**
 * 测评 Schema 服务接口
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentSchemaService extends IService<AssessmentSchema> {

    /**
     * 获取版本的结构化 Schema。
     *
     * @param versionId 版本ID
     * @return 结构化 Schema
     */
    AssessmentSchemaAggregateDTO getAggregate(Long versionId);

    /**
     * 保存结构化 Schema 草稿。
     *
     * @param versionId 版本ID
     * @param aggregate 结构化 Schema
     * @return 保存后的结构化 Schema
     */
    AssessmentSchemaAggregateDTO saveAggregate(Long versionId, AssessmentSchemaAggregateDTO aggregate);

    /**
     * 编译结构化 Schema，并重建运行时投影表。
     *
     * @param versionId 版本ID
     * @return 编译结果
     */
    AssessmentSchemaCompileResultDTO compileVersion(Long versionId);

    /**
     * 校验版本 Schema。
     *
     * @param versionId 版本ID
     * @return 校验报告
     */
    ValidationReport validateVersion(Long versionId);

    /**
     * 导入专家模式 DSL/JSON。
     *
     * @param versionId 版本ID
     * @param dslSource DSL/JSON 源码
     * @return 导入后的结构化 Schema
     */
    AssessmentSchemaAggregateDTO importDsl(Long versionId, String dslSource);

    /**
     * 导出专家模式 DSL/JSON。
     *
     * @param versionId 版本ID
     * @return DSL/JSON 源码
     */
    String exportDsl(Long versionId);
}
