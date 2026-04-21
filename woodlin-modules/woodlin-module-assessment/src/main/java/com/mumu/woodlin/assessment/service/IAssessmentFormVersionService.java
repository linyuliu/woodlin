package com.mumu.woodlin.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.model.query.AssessmentFormVersionQuery;
import com.mumu.woodlin.common.response.PageResult;

/**
 * AssessmentFormVersion 服务接口
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentFormVersionService extends IService<AssessmentFormVersion> {

    /**
     * 分页查询版本列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AssessmentFormVersion> pageList(AssessmentFormVersionQuery query);
}
