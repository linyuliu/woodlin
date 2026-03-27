package com.mumu.woodlin.etl.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.etl.entity.EtlJob;

/**
 * ETL任务服务接口
 * 
 * @author mumu
 * @description ETL任务管理服务接口
 * @since 2025-01-01
 */
public interface IEtlJobService extends IService<EtlJob> {
    
    /**
     * 创建ETL任务
     * 
     * @param etlJob ETL任务信息
     * @return 是否创建成功
     */
    boolean createJob(EtlJob etlJob);
    
    /**
     * 更新ETL任务
     * 
     * @param etlJob ETL任务信息
     * @return 是否更新成功
     */
    boolean updateJob(EtlJob etlJob);
    
    /**
     * 删除ETL任务
     * 
     * @param jobId 任务ID
     * @return 是否删除成功
     */
    boolean deleteJob(Long jobId);
    
    /**
     * 启用ETL任务
     * 
     * @param jobId 任务ID
     * @return 是否启用成功
     */
    boolean enableJob(Long jobId);
    
    /**
     * 禁用ETL任务
     * 
     * @param jobId 任务ID
     * @return 是否禁用成功
     */
    boolean disableJob(Long jobId);
    
    /**
     * 立即执行ETL任务
     * 
     * @param jobId 任务ID
     * @return 是否执行成功
     */
    boolean executeJob(Long jobId);
    
    /**
     * 查询所有启用的ETL任务
     * 
     * @return 启用的ETL任务列表
     */
    List<EtlJob> listEnabledJobs();
    
    /**
     * 根据租户ID查询ETL任务
     * 
     * @param tenantId 租户ID
     * @return ETL任务列表
     */
    List<EtlJob> listJobsByTenantId(String tenantId);
}
