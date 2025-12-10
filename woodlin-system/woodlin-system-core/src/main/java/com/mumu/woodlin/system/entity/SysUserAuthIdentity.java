package com.mumu.woodlin.system.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户多重认证标识
 *
 * @author mumu
 * @description 存储用户的多种登录标识（密码、手机号、SSO、Passkey、TOTP等）的关联信息
 * @since 2025-12-10
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_auth_identity")
@Schema(description = "用户多重认证标识")
public class SysUserAuthIdentity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 认证记录ID
     */
    @TableId(value = "auth_id", type = IdType.ASSIGN_ID)
    @Schema(description = "认证记录ID")
    private Long authId;

    /**
     * 关联用户ID
     */
    @TableField("user_id")
    @Schema(description = "关联用户ID")
    private Long userId;

    /**
     * 认证类型（password、mobile_sms、sso、passkey、totp等）
     */
    @TableField("auth_type")
    @Schema(description = "认证类型")
    private String authType;

    /**
     * 登录标识（用户名、手机号、外部用户ID、Passkey凭证ID等）
     */
    @TableField("identifier")
    @Schema(description = "登录标识")
    private String identifier;

    /**
     * 凭证信息（密钥、哈希、公钥等，根据类型决定含义）
     */
    @TableField("credential")
    @Schema(description = "凭证信息")
    private String credential;

    /**
     * 凭证盐或公钥ID（可选）
     */
    @TableField("credential_salt")
    @Schema(description = "凭证盐或公钥ID")
    private String credentialSalt;

    /**
     * 扩展数据（JSON），用于存储额外元数据
     */
    @TableField("ext_data")
    @Schema(description = "扩展数据（JSON）")
    private String extData;

    /**
     * 是否已完成绑定/验证
     */
    @TableField("verified")
    @Schema(description = "是否已验证")
    private Boolean verified;

    /**
     * 状态（1-启用，0-禁用）
     */
    @TableField("status")
    @Schema(description = "状态")
    private String status;

    /**
     * 最近使用时间
     */
    @TableField("last_used_time")
    @Schema(description = "最近使用时间")
    private LocalDateTime lastUsedTime;

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
