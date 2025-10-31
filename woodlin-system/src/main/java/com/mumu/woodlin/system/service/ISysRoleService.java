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
    
    /**
     * 查询角色的所有祖先角色（支持RBAC1）
     * 
     * @param roleId 角色ID
     * @return 祖先角色列表
     */
    List<SysRole> selectAncestorRoles(Long roleId);
    
    /**
     * 查询角色的所有后代角色（支持RBAC1）
     * 
     * @param roleId 角色ID
     * @return 后代角色列表
     */
    List<SysRole> selectDescendantRoles(Long roleId);
    
    /**
     * 查询角色的直接子角色（支持RBAC1）
     * 
     * @param roleId 角色ID
     * @return 直接子角色列表
     */
    List<SysRole> selectDirectChildRoles(Long roleId);
    
    /**
     * 查询用户的所有角色（包括继承的角色，支持RBAC1）
     * 
     * @param userId 用户ID
     * @return 角色列表（包括继承的）
     */
    List<SysRole> selectAllRolesByUserId(Long userId);
    
    /**
     * 刷新角色的继承层次关系（支持RBAC1）
     * 
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean refreshRoleHierarchy(Long roleId);
    
    /**
     * 检查角色继承是否会造成循环依赖（支持RBAC1）
     * 
     * @param roleId 角色ID
     * @param parentRoleId 父角色ID
     * @return 是否存在循环依赖
     */
    boolean checkCircularDependency(Long roleId, Long parentRoleId);
    
    /**
     * 查询角色的所有权限（包括继承的权限，支持RBAC1）
     * 
     * @param roleId 角色ID
     * @return 权限编码列表
     */
    List<String> selectAllPermissionsByRoleId(Long roleId);
    
    /**
     * 查询顶级角色列表（没有父角色的角色）
     * 
     * @param tenantId 租户ID
     * @return 顶级角色列表
     */
    List<SysRole> selectTopLevelRoles(String tenantId);
    
    /**
     * 构建角色树（RBAC1）
     * 
     * @param tenantId 租户ID
     * @return 角色树列表
     */
    List<com.mumu.woodlin.system.dto.RoleTreeDTO> buildRoleTree(String tenantId);
}