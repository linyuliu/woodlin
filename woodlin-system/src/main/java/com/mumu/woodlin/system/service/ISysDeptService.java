package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysDept;

import java.util.List;

/**
 * 部门信息服务接口
 * 
 * @author mumu
 * @description 部门信息服务接口，提供部门管理的业务功能
 * @since 2025-01-01
 */
public interface ISysDeptService extends IService<SysDept> {
    
    /**
     * 查询部门管理树
     * 
     * @param dept 查询条件
     * @return 部门树
     */
    List<SysDept> selectDeptTree(SysDept dept);
    
    /**
     * 构建前端所需要的树结构
     * 
     * @param depts 部门列表
     * @return 树结构列表
     */
    List<SysDept> buildDeptTree(List<SysDept> depts);
    
    /**
     * 根据角色ID查询部门（数据权限）
     * 
     * @param roleId 角色ID
     * @return 部门列表
     */
    List<Long> selectDeptListByRoleId(Long roleId);
    
    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    SysDept selectDeptById(Long deptId);
    
    /**
     * 是否存在子节点
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    boolean hasChildByDeptId(Long deptId);
    
    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkDeptExistUser(Long deptId);
    
    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    boolean checkDeptNameUnique(SysDept dept);
    
    /**
     * 新增部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    boolean insertDept(SysDept dept);
    
    /**
     * 修改部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    boolean updateDept(SysDept dept);
    
    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    boolean deleteDeptById(Long deptId);
}