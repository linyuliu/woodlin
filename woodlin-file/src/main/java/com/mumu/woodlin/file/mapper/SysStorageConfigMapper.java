package com.mumu.woodlin.file.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.file.entity.SysStorageConfig;

/**
 * 存储配置Mapper接口
 * 
 * @author mumu
 * @description 存储配置数据访问层
 * @since 2025-01-30
 */
@Mapper
public interface SysStorageConfigMapper extends BaseMapper<SysStorageConfig> {
    
}
