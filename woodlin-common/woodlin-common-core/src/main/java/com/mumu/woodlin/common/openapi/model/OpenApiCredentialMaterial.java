package com.mumu.woodlin.common.openapi.model;

/**
 * OpenAPI 凭证材料。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiCredentialMaterial {

    private String accessKey;
    private String secretKey;
    private String signaturePublicKey;
    private String signaturePrivateKey;
    private String encryptionPublicKey;
    private String encryptionPrivateKey;
    private String serverPublicKey;
    private String serverPrivateKey;

    public String getAccessKey() {
        return accessKey;
    }

    public OpenApiCredentialMaterial setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public OpenApiCredentialMaterial setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getSignaturePublicKey() {
        return signaturePublicKey;
    }

    public OpenApiCredentialMaterial setSignaturePublicKey(String signaturePublicKey) {
        this.signaturePublicKey = signaturePublicKey;
        return this;
    }

    public String getSignaturePrivateKey() {
        return signaturePrivateKey;
    }

    public OpenApiCredentialMaterial setSignaturePrivateKey(String signaturePrivateKey) {
        this.signaturePrivateKey = signaturePrivateKey;
        return this;
    }

    public String getEncryptionPublicKey() {
        return encryptionPublicKey;
    }

    public OpenApiCredentialMaterial setEncryptionPublicKey(String encryptionPublicKey) {
        this.encryptionPublicKey = encryptionPublicKey;
        return this;
    }

    public String getEncryptionPrivateKey() {
        return encryptionPrivateKey;
    }

    public OpenApiCredentialMaterial setEncryptionPrivateKey(String encryptionPrivateKey) {
        this.encryptionPrivateKey = encryptionPrivateKey;
        return this;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public OpenApiCredentialMaterial setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
        return this;
    }

    public String getServerPrivateKey() {
        return serverPrivateKey;
    }

    public OpenApiCredentialMaterial setServerPrivateKey(String serverPrivateKey) {
        this.serverPrivateKey = serverPrivateKey;
        return this;
    }
}
