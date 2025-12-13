package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.etl.entity.EtlJob;

/**
 * ETL任务Mapper接口
 * 
 * @author mumu
 * @description ETL任务数据访问接口
 * @since 2025-01-01
 */
@Mapper
public interface EtlJobMapper extends BaseMapper<EtlJob> {
}
