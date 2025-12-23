package com.mumu.woodlin.system.entity;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 用户信息实体
 * 
 * @author mumu
 * @description 系统用户信息实体类，包含用户的基本信息和状态
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户信息")
public class SysUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 用户名
     */
    @TableField("username")
    @Schema(description = "用户名")
    private String username;
    
    /**
     * 密码
     */
    @TableField("password")
    @Schema(description = "密码")
    private String password;
    
    /**
     * 用户昵称
     */
    @TableField("nickname")
    @Schema(description = "用户昵称")
    private String nickname;
    
    /**
     * 真实姓名
     */
    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;
    
    /**
     * 邮箱
     */
    @TableField("email")
    @Schema(description = "邮箱")
    private String email;
    
    /**
     * 手机号
     */
    @TableField("mobile")
    @Schema(description = "手机号")
    private String mobile;
    
    /**
     * 头像URL
     */
    @TableField("avatar")
    @Schema(description = "头像URL")
    private String avatar;
    
    /**
     * 性别（1-男，2-女，0-未知）
     */
    @TableField("gender")
    @Schema(description = "性别", example = "1")
    private Integer gender;
    
    /**
     * 生日
     */
    @TableField("birthday")
    @Schema(description = "生日")
    private LocalDateTime birthday;
    
    /**
     * 用户状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "用户状态", example = "1")
    private String status;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 部门ID
     */
    @TableField("dept_id")
    @Schema(description = "部门ID")
    private Long deptId;
    
    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    @Schema(description = "最后登录IP")
    private String lastLoginIp;
    
    /**
     * 登录次数
     */
    @TableField("login_count")
    @Schema(description = "登录次数")
    private Integer loginCount;
    
    /**
     * 密码错误次数
     */
    @TableField("pwd_error_count")
    @Schema(description = "密码错误次数")
    private Integer pwdErrorCount;
    
    /**
     * 账号锁定时间
     */
    @TableField("lock_time")
    @Schema(description = "账号锁定时间")
    private LocalDateTime lockTime;
    
    /**
     * 密码最后修改时间
     */
    @TableField("pwd_change_time")
    @Schema(description = "密码最后修改时间")
    private LocalDateTime pwdChangeTime;
    
    /**
     * 是否首次登录
     */
    @TableField("is_first_login")
    @Schema(description = "是否首次登录")
    private Boolean isFirstLogin;
    
    /**
     * 密码过期天数（0表示永不过期，优先于系统配置）
     */
    @TableField("pwd_expire_days")
    @Schema(description = "密码过期天数")
    private Integer pwdExpireDays;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
    
    /**
     * 角色ID列表（非数据库字段，用于前端交互）
     */
    @TableField(exist = false)
    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
    
}