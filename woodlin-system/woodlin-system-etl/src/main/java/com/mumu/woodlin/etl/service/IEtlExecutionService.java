package com.mumu.woodlin.etl.service;

import com.mumu.woodlin.etl.entity.EtlJob;

/**
 * ETL执行服务接口
 * 
 * @author mumu
 * @description ETL任务执行服务接口，负责实际的数据抽取、转换、加载操作
 * @since 2025-01-01
 */
public interface IEtlExecutionService {
    
    /**
     * 执行ETL任务
     * 
     * @param job ETL任务
     */
    void execute(EtlJob job);
}
