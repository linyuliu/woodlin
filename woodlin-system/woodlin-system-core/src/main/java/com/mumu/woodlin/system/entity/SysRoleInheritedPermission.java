package com.mumu.woodlin.system.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色继承权限缓存实体
 * 
 * @author mumu
 * @description 缓存角色的所有权限（包括直接拥有和继承获得），用于性能优化
 * @since 2025-10-31
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_inherited_permission")
@Schema(description = "角色继承权限缓存")
public class SysRoleInheritedPermission {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;
    
    /**
     * 权限ID（包括继承的）
     */
    @TableField("permission_id")
    @Schema(description = "权限ID")
    private Long permissionId;
    
    /**
     * 是否继承而来（0-直接拥有，1-继承获得）
     */
    @TableField("is_inherited")
    @Schema(description = "是否继承而来")
    private String isInherited;
    
    /**
     * 继承自哪个角色（如果是继承权限）
     */
    @TableField("inherited_from")
    @Schema(description = "继承自哪个角色")
    private Long inheritedFrom;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
