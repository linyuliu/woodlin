package com.mumu.woodlin.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.assessment.model.entity.AssessmentNormSet;
import com.mumu.woodlin.assessment.model.query.AssessmentNormSetQuery;
import com.mumu.woodlin.common.response.PageResult;

/**
 * AssessmentNormSet 服务接口
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentNormSetService extends IService<AssessmentNormSet> {

    /**
     * 分页查询常模集列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AssessmentNormSet> pageList(AssessmentNormSetQuery query);
}
