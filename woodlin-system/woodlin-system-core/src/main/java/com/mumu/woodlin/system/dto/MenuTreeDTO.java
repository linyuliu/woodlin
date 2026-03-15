package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点DTO
 *
 * @author mumu
 * @description 菜单/按钮树形结构节点
 * @since 2026-03-15
 */
@Data
@Accessors(chain = true)
@Schema(description = "菜单树节点")
public class MenuTreeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Long menuId;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    private Long parentId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String menuName;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码")
    private String permissionCode;

    /**
     * 权限类型（M-目录，C-菜单，F-按钮）
     */
    @Schema(description = "权限类型")
    private String permissionType;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址")
    private String path;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径")
    private String component;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    private Integer sortOrder;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 显示状态（1-显示，0-隐藏）
     */
    @Schema(description = "显示状态")
    private String visible;

    /**
     * 是否外链（1-是，0-否）
     */
    @Schema(description = "是否外链")
    private String isFrame;

    /**
     * 是否缓存（1-是，0-否）
     */
    @Schema(description = "是否缓存")
    private String isCache;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 子节点
     */
    @Schema(description = "子节点")
    private List<MenuTreeDTO> children = new ArrayList<>();
}
