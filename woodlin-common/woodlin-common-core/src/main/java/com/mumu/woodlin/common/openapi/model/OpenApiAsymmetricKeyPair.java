package com.mumu.woodlin.common.openapi.model;

/**
 * OpenAPI 非对称密钥对。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiAsymmetricKeyPair {

    private String publicKey;
    private String privateKey;

    public String getPublicKey() {
        return publicKey;
    }

    public OpenApiAsymmetricKeyPair setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public OpenApiAsymmetricKeyPair setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }
}
