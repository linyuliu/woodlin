package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 开放 API 全局设置。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Schema(description = "开放API全局设置")
public class OpenApiGlobalSettingsDto {

    /**
     * 默认安全模式。
     */
    @NotBlank(message = "默认安全模式不能为空")
    @Schema(description = "默认安全模式")
    private String defaultMode;

    /**
     * 默认签名算法。
     */
    @NotBlank(message = "默认签名算法不能为空")
    @Schema(description = "默认签名算法")
    private String defaultSignatureAlgorithm;

    /**
     * 时间窗秒数。
     */
    @Min(value = 1, message = "时间窗必须大于0")
    @Schema(description = "时间窗秒数")
    private Integer timestampWindowSeconds;

    /**
     * 是否启用 nonce。
     */
    @Schema(description = "是否启用nonce")
    private Boolean nonceEnabled;

    /**
     * nonce TTL。
     */
    @Min(value = 1, message = "nonce TTL 必须大于0")
    @Schema(description = "nonce TTL 秒数")
    private Integer nonceTtlSeconds;

    /**
     * 默认加密算法。
     */
    @NotBlank(message = "默认加密算法不能为空")
    @Schema(description = "默认加密算法")
    private String defaultEncryptionAlgorithm;

    /**
     * 是否要求加密。
     */
    @Schema(description = "是否要求加密")
    private Boolean encryptionRequired;

    /**
     * 是否启用国密。
     */
    @Schema(description = "是否启用国密")
    private Boolean gmEnabled;
}
