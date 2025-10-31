package com.mumu.woodlin.system.entity;

import java.io.Serial;

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
 * 角色信息实体
 * 
 * @author mumu
 * @description 系统角色信息实体类，用于角色管理和权限控制
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色信息")
public class SysRole extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.ASSIGN_ID)
    @Schema(description = "角色ID")
    private Long roleId;
    
    /**
     * 父角色ID（用于角色继承，RBAC1支持）
     */
    @TableField("parent_role_id")
    @Schema(description = "父角色ID")
    private Long parentRoleId;
    
    /**
     * 角色层级（0为顶级角色）
     */
    @TableField("role_level")
    @Schema(description = "角色层级")
    private Integer roleLevel;
    
    /**
     * 角色路径（用于快速查找祖先角色，格式：/1/2/3/）
     */
    @TableField("role_path")
    @Schema(description = "角色路径")
    private String rolePath;
    
    /**
     * 角色名称
     */
    @TableField("role_name")
    @Schema(description = "角色名称")
    private String roleName;
    
    /**
     * 角色编码
     */
    @TableField("role_code")
    @Schema(description = "角色编码")
    private String roleCode;
    
    /**
     * 显示顺序
     */
    @TableField("sort_order")
    @Schema(description = "显示顺序")
    private Integer sortOrder;
    
    /**
     * 数据范围（1-全部数据权限，2-自定数据权限，3-本部门数据权限，4-本部门及以下数据权限，5-仅本人数据权限）
     */
    @TableField("data_scope")
    @Schema(description = "数据范围")
    private String dataScope;
    
    /**
     * 是否可继承（1-是，0-否）
     */
    @TableField("is_inheritable")
    @Schema(description = "是否可继承")
    private String isInheritable;
    
    /**
     * 角色状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "角色状态", example = "1")
    private String status;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}