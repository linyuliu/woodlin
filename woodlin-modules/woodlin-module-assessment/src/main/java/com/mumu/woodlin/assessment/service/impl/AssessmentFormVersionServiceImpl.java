package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentFormVersionMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentFormVersion;
import com.mumu.woodlin.assessment.service.IAssessmentFormVersionService;

/**
 * AssessmentFormVersion 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentFormVersionServiceImpl extends ServiceImpl<AssessmentFormVersionMapper, AssessmentFormVersion> implements IAssessmentFormVersionService {
}
