package com.mumu.woodlin.authorization.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 开放接口资源目录。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_open_api_resource")
public class AuthOpenApiResource extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "resource_id", type = IdType.ASSIGN_ID)
    private Long resourceId;

    @TableField("resource_code")
    private String resourceCode;

    @TableField("resource_name")
    private String resourceName;

    @TableField("http_method")
    private String httpMethod;

    @TableField("path_pattern")
    private String pathPattern;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("scope_id")
    private Long scopeId;

    @TableField("auth_mode")
    private String authMode;

    @TableField("status")
    private String status;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("remark")
    private String remark;
}
