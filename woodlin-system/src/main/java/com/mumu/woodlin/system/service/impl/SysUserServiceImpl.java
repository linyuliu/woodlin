package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.mapper.SysUserMapper;
import com.mumu.woodlin.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息服务实现类
 * 
 * @author mumu
 * @description 用户信息服务实现类，提供用户管理的具体业务逻辑
 * @since 2025-01-01
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public SysUser selectUserByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
    }
    
    /**
     * 分页查询用户列表
     * 
     * @param user 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户分页列表
     */
    @Override
    public IPage<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(user.getUsername()), SysUser::getUsername, user.getUsername())
                .like(StrUtil.isNotBlank(user.getNickname()), SysUser::getNickname, user.getNickname())
                .like(StrUtil.isNotBlank(user.getEmail()), SysUser::getEmail, user.getEmail())
                .like(StrUtil.isNotBlank(user.getMobile()), SysUser::getMobile, user.getMobile())
                .eq(StrUtil.isNotBlank(user.getStatus()), SysUser::getStatus, user.getStatus())
                .eq(ObjectUtil.isNotNull(user.getDeptId()), SysUser::getDeptId, user.getDeptId())
                .orderByDesc(SysUser::getCreateTime);
        
        return this.page(page, queryWrapper);
    }
    
    /**
     * 校验用户名称是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUsernameUnique(SysUser user) {
        Long userId = ObjectUtil.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = selectUserByUsername(user.getUsername());
        return ObjectUtil.isNull(info) || info.getUserId().equals(userId);
    }
    
    /**
     * 校验手机号码是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkPhoneUnique(SysUser user) {
        Long userId = ObjectUtil.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMobile, user.getMobile())
                .last("LIMIT 1"));
        return ObjectUtil.isNull(info) || info.getUserId().equals(userId);
    }
    
    /**
     * 校验email是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkEmailUnique(SysUser user) {
        Long userId = ObjectUtil.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, user.getEmail())
                .last("LIMIT 1"));
        return ObjectUtil.isNull(info) || info.getUserId().equals(userId);
    }
    
    /**
     * 新增用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean insertUser(SysUser user) {
        // 校验用户名唯一性
        if (!checkUsernameUnique(user)) {
            throw new BusinessException("新增用户'" + user.getUsername() + "'失败，登录账号已存在");
        }
        // 校验手机号唯一性
        if (StrUtil.isNotBlank(user.getMobile()) && !checkPhoneUnique(user)) {
            throw new BusinessException("新增用户'" + user.getUsername() + "'失败，手机号码已存在");
        }
        // 校验邮箱唯一性
        if (StrUtil.isNotBlank(user.getEmail()) && !checkEmailUnique(user)) {
            throw new BusinessException("新增用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        
        return this.save(user);
    }
    
    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean updateUser(SysUser user) {
        // 校验用户名唯一性
        if (!checkUsernameUnique(user)) {
            throw new BusinessException("修改用户'" + user.getUsername() + "'失败，登录账号已存在");
        }
        // 校验手机号唯一性
        if (StrUtil.isNotBlank(user.getMobile()) && !checkPhoneUnique(user)) {
            throw new BusinessException("修改用户'" + user.getUsername() + "'失败，手机号码已存在");
        }
        // 校验邮箱唯一性
        if (StrUtil.isNotBlank(user.getEmail()) && !checkEmailUnique(user)) {
            throw new BusinessException("修改用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        
        return this.updateById(user);
    }
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public boolean resetPassword(Long userId, String password) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(password);
        return this.updateById(user);
    }
    
    /**
     * 导入用户数据
     * 
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport) {
        if (ObjectUtil.isEmpty(userList)) {
            throw new BusinessException("导入用户数据不能为空！");
        }
        
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        
        for (SysUser user : userList) {
            try {
                // 验证是否存在这个用户
                SysUser existUser = selectUserByUsername(user.getUsername());
                if (ObjectUtil.isNull(existUser)) {
                    this.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUsername()).append(" 导入成功");
                } else if (isUpdateSupport) {
                    user.setUserId(existUser.getUserId());
                    this.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUsername()).append(" 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUsername()).append(" 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUsername() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new BusinessException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        
        return successMsg.toString();
    }
}