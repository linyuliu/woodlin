package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mumu.woodlin.assessment.mapper.AssessmentFormVersionMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.model.query.AssessmentFormVersionQuery;
import com.mumu.woodlin.assessment.service.IAssessmentFormVersionService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.util.PageUtil;

/**
 * AssessmentFormVersion 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentFormVersionServiceImpl extends ServiceImpl<AssessmentFormVersionMapper, AssessmentFormVersion>
        implements IAssessmentFormVersionService {

    @Override
    public PageResult<AssessmentFormVersion> pageList(AssessmentFormVersionQuery query) {
        LambdaQueryWrapper<AssessmentFormVersion> wrapper = new LambdaQueryWrapper<>();
        if (query.getFormId() != null) {
            wrapper.eq(AssessmentFormVersion::getFormId, query.getFormId());
        }
        if (StringUtils.hasText(query.getVersionNo())) {
            wrapper.like(AssessmentFormVersion::getVersionNo, query.getVersionNo());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(AssessmentFormVersion::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(AssessmentFormVersion::getCreateTime);
        Page<AssessmentFormVersion> page = PageUtil.createPage(query.getPageNum(), query.getPageSize());
        Page<AssessmentFormVersion> result = baseMapper.selectPage(page, wrapper);
        return PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }
}
