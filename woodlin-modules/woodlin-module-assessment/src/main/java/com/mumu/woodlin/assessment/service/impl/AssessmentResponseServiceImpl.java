package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentResponseMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentResponse;
import com.mumu.woodlin.assessment.service.IAssessmentResponseService;

/**
 * AssessmentResponse 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentResponseServiceImpl extends ServiceImpl<AssessmentResponseMapper, AssessmentResponse> implements IAssessmentResponseService {
}
