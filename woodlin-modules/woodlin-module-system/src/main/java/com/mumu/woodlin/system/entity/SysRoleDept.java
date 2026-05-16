package com.mumu.woodlin.system.entity;

import java.io.Serial;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色与部门关联实体
 *
 * @author yulin
 * @since 2026-05
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_dept")
@Schema(description = "角色与部门关联")
public class SysRoleDept implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    @TableField("dept_id")
    @Schema(description = "部门ID")
    private Long deptId;
}
