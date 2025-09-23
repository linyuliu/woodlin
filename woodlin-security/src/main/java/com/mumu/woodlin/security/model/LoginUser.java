package com.mumu.woodlin.security.model;

import lombok.Data;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录用户信息
 * 
 * @author mumu
 * @description 当前登录用户的详细信息，包括基本信息、权限、角色等
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "登录用户信息")
public class LoginUser implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;
    
    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;
    
    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;
    
    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String mobile;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;
    
    /**
     * 性别（1-男，2-女，0-未知）
     */
    @Schema(description = "性别", example = "1")
    private Integer gender;
    
    /**
     * 用户状态（1-启用，0-禁用）
     */
    @Schema(description = "用户状态", example = "1")
    private String status;
    
    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 租户名称
     */
    @Schema(description = "租户名称")
    private String tenantName;
    
    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;
    
    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;
    
    /**
     * 角色ID列表
     */
    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
    
    /**
     * 角色编码列表
     */
    @Schema(description = "角色编码列表")
    private List<String> roleCodes;
    
    /**
     * 权限标识列表
     */
    @Schema(description = "权限标识列表")
    private List<String> permissions;
    
    /**
     * 登录时间
     */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
    
    /**
     * 登录IP
     */
    @Schema(description = "登录IP")
    private String loginIp;
    
    /**
     * 登录地址
     */
    @Schema(description = "登录地址")
    private String loginAddress;
    
    /**
     * 浏览器
     */
    @Schema(description = "浏览器")
    private String browser;
    
    /**
     * 操作系统
     */
    @Schema(description = "操作系统")
    private String os;
    
    /**
     * 是否为超级管理员
     * 
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return roleCodes != null && roleCodes.contains("super_admin");
    }
    
    /**
     * 判断是否有指定权限
     * 
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        if (isSuperAdmin()) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * 判断是否有指定角色
     * 
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    public boolean hasRole(String roleCode) {
        if (isSuperAdmin()) {
            return true;
        }
        return roleCodes != null && roleCodes.contains(roleCode);
    }
    
}