package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysUserAuthIdentity;

/**
 * 用户多重认证标识服务
 *
 * @author mumu
 * @since 2025-12-10
 */
public interface ISysUserAuthIdentityService extends IService<SysUserAuthIdentity> {

    /**
     * 根据类型和标识查询认证信息
     *
     * @param authType   认证类型
     * @param identifier 登录标识
     * @return 认证标识信息
     */
    SysUserAuthIdentity findByTypeAndIdentifier(String authType, String identifier);

    /**
     * 查询用户在指定类型下最新的认证信息
     *
     * @param userId   用户ID
     * @param authType 认证类型
     * @return 认证标识信息
     */
    SysUserAuthIdentity findByUserIdAndType(Long userId, String authType);
}
