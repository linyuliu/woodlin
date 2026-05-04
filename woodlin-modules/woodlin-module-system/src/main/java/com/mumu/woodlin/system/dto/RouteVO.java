package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 动态路由信息VO
 *
 * <p>前端动态路由下发的路由节点结构，与前端 {@code RouteItem} 接口一一对应。
 * 扁平字段风格，避免嵌套 meta 序列化歧义。</p>
 *
 * @author mumu
 * @description 用于 GET /system/user/route 下发给前端的动态路由信息
 * @since 2026-05
 */
@Data
@Accessors(chain = true)
@Schema(description = "动态路由信息")
public class RouteVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 路由ID */
    @Schema(description = "路由ID")
    private Long id;

    /** 父路由ID（根节点为 0） */
    @Schema(description = "父路由ID")
    private Long parentId;

    /**
     * 节点类型：1-目录（Layout/ParentView），2-菜单页面，3-按钮权限
     */
    @Schema(description = "节点类型：1-目录，2-菜单，3-按钮")
    private Integer type;

    /** 路由名称（PascalCase，如 SystemUser） */
    @Schema(description = "路由名称")
    private String name;

    /** 标题（菜单显示文本） */
    @Schema(description = "标题")
    private String title;

    /** 路由路径（如 /system/user） */
    @Schema(description = "路由路径")
    private String path;

    /**
     * 组件字符串：'Layout' / 'ParentView' / 'system/user/index'
     * 前端通过 import.meta.glob 映射
     */
    @Schema(description = "组件路径字符串")
    private String component;

    /** 重定向路径（目录类型可配置） */
    @Schema(description = "重定向路径")
    private String redirect;

    /** 图标（格式：vicons:antd:UserOutlined） */
    @Schema(description = "图标")
    private String icon;

    /** 权限标识（按钮类型节点，如 system:user:add） */
    @Schema(description = "权限标识")
    private String permission;

    /** 是否隐藏（true=隐藏，不出现在左侧导航） */
    @Schema(description = "是否隐藏")
    private Boolean isHidden;

    /** 是否缓存（keep-alive） */
    @Schema(description = "是否缓存")
    private Boolean isCache;

    /** 是否为外链（在新标签页打开） */
    @Schema(description = "是否为外链")
    private Boolean isFrame;

    /** 是否在标签页固定显示 */
    @Schema(description = "是否在标签页显示")
    private Boolean showInTabs;

    /**
     * 高亮菜单路径（详情页等无独立菜单场景下用于高亮父菜单，
     * 例如 /system/user/detail 的 activeMenu 为 /system/user）
     */
    @Schema(description = "高亮菜单路径")
    private String activeMenu;

    /** 显示排序 */
    @Schema(description = "排序")
    private Integer sort;

    /** 子路由（树形递归） */
    @Schema(description = "子路由列表")
    private List<RouteVO> children;
}
