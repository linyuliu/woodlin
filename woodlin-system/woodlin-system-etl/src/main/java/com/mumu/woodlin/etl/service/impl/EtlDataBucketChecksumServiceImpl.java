package com.mumu.woodlin.etl.service.impl;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.etl.entity.EtlDataBucketChecksum;
import com.mumu.woodlin.etl.mapper.EtlDataBucketChecksumMapper;
import com.mumu.woodlin.etl.service.IEtlDataBucketChecksumService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ETL 桶位校验快照服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Service
public class EtlDataBucketChecksumServiceImpl
        extends ServiceImpl<EtlDataBucketChecksumMapper, EtlDataBucketChecksum>
        implements IEtlDataBucketChecksumService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchRecords(List<EtlDataBucketChecksum> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        this.saveBatch(records);
    }

    @Override
    public int countMismatchedBuckets(Long executionLogId) {
        if (executionLogId == null) {
            return 0;
        }
        List<EtlDataBucketChecksum> records = this.list(new LambdaQueryWrapper<EtlDataBucketChecksum>()
                .eq(EtlDataBucketChecksum::getExecutionLogId, executionLogId));
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return Math.toIntExact(records.stream()
                .filter(item -> !Objects.equals(item.getSourceRowCount(), item.getTargetRowCount())
                        || !Objects.equals(item.getSourceChecksum(), item.getTargetChecksum()))
                .count());
    }
}
