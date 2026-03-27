package com.mumu.woodlin.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.tenant.entity.SysTenant;

/**
 * 租户信息 Mapper
 *
 * @author mumu
 * @since 2026-03-18
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
