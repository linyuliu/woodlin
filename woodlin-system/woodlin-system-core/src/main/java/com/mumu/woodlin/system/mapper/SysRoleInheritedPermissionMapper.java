package com.mumu.woodlin.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mumu.woodlin.system.entity.SysRoleInheritedPermission;

/**
 * 角色继承权限缓存Mapper接口
 * 
 * @author mumu
 * @description 角色继承权限缓存数据访问层接口，用于性能优化
 * @since 2025-10-31
 */
@Mapper
public interface SysRoleInheritedPermissionMapper extends BaseMapper<SysRoleInheritedPermission> {
    
    /**
     * 查询角色的所有权限ID（包括继承的）
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的直接权限ID（不包括继承的）
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectDirectPermissionIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 删除角色的所有权限缓存
     * 
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入角色权限缓存
     * 
     * @param permissions 权限缓存列表
     * @return 影响行数
     */
    int batchInsert(@Param("permissions") List<SysRoleInheritedPermission> permissions);
    
    /**
     * 刷新角色的继承权限缓存
     * 
     * @param roleId 角色ID
     * @return 影响行数
     */
    int refreshInheritedPermissions(@Param("roleId") Long roleId);
}
