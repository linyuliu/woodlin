package com.mumu.woodlin.common.openapi.model;

/**
 * OpenAPI 请求签名结果。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiSignResult {

    private String accessKey;
    private String timestamp;
    private String nonce;
    private String requestId;
    private String signatureAlgorithm;
    private String encryptionAlgorithm;
    private String signature;
    private String canonicalRequest;

    public String getAccessKey() {
        return accessKey;
    }

    public OpenApiSignResult setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public OpenApiSignResult setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public OpenApiSignResult setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public OpenApiSignResult setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public OpenApiSignResult setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        return this;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public OpenApiSignResult setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public OpenApiSignResult setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public String getCanonicalRequest() {
        return canonicalRequest;
    }

    public OpenApiSignResult setCanonicalRequest(String canonicalRequest) {
        this.canonicalRequest = canonicalRequest;
        return this;
    }
}
