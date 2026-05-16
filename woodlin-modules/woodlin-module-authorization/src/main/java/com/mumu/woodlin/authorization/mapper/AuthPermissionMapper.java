package com.mumu.woodlin.authorization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.mumu.woodlin.authorization.entity.AuthPermission;

/**
 * 授权权限 Mapper。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Mapper
public interface AuthPermissionMapper extends BaseMapper<AuthPermission> {
}
