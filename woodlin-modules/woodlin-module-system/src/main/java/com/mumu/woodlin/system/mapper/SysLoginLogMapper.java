package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.system.entity.SysLoginLog;

/**
 * 登录日志Mapper接口
 *
 * @author yulin
 * @description 登录日志数据访问层
 * @since 2026-06
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

}
