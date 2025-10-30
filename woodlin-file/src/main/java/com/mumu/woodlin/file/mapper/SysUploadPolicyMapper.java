package com.mumu.woodlin.file.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.file.entity.SysUploadPolicy;

/**
 * 上传策略Mapper接口
 * 
 * @author mumu
 * @description 上传策略数据访问层
 * @since 2025-01-30
 */
@Mapper
public interface SysUploadPolicyMapper extends BaseMapper<SysUploadPolicy> {
    
}
