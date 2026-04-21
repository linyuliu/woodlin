package com.mumu.woodlin.system;

import com.mumu.woodlin.system.dto.OpenApiEncryptedPayload;
import com.mumu.woodlin.system.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.system.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.system.util.OpenApiCryptoUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 开放 API 密码学工具测试。
 *
 * @author mumu
 * @since 2026-04-13
 */
class OpenApiCryptoUtilTest {

    @Test
    void shouldProtectAndRevealSecret() {
        String plain = "secret-value";
        String masterKey = "test-master-key";

        String protectedValue = OpenApiCryptoUtil.protect(plain, masterKey);

        Assertions.assertNotEquals(plain, protectedValue);
        Assertions.assertEquals(plain, OpenApiCryptoUtil.reveal(protectedValue, masterKey));
    }

    @Test
    void shouldEncryptAndDecryptAesGcmPayload() {
        byte[] plain = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "integration-secret";

        OpenApiEncryptedPayload payload = OpenApiCryptoUtil.encryptPayload(
            plain, ApiEncryptionAlgorithm.AES_GCM, secret, null
        );

        Assertions.assertNotNull(payload.getEncrypted());
        Assertions.assertArrayEquals(
            plain,
            OpenApiCryptoUtil.decryptPayload(payload, ApiEncryptionAlgorithm.AES_GCM, secret, null)
        );
    }

    @Test
    void shouldSignAndVerifyHmacPayload() {
        byte[] data = "open-api-signature".getBytes(StandardCharsets.UTF_8);
        String secret = "signature-secret";

        String signature = OpenApiCryptoUtil.sign(data, ApiSignatureAlgorithm.HMAC_SHA256, secret, null);

        Assertions.assertTrue(
            OpenApiCryptoUtil.verify(data, signature, ApiSignatureAlgorithm.HMAC_SHA256, secret, null)
        );
    }

    @Test
    void shouldSignAndVerifyRsaPayload() {
        byte[] data = "rsa-signature".getBytes(StandardCharsets.UTF_8);
        Map<String, String> keyPair = OpenApiCryptoUtil.generateRsaKeyPair();

        String signature = OpenApiCryptoUtil.sign(
            data, ApiSignatureAlgorithm.RSA_SHA256, null, keyPair.get("privateKey")
        );

        Assertions.assertTrue(
            OpenApiCryptoUtil.verify(data, signature, ApiSignatureAlgorithm.RSA_SHA256, null, keyPair.get("publicKey"))
        );
    }

    @Test
    void shouldSignAndVerifySm2Payload() {
        byte[] data = "sm2-signature".getBytes(StandardCharsets.UTF_8);
        Map<String, String> keyPair = OpenApiCryptoUtil.generateSm2KeyPair();

        String signature = OpenApiCryptoUtil.sign(
            data, ApiSignatureAlgorithm.SM2_SM3, null, keyPair.get("privateKey")
        );

        Assertions.assertTrue(
            OpenApiCryptoUtil.verify(data, signature, ApiSignatureAlgorithm.SM2_SM3, null, keyPair.get("publicKey"))
        );
    }
}
