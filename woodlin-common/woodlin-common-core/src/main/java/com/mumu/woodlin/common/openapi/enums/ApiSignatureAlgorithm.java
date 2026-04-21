package com.mumu.woodlin.common.openapi.enums;

import java.util.Locale;

/**
 * OpenAPI 签名算法。
 *
 * @author mumu
 * @since 2026-04-21
 */
public enum ApiSignatureAlgorithm {

    HMAC_SHA256(true, false),
    HMAC_SHA512(true, false),
    RSA_SHA256(false, false),
    SM2_SM3(false, true);

    private final boolean hmacFamily;
    private final boolean guoMi;

    ApiSignatureAlgorithm(boolean hmacFamily, boolean guoMi) {
        this.hmacFamily = hmacFamily;
        this.guoMi = guoMi;
    }

    public static ApiSignatureAlgorithm of(String value, ApiSignatureAlgorithm fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return ApiSignatureAlgorithm.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public boolean isHmacFamily() {
        return hmacFamily;
    }

    public boolean isGuoMi() {
        return guoMi;
    }
}
