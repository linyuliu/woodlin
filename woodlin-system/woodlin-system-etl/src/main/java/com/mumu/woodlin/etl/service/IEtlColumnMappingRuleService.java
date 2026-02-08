package com.mumu.woodlin.etl.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;

/**
 * ETL 字段映射规则服务接口。
 *
 * @author mumu
 * @since 1.0.0
 */
public interface IEtlColumnMappingRuleService extends IService<EtlColumnMappingRule> {

    /**
     * 查询指定任务启用的字段映射规则。
     *
     * @param jobId 任务ID
     * @return 字段映射规则列表
     */
    List<EtlColumnMappingRule> listEnabledRulesByJobId(Long jobId);
}
