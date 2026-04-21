package com.mumu.woodlin.common.openapi.exception;

/**
 * OpenAPI 安全核心异常。
 *
 * @author mumu
 * @since 2026-04-21
 */
public class OpenApiSecurityException extends RuntimeException {

    public OpenApiSecurityException(String message) {
        super(message);
    }

    public OpenApiSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
