package com.mumu.woodlin.etl.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.entity.EtlSyncCheckpoint;
import com.mumu.woodlin.etl.mapper.EtlSyncCheckpointMapper;
import com.mumu.woodlin.etl.service.IEtlSyncCheckpointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ETL 同步检查点服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Service
public class EtlSyncCheckpointServiceImpl
        extends ServiceImpl<EtlSyncCheckpointMapper, EtlSyncCheckpoint>
        implements IEtlSyncCheckpointService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EtlSyncCheckpoint getOrCreate(EtlJob job) {
        List<EtlSyncCheckpoint> checkpoints = this.list(new LambdaQueryWrapper<EtlSyncCheckpoint>()
                .eq(EtlSyncCheckpoint::getJobId, job.getJobId())
                .orderByDesc(EtlSyncCheckpoint::getUpdateTime));
        EtlSyncCheckpoint checkpoint = checkpoints.isEmpty() ? null : checkpoints.get(0);
        if (checkpoint != null) {
            return checkpoint;
        }
        EtlSyncCheckpoint initialized = new EtlSyncCheckpoint()
                .setJobId(job.getJobId())
                .setSyncMode(job.getSyncMode())
                .setIncrementalColumn(job.getIncrementalColumn())
                .setTenantId(job.getTenantId())
                .setAppliedBucketCount(0)
                .setSkippedBucketCount(0)
                .setSourceRowCount(0L)
                .setTargetRowCount(0L)
                .setValidationStatus("INIT");
        this.save(initialized);
        return initialized;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAfterExecution(
            EtlSyncCheckpoint checkpoint,
            String lastIncrementalValue,
            Long sourceRowCount,
            Long targetRowCount,
            Integer appliedBucketCount,
            Integer skippedBucketCount,
            String validationStatus,
            Long executionLogId
    ) {
        checkpoint.setLastIncrementalValue(lastIncrementalValue);
        checkpoint.setSourceRowCount(sourceRowCount);
        checkpoint.setTargetRowCount(targetRowCount);
        checkpoint.setAppliedBucketCount(appliedBucketCount);
        checkpoint.setSkippedBucketCount(skippedBucketCount);
        checkpoint.setValidationStatus(validationStatus);
        checkpoint.setLastExecutionLogId(executionLogId);
        checkpoint.setLastSyncTime(LocalDateTime.now());
        this.updateById(checkpoint);
    }
}
