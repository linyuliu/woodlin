package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户和角色关联Mapper接口
 * 
 * @author mumu
 * @description 用户和角色关联关系数据访问层接口
 * @since 2025-01-15
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 批量新增用户角色关联
     * 
     * @param userRoleList 用户角色关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<SysUserRole> userRoleList);
    
    /**
     * 删除用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询关联的用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    int countUsersByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询用户的所有角色ID
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
}
