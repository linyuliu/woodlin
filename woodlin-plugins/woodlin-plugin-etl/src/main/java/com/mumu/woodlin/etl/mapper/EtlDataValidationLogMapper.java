package com.mumu.woodlin.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.etl.entity.EtlDataValidationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL 数据一致性校验日志 Mapper。
 *
 * @author mumu
 * @since 1.0.0
 */
@Mapper
public interface EtlDataValidationLogMapper extends BaseMapper<EtlDataValidationLog> {
}
