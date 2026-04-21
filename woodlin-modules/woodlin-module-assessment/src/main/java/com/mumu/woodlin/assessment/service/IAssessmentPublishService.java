package com.mumu.woodlin.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.query.AssessmentPublishQuery;
import com.mumu.woodlin.common.response.PageResult;

/**
 * AssessmentPublish 服务接口
 *
 * @author mumu
 * @since 2025-01-01
 */
public interface IAssessmentPublishService extends IService<AssessmentPublish> {

    /**
     * 分页查询发布实例列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AssessmentPublish> pageList(AssessmentPublishQuery query);

    /**
     * 更新发布状态
     *
     * @param publishId 发布ID
     * @param status    状态
     */
    void updateStatus(Long publishId, String status);
}
