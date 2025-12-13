package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.etl.entity.EtlExecutionLog;

/**
 * ETL执行历史Mapper接口
 * 
 * @author mumu
 * @description ETL执行历史数据访问接口
 * @since 2025-01-01
 */
@Mapper
public interface EtlExecutionLogMapper extends BaseMapper<EtlExecutionLog> {
}
