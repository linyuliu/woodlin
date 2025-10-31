package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.system.entity.SysUser;

/**
 * 用户信息服务接口
 * 
 * @author mumu
 * @description 用户信息服务接口，提供用户管理的业务功能
 * @since 2025-01-01
 */
public interface ISysUserService extends IService<SysUser> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectUserByUsername(String username);
    
    /**
     * 分页查询用户列表
     * 
     * @param user 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户分页列表
     */
    IPage<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize);
    
    /**
     * 校验用户名称是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    boolean checkUsernameUnique(SysUser user);
    
    /**
     * 校验手机号码是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    boolean checkPhoneUnique(SysUser user);
    
    /**
     * 校验email是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    boolean checkEmailUnique(SysUser user);
    
    /**
     * 新增用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    boolean insertUser(SysUser user);
    
    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    boolean updateUser(SysUser user);
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 结果
     */
    boolean resetPassword(Long userId, String password);
    
    /**
     * 导入用户数据
     * 
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     */
    String importUser(java.util.List<SysUser> userList, Boolean isUpdateSupport);
}