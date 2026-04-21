package com.mumu.woodlin.common.openapi.model;

/**
 * OpenAPI 验签结果。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiVerifyResult {

    private boolean verified;
    private String reason;
    private String canonicalRequest;

    public boolean isVerified() {
        return verified;
    }

    public OpenApiVerifyResult setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public OpenApiVerifyResult setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getCanonicalRequest() {
        return canonicalRequest;
    }

    public OpenApiVerifyResult setCanonicalRequest(String canonicalRequest) {
        this.canonicalRequest = canonicalRequest;
        return this;
    }
}
