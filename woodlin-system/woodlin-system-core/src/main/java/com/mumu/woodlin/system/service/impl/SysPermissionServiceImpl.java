package com.mumu.woodlin.system.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mumu.woodlin.system.entity.SysPermission;
import com.mumu.woodlin.system.mapper.SysPermissionMapper;
import com.mumu.woodlin.system.service.ISysPermissionService;

/**
 * 权限信息服务实现
 * 
 * @author mumu
 * @description 权限信息服务实现，支持RBAC1权限继承
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> 
        implements ISysPermissionService {
    
    private final SysPermissionMapper permissionMapper;
    
    @Override
    public List<SysPermission> selectPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }
    
    @Override
    public List<SysPermission> selectPermissionsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return permissionMapper.selectPermissionsByRoleIds(roleIds);
    }
    
    @Override
    public List<SysPermission> selectPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return permissionMapper.selectPermissionsByUserId(userId);
    }
    
    @Override
    public List<String> selectPermissionCodesByUserId(Long userId) {
        List<SysPermission> permissions = selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        
        return permissions.stream()
            .map(SysPermission::getPermissionCode)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());
    }
}
