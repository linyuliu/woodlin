package com.mumu.woodlin.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mumu.woodlin.system.entity.SysRoleHierarchy;

/**
 * 角色继承层次关系Mapper接口
 * 
 * @author mumu
 * @description 角色继承层次关系数据访问层接口，支持RBAC1
 * @since 2025-10-31
 */
@Mapper
public interface SysRoleHierarchyMapper extends BaseMapper<SysRoleHierarchy> {
    
    /**
     * 查询角色的所有祖先角色ID
     * 
     * @param roleId 角色ID
     * @return 祖先角色ID列表（包含自己）
     */
    List<Long> selectAncestorRoleIds(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的所有后代角色ID
     * 
     * @param roleId 角色ID
     * @return 后代角色ID列表（包含自己）
     */
    List<Long> selectDescendantRoleIds(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的直接子角色ID
     * 
     * @param roleId 角色ID
     * @return 直接子角色ID列表
     */
    List<Long> selectDirectChildRoleIds(@Param("roleId") Long roleId);
    
    /**
     * 删除角色的所有层次关系
     * 
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入角色层次关系
     * 
     * @param hierarchies 层次关系列表
     * @return 影响行数
     */
    int batchInsert(@Param("hierarchies") List<SysRoleHierarchy> hierarchies);
    
    /**
     * 检查是否存在循环依赖
     * 
     * @param roleId 角色ID
     * @param parentRoleId 父角色ID
     * @return 是否存在循环依赖
     */
    boolean checkCircularDependency(@Param("roleId") Long roleId, @Param("parentRoleId") Long parentRoleId);
}
