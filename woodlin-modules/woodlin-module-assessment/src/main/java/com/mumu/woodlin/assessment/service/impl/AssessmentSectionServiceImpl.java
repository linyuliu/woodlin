package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentSectionMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentSection;
import com.mumu.woodlin.assessment.service.IAssessmentSectionService;

/**
 * AssessmentSection 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentSectionServiceImpl extends ServiceImpl<AssessmentSectionMapper, AssessmentSection> implements IAssessmentSectionService {
}
