package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开放应用凭证创建或轮换请求。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Schema(description = "开放应用凭证请求")
public class OpenApiCredentialRequest {

    /**
     * 凭证名称。
     */
    @NotBlank(message = "凭证名称不能为空")
    @Schema(description = "凭证名称")
    private String credentialName;

    /**
     * 安全模式。
     */
    @NotBlank(message = "安全模式不能为空")
    @Schema(description = "安全模式")
    private String securityMode;

    /**
     * 签名算法。
     */
    @NotBlank(message = "签名算法不能为空")
    @Schema(description = "签名算法")
    private String signatureAlgorithm;

    /**
     * 加密算法。
     */
    @Schema(description = "加密算法")
    private String encryptionAlgorithm;

    /**
     * 生效时间。
     */
    @Schema(description = "生效时间")
    private LocalDateTime activeFrom;

    /**
     * 失效时间。
     */
    @Schema(description = "失效时间")
    private LocalDateTime activeTo;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
