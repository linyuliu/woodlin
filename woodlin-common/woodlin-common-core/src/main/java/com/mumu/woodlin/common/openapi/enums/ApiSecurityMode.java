package com.mumu.woodlin.common.openapi.enums;

import java.util.Locale;

/**
 * OpenAPI 安全模式。
 *
 * @author mumu
 * @since 2026-04-21
 */
public enum ApiSecurityMode {

    NONE(false, false),
    TOKEN(true, false),
    AKSK(false, true),
    TOKEN_AND_AKSK(true, true);

    private final boolean requiresToken;
    private final boolean requiresAksk;

    ApiSecurityMode(boolean requiresToken, boolean requiresAksk) {
        this.requiresToken = requiresToken;
        this.requiresAksk = requiresAksk;
    }

    public static ApiSecurityMode of(String value, ApiSecurityMode fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return ApiSecurityMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public boolean requiresToken() {
        return requiresToken;
    }

    public boolean requiresAksk() {
        return requiresAksk;
    }
}
