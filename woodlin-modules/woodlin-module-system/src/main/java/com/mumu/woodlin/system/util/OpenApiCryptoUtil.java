package com.mumu.woodlin.system.util;

import com.mumu.woodlin.common.openapi.OpenApiSecurityKit;
import com.mumu.woodlin.common.openapi.model.OpenApiAsymmetricKeyPair;
import com.mumu.woodlin.common.openapi.model.OpenApiCredentialMaterial;
import com.mumu.woodlin.system.dto.OpenApiEncryptedPayload;
import com.mumu.woodlin.system.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.system.enums.ApiSignatureAlgorithm;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开放 API 密钥、签名、加解密兼容工具。
 *
 * <p>协议能力已经统一收敛到 {@link OpenApiSecurityKit}，本类只保留系统模块旧入口，
 * 方便当前业务层平滑过渡。</p>
 *
 * @author mumu
 * @since 2026-04-21
 */
public final class OpenApiCryptoUtil {

    private OpenApiCryptoUtil() {
    }

    public static String generateAccessKey() {
        return OpenApiSecurityKit.generateAccessKey();
    }

    public static String generateSecretKey() {
        return OpenApiSecurityKit.generateSecretKey();
    }

    public static Map<String, String> generateRsaKeyPair() {
        return toMap(OpenApiSecurityKit.generateRsaKeyPair());
    }

    public static Map<String, String> generateSm2KeyPair() {
        return toMap(OpenApiSecurityKit.generateSm2KeyPair());
    }

    public static String fingerprint(String value) {
        return OpenApiSecurityKit.fingerprint(value);
    }

    public static String protect(String plainText, String masterKey) {
        return OpenApiSecurityKit.protect(plainText, masterKey);
    }

    public static String reveal(String protectedText, String masterKey) {
        return OpenApiSecurityKit.reveal(protectedText, masterKey);
    }

    public static String sign(byte[] data, ApiSignatureAlgorithm algorithm, String secretKey, String privateKey) {
        return OpenApiSecurityKit.sign(data, algorithm.toCore(), new OpenApiCredentialMaterial()
            .setSecretKey(secretKey)
            .setSignaturePrivateKey(privateKey));
    }

    public static boolean verify(byte[] data, String signature, ApiSignatureAlgorithm algorithm,
                                 String secretKey, String publicKey) {
        return OpenApiSecurityKit.verify(data, signature, algorithm.toCore(), new OpenApiCredentialMaterial()
            .setSecretKey(secretKey)
            .setSignaturePublicKey(publicKey)).isVerified();
    }

    public static OpenApiEncryptedPayload encryptPayload(byte[] plainBytes, ApiEncryptionAlgorithm algorithm,
                                                         String secretKey, String publicKey) {
        com.mumu.woodlin.common.openapi.model.OpenApiEncryptedPayload payload = OpenApiSecurityKit.encrypt(
            plainBytes,
            algorithm.toCore(),
            new OpenApiCredentialMaterial().setSecretKey(secretKey).setServerPublicKey(publicKey).setEncryptionPublicKey(publicKey)
        );
        if (payload == null) {
            return null;
        }
        return new OpenApiEncryptedPayload()
            .setAlgorithm(payload.getAlgorithm())
            .setEncrypted(payload.getEncrypted())
            .setIv(payload.getIv());
    }

    public static byte[] decryptPayload(OpenApiEncryptedPayload payload, ApiEncryptionAlgorithm algorithm,
                                        String secretKey, String privateKey) {
        return OpenApiSecurityKit.decrypt(
            new com.mumu.woodlin.common.openapi.model.OpenApiEncryptedPayload()
                .setAlgorithm(payload.getAlgorithm())
                .setEncrypted(payload.getEncrypted())
                .setIv(payload.getIv()),
            algorithm.toCore(),
            new OpenApiCredentialMaterial()
                .setSecretKey(secretKey)
                .setServerPrivateKey(privateKey)
                .setEncryptionPrivateKey(privateKey)
        );
    }

    public static String sha256Hex(byte[] data) {
        return OpenApiSecurityKit.sha256Hex(data);
    }

    public static LocalDateTime parseTimestamp(String raw) {
        return LocalDateTime.ofInstant(OpenApiSecurityKit.parseTimestamp(raw), ZoneOffset.UTC);
    }

    private static Map<String, String> toMap(OpenApiAsymmetricKeyPair keyPair) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("publicKey", keyPair.getPublicKey());
        result.put("privateKey", keyPair.getPrivateKey());
        return result;
    }
}
