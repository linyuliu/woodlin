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
 * 开放能力。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_capability")
public class AuthCapability extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "capability_id", type = IdType.ASSIGN_ID)
    private Long capabilityId;

    @TableField("capability_code")
    private String capabilityCode;

    @TableField("capability_name")
    private String capabilityName;

    @TableField("resource_type")
    private String resourceType;

    @TableField("resource_pattern")
    private String resourcePattern;

    @TableField("enabled")
    private String enabled;

    @TableField("tenant_id")
    private String tenantId;
}
