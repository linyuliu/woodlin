package com.mumu.woodlin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.system.entity.SysUserRole;
import com.mumu.woodlin.system.mapper.SysUserRoleMapper;
import com.mumu.woodlin.system.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色关联服务实现类
 * 
 * @author mumu
 * @description 用户角色关联服务实现类，提供用户和角色的关联管理
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {
    
    private final SysUserRoleMapper userRoleMapper;
    
    /**
     * 保存用户角色关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            log.warn("用户ID为空，无法保存用户角色关联");
            return false;
        }
        
        // 先删除该用户的所有角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 如果角色ID列表为空，则只删除不新增
        if (roleIds == null || roleIds.isEmpty()) {
            log.info("角色ID列表为空，已删除用户 {} 的所有角色关联", userId);
            return true;
        }
        
        // 批量新增用户角色关联
        List<SysUserRole> userRoleList = new ArrayList<>();
        for (Long roleId : roleIds) {
            if (roleId != null) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleList.add(userRole);
            }
        }
        
        if (!userRoleList.isEmpty()) {
            int count = userRoleMapper.batchInsert(userRoleList);
            log.info("成功为用户 {} 分配 {} 个角色", userId, count);
            return count > 0;
        }
        
        return true;
    }
    
    /**
     * 删除用户的所有角色关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserRoles(Long userId) {
        if (userId == null) {
            log.warn("用户ID为空，无法删除用户角色关联");
            return false;
        }
        
        int count = userRoleMapper.deleteByUserId(userId);
        log.info("成功删除用户 {} 的 {} 个角色关联", userId, count);
        return true;
    }
    
    /**
     * 查询用户的所有角色ID
     */
    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        if (userId == null) {
            log.warn("用户ID为空，返回空角色列表");
            return new ArrayList<>();
        }
        
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }
    
    /**
     * 检查角色是否被用户使用
     */
    @Override
    public boolean isRoleInUse(Long roleId) {
        if (roleId == null) {
            return false;
        }
        
        int count = userRoleMapper.countUsersByRoleId(roleId);
        return count > 0;
    }
    
}
