package com.mumu.woodlin.authorization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.authorization.entity.AuthScopeResource;

/**
 * Scope 开放接口资源关联 Mapper。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Mapper
public interface AuthScopeResourceMapper extends BaseMapper<AuthScopeResource> {
}
