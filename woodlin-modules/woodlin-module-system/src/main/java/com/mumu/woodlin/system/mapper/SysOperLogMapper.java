package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.system.entity.SysOperLog;

/**
 * 操作日志Mapper接口
 *
 * @author yulin
 * @description 操作日志数据访问层
 * @since 2026-06
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

}
