package com.mumu.woodlin.common.openapi;

import com.mumu.woodlin.common.openapi.enums.ApiEncryptionAlgorithm;
import com.mumu.woodlin.common.openapi.enums.ApiSignatureAlgorithm;
import com.mumu.woodlin.common.openapi.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

class OpenApiSecurityKitTest {

    @Test
    void shouldBuildCanonicalRequest() {
        OpenApiRequestContext context = new OpenApiRequestContext()
            .setMethod("post")
            .setPath("/openapi/demo")
            .setQueryParameters(Collections.singletonMap("b", java.util.Arrays.asList("3", "2")))
            .addQueryParameter("a", "1")
            .setBody("{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8))
            .setTimestamp("1713662400")
            .setNonce("nonce-demo")
            .setTenantId("tenant-demo")
            .setAccessKey("ak_demo");

        Assertions.assertEquals(
            "POST\n" +
                "/openapi/demo\n" +
                "a=1&b=2&b=3\n" +
                "93a23971a914e5eacbf0a8d25154cda309c3c1c72fbb9914d47c60f3cb681588\n" +
                "1713662400\n" +
                "nonce-demo\n" +
                "tenant-demo\n" +
                "ak_demo",
            OpenApiSecurityKit.buildCanonicalRequest(context)
        );
    }

    @Test
    void shouldBuildDeterministicHeadersAndHmacSignature() {
        OpenApiCredentialMaterial material = new OpenApiCredentialMaterial()
            .setAccessKey("ak_demo")
            .setSecretKey("integration-secret");
        OpenApiRequestContext context = new OpenApiRequestContext()
            .setMethod("POST")
            .setPath("/openapi/demo")
            .addQueryParameter("b", "3")
            .addQueryParameter("b", "2")
            .addQueryParameter("a", "1")
            .setBody("{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8))
            .setTimestamp("1713662400")
            .setNonce("nonce-demo")
            .setTenantId("tenant-demo")
            .setRequestId("req-demo")
            .setAccessKey("ak_demo")
            .setSignatureAlgorithm(ApiSignatureAlgorithm.HMAC_SHA256)
            .setEncryptionAlgorithm(ApiEncryptionAlgorithm.AES_GCM);

        OpenApiSignResult signResult = OpenApiSecurityKit.sign(context, material);
        OpenApiVerifyResult verifyResult = OpenApiSecurityKit.verify(context, signResult.getSignature(), material);
        Map<String, String> headers = OpenApiSecurityKit.buildHeaders(context, material);

        Assertions.assertEquals("KB1yLR1/sf8qqwCxikBNQqc3LDDYtP72whcbWPDNAH0=", signResult.getSignature());
        Assertions.assertTrue(verifyResult.isVerified());
        Assertions.assertEquals("ak_demo", headers.get("X-Access-Key"));
        Assertions.assertEquals("HMAC_SHA256", headers.get("X-Signature-Algorithm"));
        Assertions.assertEquals("1713662400", headers.get("X-Timestamp"));
        Assertions.assertEquals("nonce-demo", headers.get("X-Nonce"));
        Assertions.assertEquals("req-demo", headers.get("X-Request-Id"));
        Assertions.assertEquals("AES_GCM", headers.get("X-Encrypt-Algorithm"));
    }

    @Test
    void shouldSignAndVerifyRsa() {
        OpenApiAsymmetricKeyPair keyPair = OpenApiSecurityKit.generateRsaKeyPair();
        OpenApiCredentialMaterial material = new OpenApiCredentialMaterial()
            .setSignaturePrivateKey(keyPair.getPrivateKey())
            .setSignaturePublicKey(keyPair.getPublicKey());

        String signature = OpenApiSecurityKit.sign(
            "rsa-signature".getBytes(StandardCharsets.UTF_8),
            ApiSignatureAlgorithm.RSA_SHA256,
            material
        );

        Assertions.assertTrue(
            OpenApiSecurityKit.verify(
                "rsa-signature".getBytes(StandardCharsets.UTF_8),
                signature,
                ApiSignatureAlgorithm.RSA_SHA256,
                material
            ).isVerified()
        );
    }

    @Test
    void shouldSignAndVerifySm2() {
        OpenApiAsymmetricKeyPair keyPair = OpenApiSecurityKit.generateSm2KeyPair();
        OpenApiCredentialMaterial material = new OpenApiCredentialMaterial()
            .setSignaturePrivateKey(keyPair.getPrivateKey())
            .setSignaturePublicKey(keyPair.getPublicKey());

        String signature = OpenApiSecurityKit.sign(
            "sm2-signature".getBytes(StandardCharsets.UTF_8),
            ApiSignatureAlgorithm.SM2_SM3,
            material
        );

        Assertions.assertTrue(
            OpenApiSecurityKit.verify(
                "sm2-signature".getBytes(StandardCharsets.UTF_8),
                signature,
                ApiSignatureAlgorithm.SM2_SM3,
                material
            ).isVerified()
        );
    }

    @Test
    void shouldEncryptAndDecryptSymmetricPayloads() {
        OpenApiCredentialMaterial material = new OpenApiCredentialMaterial().setSecretKey("payload-secret");
        byte[] plain = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);

        OpenApiEncryptedPayload aesGcmPayload = OpenApiSecurityKit.encrypt(plain, ApiEncryptionAlgorithm.AES_GCM, material);
        OpenApiEncryptedPayload aesCbcPayload = OpenApiSecurityKit.encrypt(plain, ApiEncryptionAlgorithm.AES_CBC, material);
        OpenApiEncryptedPayload sm4Payload = OpenApiSecurityKit.encrypt(plain, ApiEncryptionAlgorithm.SM4_CBC, material);

        Assertions.assertArrayEquals(plain, OpenApiSecurityKit.decrypt(aesGcmPayload, ApiEncryptionAlgorithm.AES_GCM, material));
        Assertions.assertArrayEquals(plain, OpenApiSecurityKit.decrypt(aesCbcPayload, ApiEncryptionAlgorithm.AES_CBC, material));
        Assertions.assertArrayEquals(plain, OpenApiSecurityKit.decrypt(sm4Payload, ApiEncryptionAlgorithm.SM4_CBC, material));
    }

    @Test
    void shouldEncryptAndDecryptRsaPayload() {
        OpenApiAsymmetricKeyPair keyPair = OpenApiSecurityKit.generateRsaKeyPair();
        OpenApiCredentialMaterial material = new OpenApiCredentialMaterial()
            .setServerPublicKey(keyPair.getPublicKey())
            .setServerPrivateKey(keyPair.getPrivateKey());
        byte[] plain = "small-payload".getBytes(StandardCharsets.UTF_8);

        OpenApiEncryptedPayload payload = OpenApiSecurityKit.encrypt(plain, ApiEncryptionAlgorithm.RSA_OAEP_SHA256, material);

        Assertions.assertArrayEquals(plain, OpenApiSecurityKit.decrypt(payload, ApiEncryptionAlgorithm.RSA_OAEP_SHA256, material));
    }

    @Test
    void shouldProtectAndRevealSecret() {
        String protectedValue = OpenApiSecurityKit.protect("secret-value", "master-key");

        Assertions.assertNotEquals("secret-value", protectedValue);
        Assertions.assertEquals("secret-value", OpenApiSecurityKit.reveal(protectedValue, "master-key"));
    }

    @Test
    void shouldParseTimestampAndValidateWindow() {
        Assertions.assertEquals(Instant.ofEpochSecond(1713662400L), OpenApiSecurityKit.parseTimestamp("1713662400"));
        Assertions.assertEquals(Instant.ofEpochMilli(1713662400123L), OpenApiSecurityKit.parseTimestamp("1713662400123"));
        Assertions.assertTrue(
            OpenApiSecurityKit.isTimestampValid("1713662400", 10, Instant.ofEpochSecond(1713662405L))
        );
        Assertions.assertFalse(
            OpenApiSecurityKit.isTimestampValid("1713662400", 2, Instant.ofEpochSecond(1713662410L))
        );
        Assertions.assertEquals(
            "openapi:nonce:ak_demo:nonce-demo",
            OpenApiSecurityKit.buildNonceKey("ak_demo", "nonce-demo")
        );
    }
}
