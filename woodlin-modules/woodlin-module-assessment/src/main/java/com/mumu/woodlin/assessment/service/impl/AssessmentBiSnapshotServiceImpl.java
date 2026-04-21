package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentBiSnapshotMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentBiSnapshot;
import com.mumu.woodlin.assessment.service.IAssessmentBiSnapshotService;

/**
 * AssessmentBiSnapshot 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentBiSnapshotServiceImpl extends ServiceImpl<AssessmentBiSnapshotMapper, AssessmentBiSnapshot> implements IAssessmentBiSnapshotService {
}
