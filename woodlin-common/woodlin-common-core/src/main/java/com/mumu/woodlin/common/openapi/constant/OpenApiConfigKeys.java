package com.mumu.woodlin.common.openapi.constant;

/**
 * OpenAPI AK/SK 协议配置键常量。
 *
 * @author mumu
 * @since 2026-04-21
 */
public final class OpenApiConfigKeys {

    public static final String DEFAULT_MODE = "api.security.default-mode";
    public static final String DEFAULT_SIGNATURE_ALGORITHM = "api.signature.default-algorithm";
    public static final String TIMESTAMP_WINDOW_SECONDS = "api.signature.timestamp-window-seconds";
    public static final String NONCE_ENABLED = "api.signature.nonce-enabled";
    public static final String NONCE_TTL_SECONDS = "api.signature.nonce-ttl-seconds";
    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "api.encryption.default-algorithm";
    public static final String ENCRYPTION_REQUIRED = "api.encryption.required";
    public static final String GM_ENABLED = "api.gm.enabled";
    public static final String MASTER_KEY = "api.security.master-key";
    public static final String DEFAULT_NONCE_CACHE_PREFIX = "openapi:nonce:";

    private OpenApiConfigKeys() {
    }
}
