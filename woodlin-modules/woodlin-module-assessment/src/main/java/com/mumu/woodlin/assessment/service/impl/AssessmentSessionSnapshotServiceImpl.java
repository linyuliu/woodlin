package com.mumu.woodlin.assessment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.assessment.mapper.AssessmentSessionSnapshotMapper;
import com.mumu.woodlin.assessment.model.entity.AssessmentSessionSnapshot;
import com.mumu.woodlin.assessment.service.IAssessmentSessionSnapshotService;

/**
 * AssessmentSessionSnapshot 服务实现
 *
 * @author mumu
 * @since 2025-01-01
 */
@Slf4j
@Service
public class AssessmentSessionSnapshotServiceImpl extends ServiceImpl<AssessmentSessionSnapshotMapper, AssessmentSessionSnapshot> implements IAssessmentSessionSnapshotService {
}
