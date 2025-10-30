package com.mumu.woodlin.file.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.file.entity.SysUploadToken;

/**
 * 上传令牌Mapper接口
 * 
 * @author mumu
 * @description 上传令牌数据访问层
 * @since 2025-01-30
 */
@Mapper
public interface SysUploadTokenMapper extends BaseMapper<SysUploadToken> {
    
}
