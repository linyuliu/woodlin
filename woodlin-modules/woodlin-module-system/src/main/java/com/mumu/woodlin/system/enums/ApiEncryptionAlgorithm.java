package com.mumu.woodlin.system.enums;

/**
 * 开放 API 加密算法兼容枚举。
 *
 * @author mumu
 * @since 2026-04-21
 */
@Deprecated
public enum ApiEncryptionAlgorithm {

    NONE(com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm.NONE),
    AES_GCM(com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm.AES_GCM),
    AES_CBC(com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm.AES_CBC),
    SM4_CBC(com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm.SM4_CBC),
    RSA_OAEP_SHA256(com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm.RSA_OAEP_SHA256);

    private final com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm delegate;

    ApiEncryptionAlgorithm(com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm delegate) {
        this.delegate = delegate;
    }

    /**
     * 解析算法，空值时返回默认值。
     *
     * @param value    原始值
     * @param fallback 默认值
     * @return 解析结果
     */
    public static ApiEncryptionAlgorithm of(String value, ApiEncryptionAlgorithm fallback) {
        com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm resolved =
            com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm.of(value, fallback == null ? null : fallback.delegate);
        return resolved == null ? null : ApiEncryptionAlgorithm.valueOf(resolved.name());
    }

    /**
     * 是否为对称加密。
     *
     * @return 是否为对称加密
     */
    public boolean isSymmetric() {
        return delegate.isSymmetric();
    }

    /**
     * 是否为非对称加密。
     *
     * @return 是否为非对称加密
     */
    public boolean isAsymmetric() {
        return delegate.isAsymmetric();
    }

    /**
     * 是否为国密加密。
     *
     * @return 是否为国密加密
     */
    public boolean isGuoMi() {
        return delegate.isGuoMi();
    }

    public com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm toCore() {
        return delegate;
    }
}
