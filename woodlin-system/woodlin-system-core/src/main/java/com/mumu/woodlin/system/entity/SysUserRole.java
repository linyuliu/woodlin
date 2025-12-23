package com.mumu.woodlin.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户和角色关联实体
 * 
 * @author mumu
 * @description 用户和角色关联关系实体类
 * @since 2025-01-15
 */
@Data
@Accessors(chain = true)
@TableName("sys_user_role")
@Schema(description = "用户和角色关联")
public class SysUserRole implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;
    
}
