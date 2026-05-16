package com.mumu.woodlin.system.model;

import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSecurityMode;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.system.entity.SysOpenApiPolicy;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 开放 API 生效安全配置。
 *
 * @author mumu
 * @since 2026-06-03
 */
@Data
@Accessors(chain = true)
public class EffectiveOpenApiSecurityProfile {

    /**
     * 生效安全模式。
     */
    private ApiSecurityMode securityMode;

    /**
     * 生效签名算法。
     */
    private ApiSignatureAlgorithm signatureAlgorithm;

    /**
     * 生效报文加密算法。
     */
    private ApiEncryptionAlgorithm encryptionAlgorithm;

    /**
     * 是否强制要求请求报文加密。
     */
    private boolean encryptionRequired;

    /**
     * 时间戳窗口秒数。
     */
    private int timestampWindowSeconds;

    /**
     * 是否启用 nonce 防重放。
     */
    private boolean nonceEnabled;

    /**
     * nonce TTL 秒数。
     */
    private int nonceTtlSeconds;

    /**
     * 是否要求租户头。
     */
    private boolean tenantRequired;

    /**
     * IP 白名单。
     */
    private String ipWhitelist;

    /**
     * 是否启用限额。
     */
    private boolean quotaEnabled = true;

    /**
     * 命中的路由策略。
     */
    private SysOpenApiPolicy policy;
}
