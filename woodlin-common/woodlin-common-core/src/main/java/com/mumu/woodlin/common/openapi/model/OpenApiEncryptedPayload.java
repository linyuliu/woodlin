package com.mumu.woodlin.common.openapi.model;

/**
 * OpenAPI 加密负载。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiEncryptedPayload {

    private String algorithm;
    private String encrypted;
    private String iv;

    public String getAlgorithm() {
        return algorithm;
    }

    public OpenApiEncryptedPayload setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public OpenApiEncryptedPayload setEncrypted(String encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    public String getIv() {
        return iv;
    }

    public OpenApiEncryptedPayload setIv(String iv) {
        this.iv = iv;
        return this;
    }
}
