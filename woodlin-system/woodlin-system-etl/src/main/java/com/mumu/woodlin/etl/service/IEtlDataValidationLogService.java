package com.mumu.woodlin.etl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.etl.entity.EtlDataValidationLog;

/**
 * ETL 数据一致性校验日志服务接口。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface IEtlDataValidationLogService extends IService<EtlDataValidationLog> {

    /**
     * 记录校验日志。
     *
     * @param log 校验日志
     */
    void record(EtlDataValidationLog log);
}
