package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mumu.woodlin.assessment.mapper.AssessmentPublishMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentPublish;
import com.mumu.woodlin.assessment.model.query.AssessmentPublishQuery;
import com.mumu.woodlin.assessment.service.IAssessmentPublishService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.common.util.PageUtil;

/**
 * AssessmentPublish 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentPublishServiceImpl extends ServiceImpl<AssessmentPublishMapper, AssessmentPublish>
        implements IAssessmentPublishService {

    @Override
    public PageResult<AssessmentPublish> pageList(AssessmentPublishQuery query) {
        LambdaQueryWrapper<AssessmentPublish> wrapper = new LambdaQueryWrapper<>();
        if (query.getFormId() != null) {
            wrapper.eq(AssessmentPublish::getFormId, query.getFormId());
        }
        if (StringUtils.hasText(query.getPublishName())) {
            wrapper.like(AssessmentPublish::getPublishName, query.getPublishName());
        }
        if (StringUtils.hasText(query.getPublishCode())) {
            wrapper.eq(AssessmentPublish::getPublishCode, query.getPublishCode());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(AssessmentPublish::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(AssessmentPublish::getCreateTime);
        Page<AssessmentPublish> page = PageUtil.createPage(query.getPageNum(), query.getPageSize());
        Page<AssessmentPublish> result = baseMapper.selectPage(page, wrapper);
        return PageResult.success(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void updateStatus(Long publishId, String status) {
        AssessmentPublish publish = new AssessmentPublish();
        publish.setPublishId(publishId);
        publish.setStatus(status);
        updateById(publish);
    }
}
