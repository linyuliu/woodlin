package com.mumu.woodlin.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.system.dto.RouteVO;
import com.mumu.woodlin.system.entity.SysPermission;

/**
 * 权限信息服务接口
 * 
 * @author mumu
 * @description 权限信息服务接口，支持RBAC1权限继承
 * @since 2025-10-31
 */
public interface ISysPermissionService extends IService<SysPermission> {
    
    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleId(Long roleId);
    
    /**
     * 根据角色ID列表查询权限列表
     * 
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleIds(List<Long> roleIds);
    
    /**
     * 根据用户ID查询权限列表（包括角色继承的权限）
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByUserId(Long userId);
    
    /**
     * 根据用户ID查询权限编码列表（包括角色继承的权限）
     * 
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> selectPermissionCodesByUserId(Long userId);
    
    /**
     * 根据用户ID查询按钮权限编码列表（包括角色继承的权限）
     * 
     * @param userId 用户ID
     * @return 按钮权限编码列表
     */
    List<String> selectButtonPermissionCodesByUserId(Long userId);
    
    /**
     * 根据用户ID查询菜单权限编码列表（包括角色继承的权限）
     * 
     * @param userId 用户ID
     * @return 菜单权限编码列表
     */
    List<String> selectMenuPermissionCodesByUserId(Long userId);
    
    /**
     * 根据用户ID查询菜单路由列表（包括角色继承的权限）
     * 
     * @param userId 用户ID
     * @return 路由列表（树形结构）
     */
    List<RouteVO> selectRoutesByUserId(Long userId);
}
