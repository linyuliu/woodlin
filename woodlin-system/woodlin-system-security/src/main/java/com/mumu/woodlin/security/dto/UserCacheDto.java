package com.mumu.woodlin.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户缓存数据传输对象
 * 
 * @author mumu
 * @description 统一的用户缓存结构，合并用户信息、扩展信息和角色信息，适用于RBAC1模型
 * @since 2025-01-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCacheDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色编码列表
     */
    private List<String> roles;
    
    /**
     * 用户所有权限列表
     */
    private List<String> permissions;
    
    /**
     * 用户按钮权限列表
     */
    private List<String> buttonPermissions;
    
    /**
     * 用户菜单权限列表
     */
    private List<String> menuPermissions;
    
    /**
     * 用户路由列表（动态路由）
     */
    private Object routes;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 用户状态
     */
    private String status;
    
    /**
     * 缓存时间戳
     */
    private Long cacheTime;
}
