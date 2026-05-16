package com.mumu.woodlin.authorization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.authorization.entity.AuthQuotaPolicy;

/**
 * 限额策略 Mapper。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Mapper
public interface AuthQuotaPolicyMapper extends BaseMapper<AuthQuotaPolicy> {
}
