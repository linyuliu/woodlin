package com.mumu.woodlin.etl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.etl.entity.EtlJob;
import com.mumu.woodlin.etl.entity.EtlSyncCheckpoint;

/**
 * ETL 同步检查点服务接口。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface IEtlSyncCheckpointService extends IService<EtlSyncCheckpoint> {

    /**
     * 获取任务检查点，若不存在则初始化。
     *
     * @param job ETL任务
     * @return 任务检查点
     */
    EtlSyncCheckpoint getOrCreate(EtlJob job);

    /**
     * 更新任务检查点。
     *
     * @param checkpoint 检查点
     * @param lastIncrementalValue 最大增量值
     * @param sourceRowCount 源侧行数
     * @param targetRowCount 目标侧行数
     * @param appliedBucketCount 命中桶数量
     * @param skippedBucketCount 跳过桶数量
     * @param validationStatus 校验状态
     * @param executionLogId 执行日志ID
     */
    void updateAfterExecution(
            EtlSyncCheckpoint checkpoint,
            String lastIncrementalValue,
            Long sourceRowCount,
            Long targetRowCount,
            Integer appliedBucketCount,
            Integer skippedBucketCount,
            String validationStatus,
            Long executionLogId
    );
}
