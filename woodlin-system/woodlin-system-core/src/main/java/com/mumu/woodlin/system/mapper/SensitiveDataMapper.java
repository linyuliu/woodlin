package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.system.entity.SensitiveData;

/**
 * 敏感数据Mapper接口
 * 
 * @author mumu
 * @description 敏感数据访问层接口
 * @since 2025-01-01
 */
@Mapper
public interface SensitiveDataMapper extends BaseMapper<SensitiveData> {
}
