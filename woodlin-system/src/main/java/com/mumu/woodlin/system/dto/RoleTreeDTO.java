package com.mumu.woodlin.system.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色树DTO
 * 
 * @author mumu
 * @description 用于角色层次结构的树形展示（RBAC1）
 * @since 2025-10-31
 */
@Data
@Accessors(chain = true)
@Schema(description = "角色树节点")
public class RoleTreeDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long roleId;
    
    /**
     * 父角色ID
     */
    @Schema(description = "父角色ID")
    private Long parentRoleId;
    
    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;
    
    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String roleCode;
    
    /**
     * 角色层级
     */
    @Schema(description = "角色层级")
    private Integer roleLevel;
    
    /**
     * 角色路径
     */
    @Schema(description = "角色路径")
    private String rolePath;
    
    /**
     * 是否可继承
     */
    @Schema(description = "是否可继承")
    private String isInheritable;
    
    /**
     * 角色状态
     */
    @Schema(description = "角色状态")
    private String status;
    
    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    private Integer sortOrder;
    
    /**
     * 子角色列表
     */
    @Schema(description = "子角色列表")
    private List<RoleTreeDTO> children = new ArrayList<>();
    
    /**
     * 是否有子节点
     */
    @Schema(description = "是否有子节点")
    private Boolean hasChildren;
    
    /**
     * 权限数量（包括继承的）
     */
    @Schema(description = "权限数量")
    private Integer permissionCount;
    
    /**
     * 用户数量
     */
    @Schema(description = "用户数量")
    private Integer userCount;
}
