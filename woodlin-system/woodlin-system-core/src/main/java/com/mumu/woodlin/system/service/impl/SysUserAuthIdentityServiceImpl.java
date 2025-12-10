package com.mumu.woodlin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.system.entity.SysUserAuthIdentity;
import com.mumu.woodlin.system.mapper.SysUserAuthIdentityMapper;
import com.mumu.woodlin.system.service.ISysUserAuthIdentityService;
import org.springframework.stereotype.Service;

/**
 * 用户多重认证标识服务实现
 *
 * @author mumu
 * @since 2025-12-10
 */
@Service
public class SysUserAuthIdentityServiceImpl
        extends ServiceImpl<SysUserAuthIdentityMapper, SysUserAuthIdentity>
        implements ISysUserAuthIdentityService {

    @Override
    public SysUserAuthIdentity findByTypeAndIdentifier(String authType, String identifier) {
        return lambdaQuery()
            .eq(SysUserAuthIdentity::getAuthType, authType)
            .eq(SysUserAuthIdentity::getIdentifier, identifier)
            .eq(SysUserAuthIdentity::getStatus, "1")
            .eq(SysUserAuthIdentity::getDeleted, "0")
            .one();
    }

    @Override
    public SysUserAuthIdentity findByUserIdAndType(Long userId, String authType) {
        return lambdaQuery()
            .eq(SysUserAuthIdentity::getUserId, userId)
            .eq(SysUserAuthIdentity::getAuthType, authType)
            .eq(SysUserAuthIdentity::getStatus, "1")
            .eq(SysUserAuthIdentity::getDeleted, "0")
            .orderByDesc(SysUserAuthIdentity::getUpdateTime)
            .last("LIMIT 1")
            .one();
    }
}
