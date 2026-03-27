package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mumu.woodlin.system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门信息Mapper接口
 *
 * @author mumu
 * @description 部门信息数据访问层接口
 * @since 2026-03-15
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 根据角色ID查询关联部门ID列表
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId);
}
