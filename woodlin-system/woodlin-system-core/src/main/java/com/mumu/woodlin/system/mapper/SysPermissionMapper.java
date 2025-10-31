package com.mumu.woodlin.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mumu.woodlin.system.entity.SysPermission;

/**
 * 权限信息Mapper接口
 * 
 * @author mumu
 * @description 权限信息数据访问层接口，支持RBAC1权限继承
 * @since 2025-10-31
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID列表查询权限列表
     * 
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);
    
    /**
     * 根据用户ID查询权限列表（包括角色继承的权限，支持RBAC1）
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);
}
