package com.mumu.woodlin.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.system.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色和权限关联Mapper接口
 *
 * @author mumu
 * @description 角色和权限关联关系数据访问层接口
 * @since 2026-03-15
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 查询角色已分配的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有权限关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量新增角色权限关联
     *
     * @param rolePermissionList 角色权限关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<SysRolePermission> rolePermissionList);
}
