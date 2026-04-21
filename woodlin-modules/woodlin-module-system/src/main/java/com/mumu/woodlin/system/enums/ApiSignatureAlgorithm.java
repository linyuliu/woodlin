package com.mumu.woodlin.system.enums;

/**
 * 开放 API 签名算法兼容枚举。
 *
 * @author mumu
 * @since 2026-04-21
 */
@Deprecated
public enum ApiSignatureAlgorithm {

    HMAC_SHA256(com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm.HMAC_SHA256),
    HMAC_SHA512(com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm.HMAC_SHA512),
    RSA_SHA256(com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm.RSA_SHA256),
    SM2_SM3(com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm.SM2_SM3);

    private final com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm delegate;

    ApiSignatureAlgorithm(com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm delegate) {
        this.delegate = delegate;
    }

    /**
     * 解析算法，空值时返回默认值。
     *
     * @param value    原始值
     * @param fallback 默认值
     * @return 解析结果
     */
    public static ApiSignatureAlgorithm of(String value, ApiSignatureAlgorithm fallback) {
        com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm resolved =
            com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm.of(value, fallback == null ? null : fallback.delegate);
        return resolved == null ? null : ApiSignatureAlgorithm.valueOf(resolved.name());
    }

    /**
     * 是否为对称签名。
     *
     * @return 是否为对称签名
     */
    public boolean isHmacFamily() {
        return delegate.isHmacFamily();
    }

    /**
     * 是否为国密签名。
     *
     * @return 是否为国密签名
     */
    public boolean isGuoMi() {
        return delegate.isGuoMi();
    }

    public com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm toCore() {
        return delegate;
    }
}
