package com.mumu.woodlin.common.openapi.constant;

/**
 * OpenAPI AK/SK 协议请求头常量。
 *
 * <p>该类不依赖 Spring/Jakarta，可直接被其他 Java 项目复用。</p>
 *
 * @author mumu
 * @since 2026-04-21
 */
public final class OpenApiHeaders {

    public static final String AUTHORIZATION = "Authorization";
    public static final String AUTHORIZATION_PREFIX = "Bearer ";
    public static final String ACCESS_KEY = "X-Access-Key";
    public static final String SIGNATURE_ALGORITHM = "X-Signature-Algorithm";
    public static final String TIMESTAMP = "X-Timestamp";
    public static final String NONCE = "X-Nonce";
    public static final String SIGNATURE = "X-Signature";
    public static final String ENCRYPT_ALGORITHM = "X-Encrypt-Algorithm";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String REQUEST_ID = "X-Request-Id";

    private OpenApiHeaders() {
    }
}
