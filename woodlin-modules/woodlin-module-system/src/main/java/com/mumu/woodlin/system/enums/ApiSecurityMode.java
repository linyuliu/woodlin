package com.mumu.woodlin.system.enums;

/**
 * 开放 API 安全模式兼容枚举。
 *
 * <p>统一协议定义已迁移到 common-core，
 * 当前系统模块继续保留旧枚举名以减少业务改动。</p>
 *
 * @author mumu
 * @since 2026-04-21
 */
@Deprecated
public enum ApiSecurityMode {

    NONE(com.mumu.woodlin.common.openapi.enums.ApiSecurityMode.NONE),
    TOKEN(com.mumu.woodlin.common.openapi.enums.ApiSecurityMode.TOKEN),
    AKSK(com.mumu.woodlin.common.openapi.enums.ApiSecurityMode.AKSK),
    TOKEN_AND_AKSK(com.mumu.woodlin.common.openapi.enums.ApiSecurityMode.TOKEN_AND_AKSK);

    private final com.mumu.woodlin.common.openapi.enums.ApiSecurityMode delegate;

    ApiSecurityMode(com.mumu.woodlin.common.openapi.enums.ApiSecurityMode delegate) {
        this.delegate = delegate;
    }

    /**
     * 解析模式，空值时返回默认值。
     *
     * @param value    原始值
     * @param fallback 默认值
     * @return 解析结果
     */
    public static ApiSecurityMode of(String value, ApiSecurityMode fallback) {
        com.mumu.woodlin.common.openapi.enums.ApiSecurityMode resolved =
            com.mumu.woodlin.common.openapi.enums.ApiSecurityMode.of(value, fallback == null ? null : fallback.delegate);
        return resolved == null ? null : ApiSecurityMode.valueOf(resolved.name());
    }

    /**
     * 是否需要 Token。
     *
     * @return 是否需要 Token
     */
    public boolean requiresToken() {
        return delegate.requiresToken();
    }

    /**
     * 是否需要 AKSK。
     *
     * @return 是否需要 AKSK
     */
    public boolean requiresAksk() {
        return delegate.requiresAksk();
    }

    public com.mumu.woodlin.common.openapi.enums.ApiSecurityMode toCore() {
        return delegate;
    }
}
