package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 开放应用凭证签发结果。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Data
@Schema(description = "开放应用凭证签发结果")
public class OpenApiCredentialIssueResponse {

    /**
     * 凭证视图。
     */
    private OpenApiCredentialView credential;

    /**
     * 明文 SK，仅首次展示。
     */
    private String secretKey;

    /**
     * 签名私钥，仅首次展示。
     */
    private String signaturePrivateKey;

    /**
     * 客户端解密私钥，仅首次展示。
     */
    private String encryptionPrivateKey;
}
