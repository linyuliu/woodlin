package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开放应用凭证展示对象。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Schema(description = "开放应用凭证视图")
public class OpenApiCredentialView {

    /**
     * 凭证ID。
     */
    private Long credentialId;

    /**
     * 应用ID。
     */
    private Long appId;

    /**
     * 凭证名称。
     */
    private String credentialName;

    /**
     * 访问密钥。
     */
    private String accessKey;

    /**
     * 指纹。
     */
    private String secretKeyFingerprint;

    /**
     * 签名算法。
     */
    private String signatureAlgorithm;

    /**
     * 加密算法。
     */
    private String encryptionAlgorithm;

    /**
     * 安全模式。
     */
    private String securityMode;

    /**
     * 生效时间。
     */
    private LocalDateTime activeFrom;

    /**
     * 失效时间。
     */
    private LocalDateTime activeTo;

    /**
     * 最近轮换时间。
     */
    private LocalDateTime lastRotatedTime;

    /**
     * 服务端公钥。
     */
    private String serverPublicKey;

    /**
     * 状态。
     */
    private String status;

    /**
     * 备注。
     */
    private String remark;
}
