package com.mumu.woodlin.authorization.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.entity.BaseEntity;

/**
 * 授权策略。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("auth_policy")
@Schema(description = "授权策略")
public class AuthPolicy extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "policy_id", type = IdType.ASSIGN_ID)
    private Long policyId;

    @TableField("policy_code")
    private String policyCode;

    @TableField("policy_name")
    private String policyName;

    @TableField("policy_type")
    private String policyType;

    @TableField("priority")
    private Integer priority;

    @TableField("effect")
    private String effect;

    @TableField("policy_json")
    private String policyJson;

    @TableField("version")
    private Integer version;

    @TableField("enabled")
    private String enabled;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("remark")
    private String remark;
}
