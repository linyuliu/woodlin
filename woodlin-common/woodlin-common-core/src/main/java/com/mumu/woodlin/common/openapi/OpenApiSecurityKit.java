package com.mumu.woodlin.common.openapi;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import com.mumu.woodlin.common.openapi.constant.OpenApiConfigKeys;
import com.mumu.woodlin.common.openapi.constant.OpenApiHeaders;
import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.common.openapi.exception.OpenApiSecurityException;
import com.mumu.woodlin.common.openapi.model.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * OpenAPI AK/SK 协议核心工具。
 *
 * <p>该工具类保持纯 Java API，不暴露 Spring/Jakarta 类型，可被 JDK8+ 项目直接复用。</p>
 *
 * @author mumu
 * @since 2026-04-21
 */
public final class OpenApiSecurityKit {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int CBC_IV_LENGTH = 16;
    private static final int AES_KEY_LENGTH = 32;
    private static final int SM4_KEY_LENGTH = 16;
    private static final int RSA_KEY_SIZE = 2048;
    private static final int RSA_DIRECT_PAYLOAD_MAX_LENGTH = 190;

    static {
        if (Security.getProvider(BOUNCY_CASTLE_PROVIDER.getName()) == null) {
            Security.addProvider(BOUNCY_CASTLE_PROVIDER);
        }
    }

    private OpenApiSecurityKit() {
    }

    public static String generateAccessKey() {
        return "ak_" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSecretKey() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static OpenApiAsymmetricKeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(RSA_KEY_SIZE, SECURE_RANDOM);
            KeyPair keyPair = generator.generateKeyPair();
            return new OpenApiAsymmetricKeyPair()
                .setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()))
                .setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("生成 RSA 密钥对失败", ex);
        }
    }

    public static OpenApiAsymmetricKeyPair generateSm2KeyPair() {
        SM2 sm2 = SmUtil.sm2();
        return new OpenApiAsymmetricKeyPair()
            .setPublicKey(sm2.getPublicKeyBase64())
            .setPrivateKey(sm2.getPrivateKeyBase64());
    }

    public static String fingerprint(String value) {
        if (!hasText(value)) {
            return "";
        }
        return sha256Hex(value.getBytes(StandardCharsets.UTF_8)).substring(0, 16);
    }

    public static String protect(String plainText, String masterKey) {
        if (!hasText(plainText)) {
            return plainText;
        }
        try {
            byte[] iv = randomBytes(GCM_IV_LENGTH);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(deriveKey(masterKey, AES_KEY_LENGTH), "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("敏感数据加密失败", ex);
        }
    }

    public static String reveal(String protectedText, String masterKey) {
        if (!hasText(protectedText)) {
            return protectedText;
        }
        String[] segments = protectedText.split(":", 2);
        if (segments.length != 2) {
            throw new OpenApiSecurityException("密文格式错误");
        }
        try {
            byte[] iv = Base64.getDecoder().decode(segments[0]);
            byte[] encrypted = Base64.getDecoder().decode(segments[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(deriveKey(masterKey, AES_KEY_LENGTH), "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("敏感数据解密失败", ex);
        }
    }

    public static String buildCanonicalRequest(OpenApiRequestContext requestContext) {
        if (requestContext == null) {
            throw new OpenApiSecurityException("请求上下文不能为空");
        }
        return joinWithLineBreak(
            normalizeMethod(requestContext.getMethod()),
            safe(requestContext.getPath()),
            buildSortedQuery(requestContext.getQueryParameters()),
            sha256Hex(requestContext.getBody()),
            safe(requestContext.getTimestamp()),
            safe(requestContext.getNonce()),
            safe(requestContext.getTenantId()),
            safe(requestContext.getAccessKey())
        );
    }

    public static String sign(byte[] data, ApiSignatureAlgorithm algorithm, OpenApiCredentialMaterial material) {
        ApiSignatureAlgorithm resolvedAlgorithm = algorithm == null ? ApiSignatureAlgorithm.HMAC_SHA256 : algorithm;
        OpenApiCredentialMaterial resolvedMaterial = material == null ? new OpenApiCredentialMaterial() : material;
        try {
            if (resolvedAlgorithm == ApiSignatureAlgorithm.HMAC_SHA256) {
                return hmac(data, "HmacSHA256", resolvedMaterial.getSecretKey());
            }
            if (resolvedAlgorithm == ApiSignatureAlgorithm.HMAC_SHA512) {
                return hmac(data, "HmacSHA512", resolvedMaterial.getSecretKey());
            }
            if (resolvedAlgorithm == ApiSignatureAlgorithm.RSA_SHA256) {
                return signRsa(data, resolvedMaterial.getSignaturePrivateKey());
            }
            if (resolvedAlgorithm == ApiSignatureAlgorithm.SM2_SM3) {
                return signSm2(data, resolvedMaterial.getSignaturePrivateKey());
            }
            throw new OpenApiSecurityException("不支持的签名算法: " + resolvedAlgorithm.name());
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("签名失败", ex);
        }
    }

    public static OpenApiSignResult sign(OpenApiRequestContext requestContext, OpenApiCredentialMaterial material) {
        OpenApiRequestContext normalizedContext = normalizeRequestContext(requestContext, material);
        String canonicalRequest = buildCanonicalRequest(normalizedContext);
        String signature = sign(
            canonicalRequest.getBytes(StandardCharsets.UTF_8),
            normalizedContext.getSignatureAlgorithm(),
            material
        );
        return new OpenApiSignResult()
            .setAccessKey(normalizedContext.getAccessKey())
            .setTimestamp(normalizedContext.getTimestamp())
            .setNonce(normalizedContext.getNonce())
            .setRequestId(normalizedContext.getRequestId())
            .setSignatureAlgorithm(normalizedContext.getSignatureAlgorithm().name())
            .setEncryptionAlgorithm(
                normalizedContext.getEncryptionAlgorithm() == null ? null : normalizedContext.getEncryptionAlgorithm().name()
            )
            .setSignature(signature)
            .setCanonicalRequest(canonicalRequest);
    }

    public static OpenApiVerifyResult verify(byte[] data, String signature, ApiSignatureAlgorithm algorithm,
                                             OpenApiCredentialMaterial material) {
        ApiSignatureAlgorithm resolvedAlgorithm = algorithm == null ? ApiSignatureAlgorithm.HMAC_SHA256 : algorithm;
        OpenApiCredentialMaterial resolvedMaterial = material == null ? new OpenApiCredentialMaterial() : material;
        boolean verified;
        try {
            if (resolvedAlgorithm == ApiSignatureAlgorithm.HMAC_SHA256) {
                verified = equalsText(signature, hmac(data, "HmacSHA256", resolvedMaterial.getSecretKey()));
            } else if (resolvedAlgorithm == ApiSignatureAlgorithm.HMAC_SHA512) {
                verified = equalsText(signature, hmac(data, "HmacSHA512", resolvedMaterial.getSecretKey()));
            } else if (resolvedAlgorithm == ApiSignatureAlgorithm.RSA_SHA256) {
                verified = verifyRsa(data, signature, resolvedMaterial.getSignaturePublicKey());
            } else if (resolvedAlgorithm == ApiSignatureAlgorithm.SM2_SM3) {
                verified = verifySm2(data, signature, resolvedMaterial.getSignaturePublicKey());
            } else {
                throw new OpenApiSecurityException("不支持的签名算法: " + resolvedAlgorithm.name());
            }
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("签名校验失败", ex);
        }
        return new OpenApiVerifyResult()
            .setVerified(verified)
            .setReason(verified ? null : "签名校验失败");
    }

    public static OpenApiVerifyResult verify(OpenApiRequestContext requestContext, String signature,
                                             OpenApiCredentialMaterial material) {
        OpenApiRequestContext normalizedContext = normalizeRequestContext(requestContext, material);
        String canonicalRequest = buildCanonicalRequest(normalizedContext);
        OpenApiVerifyResult result = verify(
            canonicalRequest.getBytes(StandardCharsets.UTF_8),
            signature,
            normalizedContext.getSignatureAlgorithm(),
            material
        );
        result.setCanonicalRequest(canonicalRequest);
        return result;
    }

    public static OpenApiEncryptedPayload encrypt(byte[] plainBytes, ApiEncryptionAlgorithm algorithm,
                                                  OpenApiCredentialMaterial material) {
        ApiEncryptionAlgorithm resolvedAlgorithm = algorithm == null ? ApiEncryptionAlgorithm.NONE : algorithm;
        OpenApiCredentialMaterial resolvedMaterial = material == null ? new OpenApiCredentialMaterial() : material;
        if (resolvedAlgorithm == ApiEncryptionAlgorithm.NONE) {
            return null;
        }
        try {
            OpenApiEncryptedPayload payload = new OpenApiEncryptedPayload().setAlgorithm(resolvedAlgorithm.name());
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.AES_GCM) {
                byte[] iv = randomBytes(GCM_IV_LENGTH);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(deriveKey(resolvedMaterial.getSecretKey(), AES_KEY_LENGTH), "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
                );
                payload.setIv(Base64.getEncoder().encodeToString(iv));
                payload.setEncrypted(Base64.getEncoder().encodeToString(cipher.doFinal(safeBytes(plainBytes))));
                return payload;
            }
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.AES_CBC) {
                byte[] iv = randomBytes(CBC_IV_LENGTH);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(deriveKey(resolvedMaterial.getSecretKey(), AES_KEY_LENGTH), "AES"),
                    new IvParameterSpec(iv)
                );
                payload.setIv(Base64.getEncoder().encodeToString(iv));
                payload.setEncrypted(Base64.getEncoder().encodeToString(cipher.doFinal(safeBytes(plainBytes))));
                return payload;
            }
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.SM4_CBC) {
                byte[] iv = randomBytes(CBC_IV_LENGTH);
                Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", BOUNCY_CASTLE_PROVIDER.getName());
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(deriveKey(resolvedMaterial.getSecretKey(), SM4_KEY_LENGTH), "SM4"),
                    new IvParameterSpec(iv)
                );
                payload.setIv(Base64.getEncoder().encodeToString(iv));
                payload.setEncrypted(Base64.getEncoder().encodeToString(cipher.doFinal(safeBytes(plainBytes))));
                return payload;
            }
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.RSA_OAEP_SHA256) {
                byte[] data = safeBytes(plainBytes);
                if (data.length > RSA_DIRECT_PAYLOAD_MAX_LENGTH) {
                    throw new OpenApiSecurityException("RSA_OAEP_SHA256 仅支持小报文或密钥封装场景");
                }
                Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    parseRsaPublicKey(firstNonBlank(resolvedMaterial.getServerPublicKey(), resolvedMaterial.getEncryptionPublicKey())),
                    new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA256,
                        PSource.PSpecified.DEFAULT
                    )
                );
                payload.setEncrypted(Base64.getEncoder().encodeToString(cipher.doFinal(data)));
                return payload;
            }
            throw new OpenApiSecurityException("不支持的加密算法: " + resolvedAlgorithm.name());
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("报文加密失败", ex);
        }
    }

    public static byte[] decrypt(OpenApiEncryptedPayload payload, ApiEncryptionAlgorithm algorithm,
                                 OpenApiCredentialMaterial material) {
        ApiEncryptionAlgorithm resolvedAlgorithm = algorithm == null ? ApiEncryptionAlgorithm.NONE : algorithm;
        OpenApiCredentialMaterial resolvedMaterial = material == null ? new OpenApiCredentialMaterial() : material;
        if (resolvedAlgorithm == ApiEncryptionAlgorithm.NONE) {
            return new byte[0];
        }
        if (payload == null || !hasText(payload.getEncrypted())) {
            throw new OpenApiSecurityException("加密负载不能为空");
        }
        try {
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.AES_GCM) {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(deriveKey(resolvedMaterial.getSecretKey(), AES_KEY_LENGTH), "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, Base64.getDecoder().decode(requireText(payload.getIv(), "初始化向量不能为空")))
                );
                return cipher.doFinal(Base64.getDecoder().decode(payload.getEncrypted()));
            }
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.AES_CBC) {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(deriveKey(resolvedMaterial.getSecretKey(), AES_KEY_LENGTH), "AES"),
                    new IvParameterSpec(Base64.getDecoder().decode(requireText(payload.getIv(), "初始化向量不能为空")))
                );
                return cipher.doFinal(Base64.getDecoder().decode(payload.getEncrypted()));
            }
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.SM4_CBC) {
                Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", BOUNCY_CASTLE_PROVIDER.getName());
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(deriveKey(resolvedMaterial.getSecretKey(), SM4_KEY_LENGTH), "SM4"),
                    new IvParameterSpec(Base64.getDecoder().decode(requireText(payload.getIv(), "初始化向量不能为空")))
                );
                return cipher.doFinal(Base64.getDecoder().decode(payload.getEncrypted()));
            }
            if (resolvedAlgorithm == ApiEncryptionAlgorithm.RSA_OAEP_SHA256) {
                Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    parseRsaPrivateKey(firstNonBlank(resolvedMaterial.getServerPrivateKey(), resolvedMaterial.getEncryptionPrivateKey())),
                    new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA256,
                        PSource.PSpecified.DEFAULT
                    )
                );
                return cipher.doFinal(Base64.getDecoder().decode(payload.getEncrypted()));
            }
            throw new OpenApiSecurityException("不支持的加密算法: " + resolvedAlgorithm.name());
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("报文解密失败", ex);
        }
    }

    public static Map<String, String> buildHeaders(OpenApiRequestContext requestContext, OpenApiCredentialMaterial material) {
        OpenApiSignResult signResult = sign(requestContext, material);
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put(OpenApiHeaders.ACCESS_KEY, signResult.getAccessKey());
        headers.put(OpenApiHeaders.SIGNATURE_ALGORITHM, signResult.getSignatureAlgorithm());
        headers.put(OpenApiHeaders.TIMESTAMP, signResult.getTimestamp());
        headers.put(OpenApiHeaders.NONCE, signResult.getNonce());
        headers.put(OpenApiHeaders.SIGNATURE, signResult.getSignature());
        if (hasText(signResult.getEncryptionAlgorithm())
            && !ApiEncryptionAlgorithm.NONE.name().equalsIgnoreCase(signResult.getEncryptionAlgorithm())) {
            headers.put(OpenApiHeaders.ENCRYPT_ALGORITHM, signResult.getEncryptionAlgorithm());
        }
        if (hasText(requestContext.getTenantId())) {
            headers.put(OpenApiHeaders.TENANT_ID, requestContext.getTenantId());
        }
        if (hasText(signResult.getRequestId())) {
            headers.put(OpenApiHeaders.REQUEST_ID, signResult.getRequestId());
        }
        return headers;
    }

    public static Instant parseTimestamp(String raw) {
        if (!hasText(raw)) {
            throw new OpenApiSecurityException("时间戳不能为空");
        }
        String normalized = raw.trim();
        if (normalized.matches("^\\d{10}$")) {
            return Instant.ofEpochSecond(Long.parseLong(normalized));
        }
        if (normalized.matches("^\\d{13}$")) {
            return Instant.ofEpochMilli(Long.parseLong(normalized));
        }
        try {
            return Instant.parse(normalized);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(normalized).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException inner) {
                throw new OpenApiSecurityException("无法解析时间戳: " + raw, inner);
            }
        }
    }

    public static boolean isTimestampValid(String raw, long windowSeconds) {
        return isTimestampValid(raw, windowSeconds, Instant.now());
    }

    public static boolean isTimestampValid(String raw, long windowSeconds, Instant now) {
        Instant parsed = parseTimestamp(raw);
        long delta = Math.abs(Duration.between(parsed, now).getSeconds());
        return delta <= Math.max(windowSeconds, 0L);
    }

    public static String buildNonceKey(String accessKey, String nonce) {
        return buildNonceKey(OpenApiConfigKeys.DEFAULT_NONCE_CACHE_PREFIX, accessKey, nonce);
    }

    public static String buildNonceKey(String prefix, String accessKey, String nonce) {
        return safe(prefix) + requireText(accessKey, "accessKey 不能为空") + ":" + requireText(nonce, "nonce 不能为空");
    }

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(safeBytes(data));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", value));
            }
            return builder.toString();
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("计算摘要失败", ex);
        }
    }

    private static OpenApiRequestContext normalizeRequestContext(OpenApiRequestContext requestContext,
                                                                 OpenApiCredentialMaterial material) {
        if (requestContext == null) {
            throw new OpenApiSecurityException("请求上下文不能为空");
        }
        OpenApiRequestContext normalized = new OpenApiRequestContext()
            .setMethod(requestContext.getMethod())
            .setPath(requestContext.getPath())
            .setQueryParameters(copyQueryParameters(requestContext.getQueryParameters()))
            .setBody(requestContext.getBody())
            .setTenantId(requestContext.getTenantId())
            .setRequestId(hasText(requestContext.getRequestId()) ? requestContext.getRequestId() : generateRequestId())
            .setTimestamp(hasText(requestContext.getTimestamp()) ? requestContext.getTimestamp() : Long.toString(Instant.now().getEpochSecond()))
            .setNonce(hasText(requestContext.getNonce()) ? requestContext.getNonce() : generateNonce())
            .setAccessKey(firstNonBlank(requestContext.getAccessKey(), material == null ? null : material.getAccessKey()))
            .setSignatureAlgorithm(
                requestContext.getSignatureAlgorithm() == null ? ApiSignatureAlgorithm.HMAC_SHA256 : requestContext.getSignatureAlgorithm()
            )
            .setEncryptionAlgorithm(
                requestContext.getEncryptionAlgorithm() == null ? ApiEncryptionAlgorithm.NONE : requestContext.getEncryptionAlgorithm()
            );
        requireText(normalized.getAccessKey(), "accessKey 不能为空");
        return normalized;
    }

    private static Map<String, List<String>> copyQueryParameters(Map<String, List<String>> queryParameters) {
        Map<String, List<String>> copied = new LinkedHashMap<String, List<String>>();
        if (queryParameters == null || queryParameters.isEmpty()) {
            return copied;
        }
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            List<String> values = entry.getValue() == null ? Collections.emptyList() : new ArrayList<String>(entry.getValue());
            copied.put(entry.getKey(), values);
        }
        return copied;
    }

    private static String buildSortedQuery(Map<String, List<String>> queryParameters) {
        if (queryParameters == null || queryParameters.isEmpty()) {
            return "";
        }
        TreeMap<String, List<String>> sorted = new TreeMap<String, List<String>>(queryParameters);
        List<String> pairs = new ArrayList<String>();
        for (Map.Entry<String, List<String>> entry : sorted.entrySet()) {
            List<String> values = entry.getValue() == null ? Collections.emptyList() : new ArrayList<String>(entry.getValue());
            Collections.sort(values);
            if (values.isEmpty()) {
                pairs.add(encode(entry.getKey()) + "=");
                continue;
            }
            for (String value : values) {
                pairs.add(encode(entry.getKey()) + "=" + encode(value));
            }
        }
        return joinWithAmpersand(pairs);
    }

    private static String joinWithLineBreak(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(values[i] == null ? "" : values[i]);
        }
        return builder.toString();
    }

    private static String joinWithAmpersand(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append('&');
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String normalizeMethod(String method) {
        return safe(method).trim().toUpperCase(Locale.ROOT);
    }

    private static String hmac(byte[] data, String algorithm, String secretKey) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(deriveKey(secretKey, AES_KEY_LENGTH), algorithm));
        return Base64.getEncoder().encodeToString(mac.doFinal(safeBytes(data)));
    }

    private static String signRsa(byte[] data, String privateKey) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(parseRsaPrivateKey(privateKey));
        signature.update(safeBytes(data));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private static boolean verifyRsa(byte[] data, String signatureValue, String publicKey) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(parseRsaPublicKey(publicKey));
        signature.update(safeBytes(data));
        return signature.verify(Base64.getDecoder().decode(requireText(signatureValue, "签名不能为空")));
    }

    private static String signSm2(byte[] data, String privateKey) {
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return Base64.getEncoder().encodeToString(sm2.sign(safeBytes(data)));
    }

    private static boolean verifySm2(byte[] data, String signatureValue, String publicKey) {
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.verify(safeBytes(data), Base64.getDecoder().decode(requireText(signatureValue, "签名不能为空")));
    }

    private static PublicKey parseRsaPublicKey(String rawKey) throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeKey(rawKey));
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private static PrivateKey parseRsaPrivateKey(String rawKey) throws GeneralSecurityException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodeKey(rawKey));
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private static byte[] decodeKey(String rawKey) {
        String normalized = requireText(rawKey, "密钥不能为空")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(normalized);
    }

    private static byte[] deriveKey(String secret, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(requireText(secret, "密钥不能为空").getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[length];
            System.arraycopy(digest, 0, result, 0, length);
            return result;
        } catch (GeneralSecurityException ex) {
            throw new OpenApiSecurityException("派生密钥失败", ex);
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static byte[] safeBytes(byte[] data) {
        return data == null ? new byte[0] : data;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String requireText(String value, String message) {
        if (!hasText(value)) {
            throw new OpenApiSecurityException(message);
        }
        return value.trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean equalsText(String left, String right) {
        return safe(left).equals(safe(right));
    }

    private static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String generateRequestId() {
        return Long.toHexString(System.currentTimeMillis()) + "-" + generateNonce().substring(0, 12);
    }
}
