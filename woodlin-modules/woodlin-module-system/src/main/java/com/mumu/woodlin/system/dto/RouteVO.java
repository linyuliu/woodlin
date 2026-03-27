package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 路由信息VO
 * 
 * @author mumu
 * @description 用于前端动态路由的路由信息对象
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "路由信息")
public class RouteVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 路由ID
     */
    @Schema(description = "路由ID")
    private Long id;
    
    /**
     * 父路由ID
     */
    @Schema(description = "父路由ID")
    private Long parentId;
    
    /**
     * 路由名称
     */
    @Schema(description = "路由名称")
    private String name;
    
    /**
     * 路由路径
     */
    @Schema(description = "路由路径")
    private String path;
    
    /**
     * 组件路径
     */
    @Schema(description = "组件路径")
    private String component;
    
    /**
     * 重定向路径
     */
    @Schema(description = "重定向路径")
    private String redirect;
    
    /**
     * 路由元信息
     */
    @Schema(description = "路由元信息")
    private RouteMeta meta;
    
    /**
     * 子路由列表
     */
    @Schema(description = "子路由列表")
    private List<RouteVO> children;
    
    /**
     * 路由元信息
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "路由元信息")
    public static class RouteMeta implements Serializable {
        
        @Serial
        private static final long serialVersionUID = 1L;
        
        /**
         * 路由标题
         */
        @Schema(description = "路由标题")
        private String title;
        
        /**
         * 路由图标
         */
        @Schema(description = "路由图标")
        private String icon;
        
        /**
         * 是否隐藏
         */
        @Schema(description = "是否隐藏")
        private Boolean hideInMenu;
        
        /**
         * 是否固定在标签页
         */
        @Schema(description = "是否固定在标签页")
        private Boolean affix;
        
        /**
         * 是否缓存
         */
        @Schema(description = "是否缓存")
        private Boolean keepAlive;
        
        /**
         * 权限标识列表
         */
        @Schema(description = "权限标识列表")
        private List<String> permissions;
        
        /**
         * 排序顺序
         */
        @Schema(description = "排序顺序")
        private Integer order;
        
        /**
         * 是否为外链
         */
        @Schema(description = "是否为外链")
        private Boolean isFrame;
    }
}
