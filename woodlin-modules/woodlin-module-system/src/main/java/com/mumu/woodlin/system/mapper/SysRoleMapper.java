package com.mumu.woodlin.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mumu.woodlin.system.entity.SysRole;

/**
 * 角色信息Mapper接口
 * 
 * @author mumu
 * @description 角色信息数据访问层接口，支持RBAC1角色继承
 * @since 2025-10-31
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据用户ID查询角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 分页查询角色列表
     * 
     * @param page 分页对象
     * @param role 查询条件
     * @return 角色分页列表
     */
    IPage<SysRole> selectRolePage(Page<SysRole> page, @Param("role") SysRole role);
    
    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @return 角色信息
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 根据角色名称查询角色
     * 
     * @param roleName 角色名称
     * @return 角色信息
     */
    SysRole selectByRoleName(@Param("roleName") String roleName);
    
    /**
     * 查询角色的所有子角色（包括子孙角色）
     * 
     * @param roleId 角色ID
     * @return 子角色列表
     */
    List<SysRole> selectDescendantRoles(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的直接子角色
     * 
     * @param roleId 角色ID
     * @return 直接子角色列表
     */
    List<SysRole> selectDirectChildRoles(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的所有祖先角色
     * 
     * @param roleId 角色ID
     * @return 祖先角色列表
     */
    List<SysRole> selectAncestorRoles(@Param("roleId") Long roleId);
    
    /**
     * 查询顶级角色列表（没有父角色的角色）
     * 
     * @param tenantId 租户ID
     * @return 顶级角色列表
     */
    List<SysRole> selectTopLevelRoles(@Param("tenantId") String tenantId);
}
