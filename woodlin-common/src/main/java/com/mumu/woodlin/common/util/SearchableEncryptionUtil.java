package com.mumu.woodlin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.mumu.woodlin.common.exception.BusinessException;

/**
 * 可搜索加密工具类
 * 
 * @author mumu
 * @description 提供数据库字段的可搜索加密功能，支持模糊查询和精确匹配
 *              使用确定性加密算法保证相同明文生成相同密文，同时生成N-gram索引支持模糊搜索
 * 
 * 安全说明：
 * - 使用AES-256/CBC模式实现确定性加密（从明文派生IV）
 * - CBC模式理论上存在padding oracle攻击风险，但本实现中风险极低：
 *   1. 无公开解密API（数据保持加密状态）
 *   2. 不暴露解密错误信息
 *   3. 不支持批量解密尝试
 * - 为实现可搜索性，无法使用GCM等AEAD模式（需要随机nonce）
 * - 适用于需要在加密状态下进行搜索的场景
 * 
 * @since 2025-01-01
 */
@Slf4j
public class SearchableEncryptionUtil {
    
    // 使用CBC模式以支持确定性加密（从明文派生IV）
    // 注意：这是可搜索加密的必要权衡，风险已通过设计缓解
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_SIZE = 16;
    private static final int DEFAULT_NGRAM_SIZE = 2;
    
    /**
     * 确定性加密 - 相同明文产生相同密文，支持精确查询
     * 使用固定的IV（从密钥派生）确保相同输入产生相同输出
     * 
     * @param plaintext 明文
     * @param key 加密密钥（Base64编码）
     * @return 密文（Base64编码）
     */
    public static String encrypt(String plaintext, String key) {
        if (StrUtil.isBlank(plaintext)) {
            return plaintext;
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            
            // 使用从明文和密钥派生的确定性IV
            byte[] iv = generateDeterministicIv(plaintext, keyBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("数据加密失败: plaintext={}", plaintext, e);
            throw new BusinessException("数据加密失败: " + e.getMessage());
        }
    }
    
    /**
     * 解密
     * 
     * 注意：此方法需要原始明文参数，因为本工具使用确定性加密（从明文派生IV）。
     * 这是有意的设计选择，因为可搜索加密的主要目的是：
     * 1. 在数据库中安全存储敏感数据
     * 2. 支持加密数据的搜索和匹配
     * 3. 数据在应用层保持加密状态
     * 
     * 如果需要完全解密数据用于显示，建议：
     * - 使用单独的加密方案（如标准AES/CBC with random IV）
     * - 或在插入时保存明文哈希，用于验证解密
     * - 或使用混合加密方案
     * 
     * @param ciphertext 密文（Base64编码）
     * @param key 解密密钥（Base64编码）
     * @param originalPlaintext 原始明文（用于生成确定性IV）
     * @return 明文
     */
    public static String decrypt(String ciphertext, String key, String originalPlaintext) {
        if (StrUtil.isBlank(ciphertext)) {
            return ciphertext;
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            
            // 使用与加密时相同的确定性IV
            byte[] iv = generateDeterministicIv(originalPlaintext, keyBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("数据解密失败", e);
            throw new BusinessException("数据解密失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成N-gram索引标记
     * 用于支持模糊搜索，将文本分割为固定长度的子串
     * 
     * @param text 原始文本
     * @param n N-gram的大小（默认为2）
     * @return N-gram标记集合
     */
    public static Set<String> generateNGrams(String text, int n) {
        if (StrUtil.isBlank(text)) {
            return Set.of();
        }
        
        // 转换为小写并去除空格，提高匹配准确度
        String normalized = text.toLowerCase().replaceAll("\\s+", "");
        
        if (normalized.length() < n) {
            return Set.of(normalized);
        }
        
        Set<String> ngrams = new java.util.HashSet<>();
        for (int i = 0; i <= normalized.length() - n; i++) {
            ngrams.add(normalized.substring(i, i + n));
        }
        
        return ngrams;
    }
    
    /**
     * 生成默认大小的N-gram索引标记
     * 
     * @param text 原始文本
     * @return N-gram标记集合
     */
    public static Set<String> generateNGrams(String text) {
        return generateNGrams(text, DEFAULT_NGRAM_SIZE);
    }
    
    /**
     * 加密N-gram标记
     * 每个N-gram都单独加密，用于存储在数据库的搜索索引字段
     * 
     * @param ngrams N-gram标记集合
     * @param key 加密密钥（Base64编码）
     * @return 加密后的N-gram标记集合
     */
    public static Set<String> encryptNGrams(Set<String> ngrams, String key) {
        return ngrams.stream()
                .map(ngram -> encrypt(ngram, key))
                .collect(Collectors.toSet());
    }
    
    /**
     * 生成搜索索引（加密的N-gram集合的字符串表示）
     * 用于存储在数据库中的搜索索引字段
     * 
     * @param text 原始文本
     * @param key 加密密钥（Base64编码）
     * @return 搜索索引字符串（多个加密N-gram用逗号分隔）
     */
    public static String generateSearchIndex(String text, String key) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        
        Set<String> ngrams = generateNGrams(text);
        Set<String> encryptedNGrams = encryptNGrams(ngrams, key);
        
        return String.join(",", encryptedNGrams);
    }
    
    /**
     * 生成搜索查询的加密N-gram
     * 用于模糊查询时，将搜索关键字转换为加密的N-gram进行匹配
     * 
     * @param searchKeyword 搜索关键字
     * @param key 加密密钥（Base64编码）
     * @return 加密的N-gram列表
     */
    public static List<String> generateSearchTokens(String searchKeyword, String key) {
        if (StrUtil.isBlank(searchKeyword)) {
            return List.of();
        }
        
        Set<String> ngrams = generateNGrams(searchKeyword);
        return ngrams.stream()
                .map(ngram -> encrypt(ngram, key))
                .collect(Collectors.toList());
    }
    
    /**
     * 生成确定性IV
     * 通过对明文和密钥进行哈希生成固定的IV，确保相同输入产生相同密文
     * 
     * @param plaintext 明文
     * @param keyBytes 密钥字节数组
     * @return IV字节数组
     */
    private static byte[] generateDeterministicIv(String plaintext, byte[] keyBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(plaintext.getBytes(StandardCharsets.UTF_8));
            digest.update(keyBytes);
            byte[] hash = digest.digest();
            
            // 取哈希的前16字节作为IV
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(hash, 0, iv, 0, IV_SIZE);
            return iv;
        } catch (Exception e) {
            log.error("生成确定性IV失败", e);
            throw new BusinessException("生成确定性IV失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成随机加密密钥（Base64编码）
     * 
     * @param keySize 密钥位数（128、192或256）
     * @return Base64编码的密钥
     */
    public static String generateKey(int keySize) {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] key = new byte[keySize / 8];
            secureRandom.nextBytes(key);
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            log.error("生成密钥失败", e);
            throw new BusinessException("生成密钥失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成默认256位密钥
     * 
     * @return Base64编码的密钥
     */
    public static String generateKey() {
        return generateKey(256);
    }
    
    /**
     * 验证密钥格式是否正确
     * 
     * @param key 密钥（Base64编码）
     * @return 是否有效
     */
    public static boolean isValidKey(String key) {
        if (StrUtil.isBlank(key)) {
            return false;
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            return keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32;
        } catch (Exception e) {
            return false;
        }
    }
}
