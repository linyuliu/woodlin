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
 * 开放范围。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_scope")
public class AuthScope extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "scope_id", type = IdType.ASSIGN_ID)
    private Long scopeId;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("scope_code")
    private String scopeCode;

    @TableField("scope_name")
    private String scopeName;

    @TableField("actions")
    private String actions;

    @TableField("enabled")
    private String enabled;

    @TableField("tenant_id")
    private String tenantId;
}
