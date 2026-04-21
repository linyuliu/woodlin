package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mumu.woodlin.assessment.mapper.AssessmentFormMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.model.query.AssessmentFormQuery;
import com.mumu.woodlin.assessment.service.IAssessmentFormService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.util.PageUtil;

/**
 * AssessmentForm 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentFormServiceImpl extends ServiceImpl<AssessmentFormMapper, AssessmentForm>
        implements IAssessmentFormService {

    @Override
    public PageResult<AssessmentForm> pageList(AssessmentFormQuery query) {
        LambdaQueryWrapper<AssessmentForm> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getFormName())) {
            wrapper.like(AssessmentForm::getFormName, query.getFormName());
        }
        if (StringUtils.hasText(query.getFormCode())) {
            wrapper.eq(AssessmentForm::getFormCode, query.getFormCode());
        }
        if (StringUtils.hasText(query.getAssessmentType())) {
            wrapper.eq(AssessmentForm::getAssessmentType, query.getAssessmentType());
        }
        if (StringUtils.hasText(query.getCategoryCode())) {
            wrapper.eq(AssessmentForm::getCategoryCode, query.getCategoryCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(AssessmentForm::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(AssessmentForm::getSortOrder).orderByDesc(AssessmentForm::getCreateTime);
        Page<AssessmentForm> page = PageUtil.createPage(query.getPageNum(), query.getPageSize());
        Page<AssessmentForm> result = baseMapper.selectPage(page, wrapper);
        return PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void updateStatus(Long formId, Integer status) {
        AssessmentForm form = new AssessmentForm();
        form.setFormId(formId);
        form.setStatus(status);
        updateById(form);
    }
}
