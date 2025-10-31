package com.mumu.woodlin.file.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.file.entity.SysFile;

/**
 * 文件信息Mapper接口
 * 
 * @author mumu
 * @description 文件信息数据访问层
 * @since 2025-01-30
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {
    
}
