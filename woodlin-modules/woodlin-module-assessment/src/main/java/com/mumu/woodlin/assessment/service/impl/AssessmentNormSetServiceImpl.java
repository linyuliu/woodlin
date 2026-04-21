package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mumu.woodlin.assessment.mapper.AssessmentNormSetMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentNormSet;
import com.mumu.woodlin.assessment.model.query.AssessmentNormSetQuery;
import com.mumu.woodlin.assessment.service.IAssessmentNormSetService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.util.PageUtil;

/**
 * AssessmentNormSet 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentNormSetServiceImpl extends ServiceImpl<AssessmentNormSetMapper, AssessmentNormSet>
        implements IAssessmentNormSetService {

    @Override
    public PageResult<AssessmentNormSet> pageList(AssessmentNormSetQuery query) {
        LambdaQueryWrapper<AssessmentNormSet> wrapper = new LambdaQueryWrapper<>();
        if (query.getFormId() != null) {
            wrapper.eq(AssessmentNormSet::getFormId, query.getFormId());
        }
        if (StringUtils.hasText(query.getNormSetName())) {
            wrapper.like(AssessmentNormSet::getNormSetName, query.getNormSetName());
        }
        if (StringUtils.hasText(query.getNormSetCode())) {
            wrapper.eq(AssessmentNormSet::getNormSetCode, query.getNormSetCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(AssessmentNormSet::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(AssessmentNormSet::getCreateTime);
        Page<AssessmentNormSet> page = PageUtil.createPage(query.getPageNum(), query.getPageSize());
        Page<AssessmentNormSet> result = baseMapper.selectPage(page, wrapper);
        return PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }
}
