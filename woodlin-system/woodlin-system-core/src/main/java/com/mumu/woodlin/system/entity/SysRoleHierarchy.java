package com.mumu.woodlin.system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色继承层次关系实体
 *
 * @author mumu
 * @description 使用闭包表模式存储角色继承的完整层次关系，支持RBAC1
 * @since 2025-10-31
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_hierarchy")
@Schema(description = "角色继承层次关系")
public class SysRoleHierarchy {

    private static final long serialVersionUID = 1L;

    /**
     * 祖先角色ID
     */
    @TableField("ancestor_role_id")
    @Schema(description = "祖先角色ID")
    private Long ancestorRoleId;

    /**
     * 后代角色ID
     */
    @TableField("descendant_role_id")
    @Schema(description = "后代角色ID")
    private Long descendantRoleId;

    /**
     * 层级距离（0表示自己）
     */
    @TableField("distance")
    @Schema(description = "层级距离")
    private Integer distance;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
