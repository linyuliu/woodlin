package com.mumu.woodlin.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * 
 * @author mumu
 * @description 提供所有实体类的公共字段，包括创建人、创建时间、更新人、更新时间、删除标识
 * @since 2025-01-01
 */
@Data
@Schema(description = "实体基类")
public abstract class BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private String updateBy;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    /**
     * 删除标识（0-正常，1-删除）
     */
    @TableField("deleted")
    @TableLogic
    @Schema(description = "删除标识")
    private String deleted;
}