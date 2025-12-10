package com.mumu.woodlin.security.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 简化版 SSO Token 验证器
 *
 * <p>Token格式: Base64Url(payload) + "." + Base64Url(HMAC_SHA256(payload, sharedSecret))</p>
 * <p>payload内容: provider|externalId|username(可选)</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SsoTokenVerifier {

    /**
     * 校验并解析SSO令牌
     *
     * @param token        客户端提供的令牌
     * @param expectedProvider 期望的SSO提供商
     * @param sharedSecret 签名密钥
     * @return 解析后的主体信息
     */
    public static SsoPrincipal parse(String token, String expectedProvider, String sharedSecret) {
        if (StrUtil.isBlank(token)) {
            throw new IllegalArgumentException("SSO令牌为空");
        }
        if (StrUtil.isBlank(sharedSecret)) {
            throw new IllegalArgumentException("SSO签名密钥未配置");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("SSO令牌格式不正确");
        }

        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[0]);
        byte[] signature = Base64.getUrlDecoder().decode(parts[1]);

        byte[] expectedSignature = sign(sharedSecret, payloadBytes);
        if (!MessageDigest.isEqual(signature, expectedSignature)) {
            throw new IllegalArgumentException("SSO令牌签名校验失败");
        }

        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        String[] fields = payload.split("\\|");
        if (fields.length < 2) {
            throw new IllegalArgumentException("SSO令牌内容不完整");
        }

        String provider = fields[0];
        if (StrUtil.isNotBlank(expectedProvider) && !provider.equalsIgnoreCase(expectedProvider)) {
            throw new IllegalArgumentException("SSO提供商不匹配");
        }

        String externalId = fields[1];
        String username = fields.length > 2 ? fields[2] : externalId;

        return new SsoPrincipal(provider, externalId, username);
    }

    private static byte[] sign(String sharedSecret, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("SSO令牌签名计算失败", ex);
        }
    }

    /**
     * SSO令牌主体信息
     */
    @Getter
    @AllArgsConstructor
    public static class SsoPrincipal {
        private final String provider;
        private final String externalId;
        private final String username;
    }
}
