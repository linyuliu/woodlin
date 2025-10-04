package com.mumu.woodlin.sql2api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * API编排配置实体
 * 
 * @author mumu
 * @description 支持多个API之间的编排和数据流转
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sql2api_orchestration")
@Schema(description = "API编排配置")
public class ApiOrchestration extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 编排ID
     */
    @TableId(value = "orchestration_id", type = IdType.ASSIGN_ID)
    @Schema(description = "编排ID")
    private Long orchestrationId;
    
    /**
     * 编排名称
     */
    @TableField("orchestration_name")
    @Schema(description = "编排名称")
    private String orchestrationName;
    
    /**
     * 编排路径
     */
    @TableField("orchestration_path")
    @Schema(description = "编排路径")
    private String orchestrationPath;
    
    /**
     * 编排配置 (JSON格式)
     * 包含节点、边、条件、转换规则等
     */
    @TableField("orchestration_config")
    @Schema(description = "编排配置")
    private String orchestrationConfig;
    
    /**
     * 执行顺序配置 (JSON数组)
     * 示例: [{"step":1,"apiId":123,"condition":"success"}]
     */
    @TableField("execution_order")
    @Schema(description = "执行顺序配置")
    private String executionOrder;
    
    /**
     * 字段映射配置 (JSON格式)
     * 示例: {"sourceField":"userId","targetField":"uid","transform":"uppercase"}
     */
    @TableField("field_mapping")
    @Schema(description = "字段映射配置")
    private String fieldMapping;
    
    /**
     * 校验规则配置 (JSON格式)
     * 示例: [{"field":"status","operator":"equals","value":"success"}]
     */
    @TableField("validation_rules")
    @Schema(description = "校验规则配置")
    private String validationRules;
    
    /**
     * 错误处理策略 (STOP, CONTINUE, RETRY)
     */
    @TableField("error_strategy")
    @Schema(description = "错误处理策略")
    private String errorStrategy;
    
    /**
     * 超时时间（毫秒）
     */
    @TableField("timeout")
    @Schema(description = "超时时间")
    private Integer timeout;
    
    /**
     * 是否启用
     */
    @TableField("enabled")
    @Schema(description = "是否启用")
    private Boolean enabled;
    
    /**
     * 编排描述
     */
    @TableField("orchestration_desc")
    @Schema(description = "编排描述")
    private String orchestrationDesc;
}
