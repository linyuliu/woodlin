package com.mumu.woodlin.sql2api.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.sql2api.entity.SqlApiConfig;

/**
 * SQL API 配置 Mapper
 *
 * @author yulin
 * @since 2026-05
 */
@Mapper
public interface SqlApiConfigMapper extends BaseMapper<SqlApiConfig> {
}
