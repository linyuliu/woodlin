package com.mumu.woodlin.tenant.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 租户套餐实体
 *
 * @author yulin
 * @since 2026-06
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant_package")
@Schema(description = "租户套餐")
public class SysTenantPackage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐ID
     */
    @TableId(value = "package_id", type = IdType.ASSIGN_ID)
    @Schema(description = "套餐ID")
    private Long packageId;

    /**
     * 套餐名称
     */
    @TableField("package_name")
    @NotBlank(message = "套餐名称不能为空")
    @Schema(description = "套餐名称")
    private String packageName;

    /**
     * 关联菜单ID集合（逗号分隔）
     */
    @TableField("menu_ids")
    @Schema(description = "关联菜单ID集合（逗号分隔）")
    private String menuIds;

    /**
     * 套餐状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "套餐状态", example = "1")
    private String status;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
