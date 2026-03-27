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
 * 权限信息实体
 * 
 * @author mumu
 * @description 系统权限信息实体类，用于权限管理和访问控制
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
@Schema(description = "权限信息")
public class SysPermission extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 权限ID
     */
    @TableId(value = "permission_id", type = IdType.ASSIGN_ID)
    @Schema(description = "权限ID")
    private Long permissionId;
    
    /**
     * 父权限ID
     */
    @TableField("parent_id")
    @Schema(description = "父权限ID")
    private Long parentId;
    
    /**
     * 权限名称
     */
    @TableField("permission_name")
    @Schema(description = "权限名称")
    private String permissionName;
    
    /**
     * 权限编码
     */
    @TableField("permission_code")
    @Schema(description = "权限编码")
    private String permissionCode;
    
    /**
     * 权限类型（M-目录，C-菜单，F-按钮）
     */
    @TableField("permission_type")
    @Schema(description = "权限类型")
    private String permissionType;
    
    /**
     * 路由地址
     */
    @TableField("path")
    @Schema(description = "路由地址")
    private String path;
    
    /**
     * 组件路径
     */
    @TableField("component")
    @Schema(description = "组件路径")
    private String component;
    
    /**
     * 权限图标
     */
    @TableField("icon")
    @Schema(description = "权限图标")
    private String icon;
    
    /**
     * 显示顺序
     */
    @TableField("sort_order")
    @Schema(description = "显示顺序")
    private Integer sortOrder;
    
    /**
     * 权限状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "权限状态", example = "1")
    private String status;
    
    /**
     * 是否为外链（1-是，0-否）
     */
    @TableField("is_frame")
    @Schema(description = "是否为外链")
    private String isFrame;
    
    /**
     * 是否缓存（1-缓存，0-不缓存）
     */
    @TableField("is_cache")
    @Schema(description = "是否缓存")
    private String isCache;
    
    /**
     * 是否显示（1-显示，0-隐藏）
     */
    @TableField("visible")
    @Schema(description = "是否显示")
    private String visible;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}