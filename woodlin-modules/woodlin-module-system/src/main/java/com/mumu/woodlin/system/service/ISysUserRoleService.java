package com.mumu.woodlin.system.service;

import java.util.List;

/**
 * 用户角色关联服务接口
 * 
 * @author mumu
 * @description 用户角色关联服务接口，提供用户和角色的关联管理
 * @since 2025-01-15
 */
public interface ISysUserRoleService {
    
    /**
     * 保存用户角色关联
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 结果
     */
    boolean saveUserRoles(Long userId, List<Long> roleIds);
    
    /**
     * 删除用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return 结果
     */
    boolean deleteUserRoles(Long userId);
    
    /**
     * 查询用户的所有角色ID
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(Long userId);
    
    /**
     * 检查角色是否被用户使用
     * 
     * @param roleId 角色ID
     * @return 是否被使用
     */
    boolean isRoleInUse(Long roleId);
    
}
