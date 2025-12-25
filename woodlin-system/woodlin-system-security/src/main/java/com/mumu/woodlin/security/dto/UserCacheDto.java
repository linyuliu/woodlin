package com.mumu.woodlin.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户缓存数据传输对象
 * 
 * @author mumu
 * @description 统一的用户缓存结构，合并用户信息、扩展信息和角色信息，适用于RBAC1模型
 *              包含安全相关字段（首次登录、密码过期、二次认证等）用于安全控制
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
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 头像URL
     */
    private String avatar;
    
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
    
    // ==================== 安全相关字段 ====================
    
    /**
     * 是否首次登录
     * 用于强制首次登录修改密码等场景
     */
    private Boolean isFirstLogin;
    
    /**
     * 密码最后修改时间
     * 用于判断密码是否过期
     */
    private LocalDateTime pwdChangeTime;
    
    /**
     * 密码过期天数（0表示永不过期）
     * 用于密码过期策略
     */
    private Integer pwdExpireDays;
    
    /**
     * 密码错误次数
     * 用于账号锁定策略
     */
    private Integer pwdErrorCount;
    
    /**
     * 账号锁定时间
     * 用于判断账号是否被锁定
     */
    private LocalDateTime lockTime;
    
    /**
     * 是否启用二次认证/MFA
     * 用于二次认证流程控制
     */
    private Boolean mfaEnabled;
    
    /**
     * 二次认证密钥
     * 用于TOTP等二次认证方式
     */
    private String mfaSecret;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 缓存时间戳
     */
    private Long cacheTime;
}
