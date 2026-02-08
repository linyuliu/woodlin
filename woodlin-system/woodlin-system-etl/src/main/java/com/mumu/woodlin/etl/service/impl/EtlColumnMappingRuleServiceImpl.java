package com.mumu.woodlin.etl.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;
import com.mumu.woodlin.etl.mapper.EtlColumnMappingRuleMapper;
import com.mumu.woodlin.etl.service.IEtlColumnMappingRuleService;
import org.springframework.stereotype.Service;

/**
 * ETL 字段映射规则服务实现。
 *
 * @author mumu
 * @since 1.0.0
 */
@Service
public class EtlColumnMappingRuleServiceImpl
        extends ServiceImpl<EtlColumnMappingRuleMapper, EtlColumnMappingRule>
        implements IEtlColumnMappingRuleService {

    @Override
    public List<EtlColumnMappingRule> listEnabledRulesByJobId(Long jobId) {
        return this.list(new LambdaQueryWrapper<EtlColumnMappingRule>()
                .eq(EtlColumnMappingRule::getJobId, jobId)
                .eq(EtlColumnMappingRule::getEnabled, "1")
                .orderByAsc(EtlColumnMappingRule::getOrdinalPosition));
    }
}
