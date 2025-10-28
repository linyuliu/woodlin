package com.mumu.woodlin.system.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.system.entity.SysRole;

/**
 * 角色信息服务接口
 * 
 * @author mumu
 * @description 角色信息服务接口，提供角色管理的业务功能
 * @since 2025-01-01
 */
public interface ISysRoleService extends IService<SysRole> {
    
    /**
     * 分页查询角色列表
     * 
     * @param role 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 角色分页列表
     */
    IPage<SysRole> selectRolePage(SysRole role, Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户ID查询角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(Long userId);
    
    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    boolean checkRoleNameUnique(SysRole role);
    
    /**
     * 校验角色编码是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    boolean checkRoleCodeUnique(SysRole role);
    
    /**
     * 新增角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    boolean insertRole(SysRole role);
    
    /**
     * 修改角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    boolean updateRole(SysRole role);
    
    /**
     * 批量删除角色信息
     * 
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    boolean deleteRoleByIds(List<Long> roleIds);
}