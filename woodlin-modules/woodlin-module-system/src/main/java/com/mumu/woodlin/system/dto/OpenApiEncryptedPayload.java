package com.mumu.woodlin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 开放 API 加密报文。
 *
 * @author mumu
 * @since 2026-04-13
 */
@Schema(description = "开放API加密报文")
public class OpenApiEncryptedPayload extends com.mumu.woodlin.common.openapi.model.OpenApiEncryptedPayload {

    @Override
    public OpenApiEncryptedPayload setAlgorithm(String algorithm) {
        super.setAlgorithm(algorithm);
        return this;
    }

    @Override
    public OpenApiEncryptedPayload setEncrypted(String encrypted) {
        super.setEncrypted(encrypted);
        return this;
    }

    @Override
    public OpenApiEncryptedPayload setIv(String iv) {
        super.setIv(iv);
        return this;
    }
}
