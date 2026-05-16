package com.mumu.woodlin.authorization.entity;

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
 * 授权角色。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_role")
@Schema(description = "授权角色")
public class AuthRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = IdType.ASSIGN_ID)
    private Long roleId;

    @TableField("parent_role_id")
    private Long parentRoleId;

    @TableField("role_level")
    private Integer roleLevel;

    @TableField("role_path")
    private String rolePath;

    @TableField("role_name")
    private String roleName;

    @TableField("role_code")
    private String roleCode;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("data_scope")
    private String dataScope;

    @TableField("is_inheritable")
    private String inheritable;

    @TableField("status")
    private String status;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("remark")
    private String remark;
}
