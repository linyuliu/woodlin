package com.mumu.woodlin.system.util;

import com.mumu.woodlin.common.openapi.constant.OpenApiConfigKeys;
import com.mumu.woodlin.common.openapi.constant.OpenApiHeaders;

/**
 * 开放 API 安全中心常量。
 *
 * @author mumu
 * @since 2026-04-13
 */
public final class OpenApiSecurityConstants {

    /**
     * 开放 API 前缀。
     */
    public static final String OPEN_API_PATH_PREFIX = "/openapi/";

    /**
     * 访问凭证头。
     */
    public static final String HEADER_ACCESS_KEY = OpenApiHeaders.ACCESS_KEY;

    /**
     * 签名算法头。
     */
    public static final String HEADER_SIGNATURE_ALGORITHM = OpenApiHeaders.SIGNATURE_ALGORITHM;

    /**
     * 时间戳头。
     */
    public static final String HEADER_TIMESTAMP = OpenApiHeaders.TIMESTAMP;

    /**
     * 随机串头。
     */
    public static final String HEADER_NONCE = OpenApiHeaders.NONCE;

    /**
     * 签名头。
     */
    public static final String HEADER_SIGNATURE = OpenApiHeaders.SIGNATURE;

    /**
     * 加密算法头。
     */
    public static final String HEADER_ENCRYPT_ALGORITHM = OpenApiHeaders.ENCRYPT_ALGORITHM;

    /**
     * 租户头。
     */
    public static final String HEADER_TENANT_ID = OpenApiHeaders.TENANT_ID;

    /**
     * 请求追踪头。
     */
    public static final String HEADER_REQUEST_ID = OpenApiHeaders.REQUEST_ID;

    /**
     * 认证头。
     */
    public static final String HEADER_AUTHORIZATION = OpenApiHeaders.AUTHORIZATION;

    /**
     * Bearer 前缀。
     */
    public static final String AUTHORIZATION_PREFIX = OpenApiHeaders.AUTHORIZATION_PREFIX;

    /**
     * Nonce Redis 前缀。
     */
    public static final String NONCE_CACHE_PREFIX = OpenApiConfigKeys.DEFAULT_NONCE_CACHE_PREFIX;

    /**
     * 策略缓存键。
     */
    public static final String POLICY_CACHE_KEY = "open_api_policy:enabled";

    /**
     * 配置键: 默认安全模式。
     */
    public static final String CONFIG_DEFAULT_MODE = OpenApiConfigKeys.DEFAULT_MODE;

    /**
     * 配置键: 默认签名算法。
     */
    public static final String CONFIG_DEFAULT_SIGNATURE_ALGORITHM = OpenApiConfigKeys.DEFAULT_SIGNATURE_ALGORITHM;

    /**
     * 配置键: 时间窗。
     */
    public static final String CONFIG_TIMESTAMP_WINDOW = OpenApiConfigKeys.TIMESTAMP_WINDOW_SECONDS;

    /**
     * 配置键: 是否启用 nonce。
     */
    public static final String CONFIG_NONCE_ENABLED = OpenApiConfigKeys.NONCE_ENABLED;

    /**
     * 配置键: nonce TTL。
     */
    public static final String CONFIG_NONCE_TTL = OpenApiConfigKeys.NONCE_TTL_SECONDS;

    /**
     * 配置键: 默认加密算法。
     */
    public static final String CONFIG_DEFAULT_ENCRYPTION_ALGORITHM = OpenApiConfigKeys.DEFAULT_ENCRYPTION_ALGORITHM;

    /**
     * 配置键: 是否要求加密。
     */
    public static final String CONFIG_ENCRYPTION_REQUIRED = OpenApiConfigKeys.ENCRYPTION_REQUIRED;

    /**
     * 配置键: 国密开关。
     */
    public static final String CONFIG_GM_ENABLED = OpenApiConfigKeys.GM_ENABLED;

    /**
     * 配置键: 主密钥。
     */
    public static final String CONFIG_MASTER_KEY = OpenApiConfigKeys.MASTER_KEY;

    /**
     * 响应字段: 加密负载。
     */
    public static final String FIELD_ENCRYPTED = "encrypted";

    /**
     * 响应字段: 向量。
     */
    public static final String FIELD_IV = "iv";

    /**
     * 响应字段: 算法。
     */
    public static final String FIELD_ALGORITHM = "algorithm";

    private OpenApiSecurityConstants() {
    }
}
