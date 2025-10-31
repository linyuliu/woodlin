package com.mumu.woodlin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mumu.woodlin.system.entity.SysUser;

/**
 * 用户信息Mapper接口
 * 
 * @author mumu
 * @description 用户信息数据访问层接口，基于MyBatis Plus实现
 * @since 2025-01-01
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户信息
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser selectByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查询用户信息
     * 
     * @param mobile 手机号
     * @return 用户信息
     */
    SysUser selectByMobile(@Param("mobile") String mobile);
    
    /**
     * 更新用户登录信息
     * 
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    void updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);
    
    /**
     * 重置密码错误次数
     * 
     * @param userId 用户ID
     */
    void resetPwdErrorCount(@Param("userId") Long userId);
    
    /**
     * 增加密码错误次数
     * 
     * @param userId 用户ID
     */
    void increasePwdErrorCount(@Param("userId") Long userId);
    
}