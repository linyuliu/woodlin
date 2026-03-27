package com.mumu.woodlin.system.entity;

import java.io.Serial;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色和权限关联实体
 *
 * @author mumu
 * @description 角色和权限关联关系实体类
 * @since 2026-03-15
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_permission")
@Schema(description = "角色和权限关联")
public class SysRolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 权限ID
     */
    @TableField("permission_id")
    @Schema(description = "权限ID")
    private Long permissionId;

}
