package com.mumu.woodlin.sql2api.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * SQL API配置实体
 * 
 * @author mumu
 * @description 通过配置SQL语句和参数动态生成API接口
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sql2api_config")
@Schema(description = "SQL API配置")
public class SqlApiConfig extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * API ID
     */
    @TableId(value = "api_id", type = IdType.ASSIGN_ID)
    @Schema(description = "API ID")
    private Long apiId;
    
    /**
     * API名称
     */
    @TableField("api_name")
    @Schema(description = "API名称")
    private String apiName;
    
    /**
     * API路径 (如 /api/user/list)
     */
    @TableField("api_path")
    @Schema(description = "API路径")
    private String apiPath;
    
    /**
     * 请求方法 (GET, POST, PUT, DELETE)
     */
    @TableField("http_method")
    @Schema(description = "请求方法")
    private String httpMethod;
    
    /**
     * 数据源名称
     */
    @TableField("datasource_name")
    @Schema(description = "数据源名称")
    private String datasourceName;
    
    /**
     * SQL类型 (SELECT, INSERT, UPDATE, DELETE)
     */
    @TableField("sql_type")
    @Schema(description = "SQL类型")
    private String sqlType;
    
    /**
     * SQL语句 (支持MyBatis动态SQL语法)
     */
    @TableField("sql_content")
    @Schema(description = "SQL语句")
    private String sqlContent;
    
    /**
     * 参数配置 (JSON格式)
     * 示例: [{"name":"userId","type":"Long","required":true,"desc":"用户ID"}]
     */
    @TableField("params_config")
    @Schema(description = "参数配置")
    private String paramsConfig;
    
    /**
     * 返回结果类型 (single, list, page)
     */
    @TableField("result_type")
    @Schema(description = "返回结果类型")
    private String resultType;
    
    /**
     * 是否启用缓存
     */
    @TableField("cache_enabled")
    @Schema(description = "是否启用缓存")
    private Boolean cacheEnabled;
    
    /**
     * 缓存过期时间（秒）
     */
    @TableField("cache_expire")
    @Schema(description = "缓存过期时间")
    private Integer cacheExpire;
    
    /**
     * 是否启用加密
     */
    @TableField("encrypt_enabled")
    @Schema(description = "是否启用加密")
    private Boolean encryptEnabled;
    
    /**
     * 加密算法 (AES, RSA, SM4)
     */
    @TableField("encrypt_algorithm")
    @Schema(description = "加密算法")
    private String encryptAlgorithm;
    
    /**
     * 是否需要认证
     */
    @TableField("auth_required")
    @Schema(description = "是否需要认证")
    private Boolean authRequired;
    
    /**
     * 认证类型 (TOKEN, API_KEY, NONE)
     */
    @TableField("auth_type")
    @Schema(description = "认证类型")
    private String authType;
    
    /**
     * 流控配置 (QPS限制)
     */
    @TableField("flow_limit")
    @Schema(description = "流控配置")
    private Integer flowLimit;
    
    /**
     * API描述
     */
    @TableField("api_desc")
    @Schema(description = "API描述")
    private String apiDesc;
    
    /**
     * 是否启用
     */
    @TableField("enabled")
    @Schema(description = "是否启用")
    private Boolean enabled;
    
    /**
     * 状态 (0=正常, 1=禁用)
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;
}
