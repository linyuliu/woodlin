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
 * 授权权限。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_permission")
@Schema(description = "授权权限")
public class AuthPermission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "permission_id", type = IdType.ASSIGN_ID)
    private Long permissionId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("permission_type")
    private String permissionType;

    @TableField("resource_type")
    private String resourceType;

    @TableField("resource_id")
    private String resourceId;

    @TableField("path")
    private String path;

    @TableField("component")
    private String component;

    @TableField("icon")
    private String icon;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private String status;

    @TableField("is_frame")
    private String isFrame;

    @TableField("is_cache")
    private String isCache;

    @TableField("visible")
    private String visible;

    @TableField("show_in_tabs")
    private String showInTabs;

    @TableField("active_menu")
    private String activeMenu;

    @TableField("redirect")
    private String redirect;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("remark")
    private String remark;
}
