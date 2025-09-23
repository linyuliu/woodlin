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
 * 系统配置实体
 * 
 * @author mumu
 * @description 系统配置信息实体类，用于系统参数配置管理
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
@Schema(description = "系统配置")
public class SysConfig extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    @Schema(description = "配置ID")
    private Long configId;
    
    /**
     * 配置名称
     */
    @TableField("config_name")
    @Schema(description = "配置名称")
    private String configName;
    
    /**
     * 配置键名
     */
    @TableField("config_key")
    @Schema(description = "配置键名")
    private String configKey;
    
    /**
     * 配置键值
     */
    @TableField("config_value")
    @Schema(description = "配置键值")
    private String configValue;
    
    /**
     * 配置类型（Y-系统内置，N-用户自定义）
     */
    @TableField("config_type")
    @Schema(description = "配置类型")
    private String configType;
    
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