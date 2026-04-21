package com.mumu.woodlin.common.openapi.enums;

import java.util.Locale;

/**
 * OpenAPI 加密算法。
 *
 * @author mumu
 * @since 2026-04-21
 */
public enum ApiEncryptionAlgorithm {

    NONE(true, false, false),
    AES_GCM(true, false, false),
    AES_CBC(true, false, false),
    RSA_OAEP_SHA256(false, true, false),
    SM4_CBC(true, false, true);

    private final boolean symmetric;
    private final boolean asymmetric;
    private final boolean guoMi;

    ApiEncryptionAlgorithm(boolean symmetric, boolean asymmetric, boolean guoMi) {
        this.symmetric = symmetric;
        this.asymmetric = asymmetric;
        this.guoMi = guoMi;
    }

    public static ApiEncryptionAlgorithm of(String value, ApiEncryptionAlgorithm fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return ApiEncryptionAlgorithm.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public boolean isSymmetric() {
        return symmetric && this != NONE;
    }

    public boolean isAsymmetric() {
        return asymmetric;
    }

    public boolean isGuoMi() {
        return guoMi;
    }
}
