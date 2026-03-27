package com.mumu.woodlin.common.util;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.config.ApiEncryptionProperties;
import com.mumu.woodlin.common.exception.BusinessException;

/**
 * API 加密工具类
 * 
 * @author mumu
 * @description 提供多种加密算法的统一接口，支持 AES、RSA、SM4 等加密算法
 * @since 2025-01-01
 */
@Slf4j
public class ApiEncryptionUtil {
    
    /**
     * 加密数据
     * 
     * @param data 原始数据
     * @param properties 加密配置
     * @return 加密后的数据（Base64编码）
     */
    public static String encrypt(String data, ApiEncryptionProperties properties) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        try {
            switch (properties.getAlgorithm()) {
                case AES:
                    return encryptByAes(data, properties);
                case RSA:
                    return encryptByRsa(data, properties);
                case SM4:
                    return encryptBySm4(data, properties);
                default:
                    throw new BusinessException("不支持的加密算法: " + properties.getAlgorithm());
            }
        } catch (Exception e) {
            log.error("数据加密失败", e);
            throw new BusinessException("数据加密失败: " + e.getMessage());
        }
    }
    
    /**
     * 解密数据
     * 
     * @param encryptedData 加密的数据（Base64编码）
     * @param properties 加密配置
     * @return 解密后的原始数据
     */
    public static String decrypt(String encryptedData, ApiEncryptionProperties properties) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        try {
            switch (properties.getAlgorithm()) {
                case AES:
                    return decryptByAes(encryptedData, properties);
                case RSA:
                    return decryptByRsa(encryptedData, properties);
                case SM4:
                    return decryptBySm4(encryptedData, properties);
                default:
                    throw new BusinessException("不支持的加密算法: " + properties.getAlgorithm());
            }
        } catch (Exception e) {
            log.error("数据解密失败", e);
            throw new BusinessException("数据解密失败: " + e.getMessage());
        }
    }
    
    /**
     * AES 加密
     * 
     * @param data 原始数据
     * @param properties 加密配置
     * @return 加密后的数据（Base64编码）
     */
    private static String encryptByAes(String data, ApiEncryptionProperties properties) throws Exception {
        if (properties.getAesKey() == null || properties.getAesKey().isEmpty()) {
            throw new BusinessException("AES加密密钥未配置");
        }
        
        byte[] keyBytes = Base64.decode(properties.getAesKey());
        byte[] ivBytes = properties.getAesIv() != null ? Base64.decode(properties.getAesIv()) : null;
        
        AES aes;
        if (ivBytes != null) {
            aes = new AES(properties.getAesMode(), properties.getAesPadding(), keyBytes, ivBytes);
        } else {
            aes = new AES(properties.getAesMode(), properties.getAesPadding(), keyBytes);
        }
        
        byte[] encrypted = aes.encrypt(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encode(encrypted);
    }
    
    /**
     * AES 解密
     * 
     * @param encryptedData 加密的数据（Base64编码）
     * @param properties 加密配置
     * @return 解密后的原始数据
     */
    private static String decryptByAes(String encryptedData, ApiEncryptionProperties properties) throws Exception {
        if (properties.getAesKey() == null || properties.getAesKey().isEmpty()) {
            throw new BusinessException("AES解密密钥未配置");
        }
        
        byte[] keyBytes = Base64.decode(properties.getAesKey());
        byte[] ivBytes = properties.getAesIv() != null ? Base64.decode(properties.getAesIv()) : null;
        
        AES aes;
        if (ivBytes != null) {
            aes = new AES(properties.getAesMode(), properties.getAesPadding(), keyBytes, ivBytes);
        } else {
            aes = new AES(properties.getAesMode(), properties.getAesPadding(), keyBytes);
        }
        
        byte[] decrypted = aes.decrypt(Base64.decode(encryptedData));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * RSA 加密（使用公钥）
     * 
     * @param data 原始数据
     * @param properties 加密配置
     * @return 加密后的数据（Base64编码）
     */
    private static String encryptByRsa(String data, ApiEncryptionProperties properties) throws Exception {
        if (properties.getRsaPublicKey() == null || properties.getRsaPublicKey().isEmpty()) {
            throw new BusinessException("RSA公钥未配置");
        }
        
        RSA rsa = new RSA(null, properties.getRsaPublicKey());
        byte[] encrypted = rsa.encrypt(data.getBytes(StandardCharsets.UTF_8), KeyType.PublicKey);
        return Base64.encode(encrypted);
    }
    
    /**
     * RSA 解密（使用私钥）
     * 
     * @param encryptedData 加密的数据（Base64编码）
     * @param properties 加密配置
     * @return 解密后的原始数据
     */
    private static String decryptByRsa(String encryptedData, ApiEncryptionProperties properties) throws Exception {
        if (properties.getRsaPrivateKey() == null || properties.getRsaPrivateKey().isEmpty()) {
            throw new BusinessException("RSA私钥未配置");
        }
        
        RSA rsa = new RSA(properties.getRsaPrivateKey(), null);
        byte[] decrypted = rsa.decrypt(Base64.decode(encryptedData), KeyType.PrivateKey);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * SM4 加密（国密算法）
     * 
     * @param data 原始数据
     * @param properties 加密配置
     * @return 加密后的数据（Base64编码）
     */
    private static String encryptBySm4(String data, ApiEncryptionProperties properties) throws Exception {
        if (properties.getSm4Key() == null || properties.getSm4Key().isEmpty()) {
            throw new BusinessException("SM4加密密钥未配置");
        }
        
        byte[] keyBytes = Base64.decode(properties.getSm4Key());
        String algorithm = "SM4/" + properties.getSm4Mode() + "/PKCS5Padding";
        
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
        Cipher cipher = Cipher.getInstance(algorithm, "BC");
        
        if ("CBC".equalsIgnoreCase(properties.getSm4Mode())) {
            if (properties.getSm4Iv() == null || properties.getSm4Iv().isEmpty()) {
                throw new BusinessException("SM4 CBC模式需要配置IV");
            }
            byte[] ivBytes = Base64.decode(properties.getSm4Iv());
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }
        
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encode(encrypted);
    }
    
    /**
     * SM4 解密（国密算法）
     * 
     * @param encryptedData 加密的数据（Base64编码）
     * @param properties 加密配置
     * @return 解密后的原始数据
     */
    private static String decryptBySm4(String encryptedData, ApiEncryptionProperties properties) throws Exception {
        if (properties.getSm4Key() == null || properties.getSm4Key().isEmpty()) {
            throw new BusinessException("SM4解密密钥未配置");
        }
        
        byte[] keyBytes = Base64.decode(properties.getSm4Key());
        String algorithm = "SM4/" + properties.getSm4Mode() + "/PKCS5Padding";
        
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
        Cipher cipher = Cipher.getInstance(algorithm, "BC");
        
        if ("CBC".equalsIgnoreCase(properties.getSm4Mode())) {
            if (properties.getSm4Iv() == null || properties.getSm4Iv().isEmpty()) {
                throw new BusinessException("SM4 CBC模式需要配置IV");
            }
            byte[] ivBytes = Base64.decode(properties.getSm4Iv());
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        }
        
        byte[] decrypted = cipher.doFinal(Base64.decode(encryptedData));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * 生成 AES 密钥（Base64编码）
     * 
     * @param keySize 密钥长度（128、192 或 256）
     * @return Base64编码的密钥
     */
    public static String generateAesKey(int keySize) {
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue(), keySize).getEncoded();
        return Base64.encode(key);
    }
    
    /**
     * 生成 RSA 密钥对
     * 
     * @param keySize 密钥长度（1024、2048 或 4096）
     * @return 密钥对数组 [公钥, 私钥]（Base64编码）
     */
    public static String[] generateRsaKeyPair(int keySize) {
        RSA rsa = SecureUtil.rsa();
        String publicKey = rsa.getPublicKeyBase64();
        String privateKey = rsa.getPrivateKeyBase64();
        return new String[]{publicKey, privateKey};
    }
}
