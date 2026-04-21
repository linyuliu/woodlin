package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentFormMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentForm;
import com.mumu.woodlin.assessment.service.IAssessmentFormService;

/**
 * AssessmentForm 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentFormServiceImpl extends ServiceImpl<AssessmentFormMapper, AssessmentForm> implements IAssessmentFormService {
}
