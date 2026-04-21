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
import java.time.LocalDateTime;

/**
 * 开放应用凭证实体。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_open_app_credential")
@Schema(description = "开放应用凭证")
public class SysOpenAppCredential extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 凭证ID。
     */
    @TableId(value = "credential_id", type = IdType.ASSIGN_ID)
    @Schema(description = "凭证ID")
    private Long credentialId;

    /**
     * 应用ID。
     */
    @TableField("app_id")
    @Schema(description = "应用ID")
    private Long appId;

    /**
     * 凭证名称。
     */
    @TableField("credential_name")
    @Schema(description = "凭证名称")
    private String credentialName;

    /**
     * 访问密钥。
     */
    @TableField("access_key")
    @Schema(description = "访问密钥")
    private String accessKey;

    /**
     * 加密存储的密钥。
     */
    @TableField("secret_key_encrypted")
    @Schema(description = "加密存储的密钥")
    private String secretKeyEncrypted;

    /**
     * 密钥指纹。
     */
    @TableField("secret_key_fingerprint")
    @Schema(description = "密钥指纹")
    private String secretKeyFingerprint;

    /**
     * 签名公钥。
     */
    @TableField("signature_public_key")
    @Schema(description = "签名公钥")
    private String signaturePublicKey;

    /**
     * 客户端加密公钥。
     */
    @TableField("encryption_public_key")
    @Schema(description = "客户端加密公钥")
    private String encryptionPublicKey;

    /**
     * 服务端加密公钥。
     */
    @TableField("server_public_key")
    @Schema(description = "服务端加密公钥")
    private String serverPublicKey;

    /**
     * 加密存储的服务端私钥。
     */
    @TableField("server_private_key_encrypted")
    @Schema(description = "服务端私钥")
    private String serverPrivateKeyEncrypted;

    /**
     * 签名算法。
     */
    @TableField("signature_algorithm")
    @Schema(description = "签名算法")
    private String signatureAlgorithm;

    /**
     * 加密算法。
     */
    @TableField("encryption_algorithm")
    @Schema(description = "加密算法")
    private String encryptionAlgorithm;

    /**
     * 安全模式。
     */
    @TableField("security_mode")
    @Schema(description = "安全模式")
    private String securityMode;

    /**
     * 生效时间。
     */
    @TableField("active_from")
    @Schema(description = "生效时间")
    private LocalDateTime activeFrom;

    /**
     * 失效时间。
     */
    @TableField("active_to")
    @Schema(description = "失效时间")
    private LocalDateTime activeTo;

    /**
     * 最近轮换时间。
     */
    @TableField("last_rotated_time")
    @Schema(description = "最近轮换时间")
    private LocalDateTime lastRotatedTime;

    /**
     * 凭证状态。
     */
    @TableField("status")
    @Schema(description = "凭证状态")
    private String status;

    /**
     * 备注。
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
