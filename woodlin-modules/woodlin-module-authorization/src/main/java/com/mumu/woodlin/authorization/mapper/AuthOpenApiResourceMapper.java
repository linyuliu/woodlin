package com.mumu.woodlin.authorization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.authorization.entity.AuthOpenApiResource;

/**
 * 开放接口资源目录 Mapper。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Mapper
public interface AuthOpenApiResourceMapper extends BaseMapper<AuthOpenApiResource> {
}
