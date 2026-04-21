package com.mumu.woodlin.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.model.query.AssessmentFormQuery;
import com.mumu.woodlin.common.response.PageResult;

/**
 * AssessmentForm 服务接口
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentFormService extends IService<AssessmentForm> {

    /**
     * 分页查询测评主体列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AssessmentForm> pageList(AssessmentFormQuery query);

    /**
     * 更新测评状态
     *
     * @param formId 测评ID
     * @param status 状态
     */
    void updateStatus(Long formId, Integer status);
}
