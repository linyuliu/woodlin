package com.mumu.woodlin.etl.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.etl.entity.EtlDataBucketChecksum;

/**
 * ETL 桶位校验快照服务接口。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface IEtlDataBucketChecksumService extends IService<EtlDataBucketChecksum> {

    /**
     * 批量保存桶位校验记录。
     *
     * @param records 桶位校验记录
     */
    void saveBatchRecords(List<EtlDataBucketChecksum> records);

    /**
     * 统计执行中差异桶数量。
     *
     * @param executionLogId 执行日志ID
     * @return 差异桶数量
     */
    int countMismatchedBuckets(Long executionLogId);
}
