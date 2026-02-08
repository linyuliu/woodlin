package com.mumu.woodlin.etl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.etl.entity.EtlDataValidationLog;
import com.mumu.woodlin.etl.mapper.EtlDataValidationLogMapper;
import com.mumu.woodlin.etl.service.IEtlDataValidationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ETL 数据一致性校验日志服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Service
public class EtlDataValidationLogServiceImpl
        extends ServiceImpl<EtlDataValidationLogMapper, EtlDataValidationLog>
        implements IEtlDataValidationLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(EtlDataValidationLog log) {
        this.save(log);
    }
}
