package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.etl.entity.EtlColumnMappingRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL 字段映射规则 Mapper。
 *
 * @author mumu
 * @since 1.0.0
 */
@Mapper
public interface EtlColumnMappingRuleMapper extends BaseMapper<EtlColumnMappingRule> {
}
