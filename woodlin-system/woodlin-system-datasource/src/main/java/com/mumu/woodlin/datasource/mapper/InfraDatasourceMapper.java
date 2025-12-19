package com.mumu.woodlin.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.datasource.entity.InfraDatasourceConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基础设施数据源Mapper
 * 
 * @author mumu
 * @since 1.0.0
 */
@Mapper
public interface InfraDatasourceMapper extends BaseMapper<InfraDatasourceConfig> {
}
