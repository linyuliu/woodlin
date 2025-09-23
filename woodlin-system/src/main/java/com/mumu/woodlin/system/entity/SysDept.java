package com.mumu.woodlin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 部门信息实体
 * 
 * @author mumu
 * @description 系统部门信息实体类，支持树形结构的部门管理
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@Schema(description = "部门信息")
public class SysDept extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 部门ID
     */
    @TableId(value = "dept_id", type = IdType.ASSIGN_ID)
    @Schema(description = "部门ID")
    private Long deptId;
    
    /**
     * 父部门ID
     */
    @TableField("parent_id")
    @Schema(description = "父部门ID")
    private Long parentId;
    
    /**
     * 祖级列表（父级路径）
     */
    @TableField("ancestors")
    @Schema(description = "祖级列表")
    private String ancestors;
    
    /**
     * 部门名称
     */
    @TableField("dept_name")
    @Schema(description = "部门名称")
    private String deptName;
    
    /**
     * 部门编码
     */
    @TableField("dept_code")
    @Schema(description = "部门编码")
    private String deptCode;
    
    /**
     * 显示顺序
     */
    @TableField("sort_order")
    @Schema(description = "显示顺序")
    private Integer sortOrder;
    
    /**
     * 负责人
     */
    @TableField("leader")
    @Schema(description = "负责人")
    private String leader;
    
    /**
     * 联系电话
     */
    @TableField("phone")
    @Schema(description = "联系电话")
    private String phone;
    
    /**
     * 邮箱
     */
    @TableField("email")
    @Schema(description = "邮箱")
    private String email;
    
    /**
     * 部门状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "部门状态", example = "1")
    private String status;
    
    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;
    
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}