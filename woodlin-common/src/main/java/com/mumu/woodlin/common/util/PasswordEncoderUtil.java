package com.mumu.woodlin.common.util;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;

/**
 * 密码编码工具类
 * 
 * @author mumu
 * @description 提供密码加密和验证功能，支持BCrypt和noop(无加密)两种模式
 * @since 2025-01-15
 */
@Slf4j
public class PasswordEncoderUtil {
    
    /**
     * BCrypt前缀
     */
    private static final String BCRYPT_PREFIX = "{bcrypt}";
    
    /**
     * Noop前缀（用于测试，不加密）
     */
    private static final String NOOP_PREFIX = "{noop}";
    
    /**
     * 加密密码（使用BCrypt）
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码，带{bcrypt}前缀
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        
        // 如果已经加密过，直接返回
        if (isEncoded(rawPassword)) {
            return rawPassword;
        }
        
        return BCRYPT_PREFIX + BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
    
    /**
     * 加密密码（支持选择编码器）
     * 
     * @param rawPassword 原始密码
     * @param useNoop 是否使用noop（不加密，仅用于测试）
     * @return 加密后的密码
     */
    public static String encode(String rawPassword, boolean useNoop) {
        if (rawPassword == null) {
            return null;
        }
        
        // 如果已经加密过，直接返回
        if (isEncoded(rawPassword)) {
            return rawPassword;
        }
        
        if (useNoop) {
            return NOOP_PREFIX + rawPassword;
        } else {
            return encode(rawPassword);
        }
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        // 如果是noop编码
        if (encodedPassword.startsWith(NOOP_PREFIX)) {
            String storedPassword = encodedPassword.substring(NOOP_PREFIX.length());
            return rawPassword.equals(storedPassword);
        }
        
        // 如果是BCrypt编码
        if (encodedPassword.startsWith(BCRYPT_PREFIX)) {
            String hash = encodedPassword.substring(BCRYPT_PREFIX.length());
            return BCrypt.checkpw(rawPassword, hash);
        }
        
        // 兼容旧版本没有前缀的BCrypt密码
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        }
        
        // 如果都不是，说明密码格式不对
        log.warn("未知的密码编码格式: {}", encodedPassword);
        return false;
    }
    
    /**
     * 检查密码是否已经加密
     * 
     * @param password 密码
     * @return 是否已加密
     */
    public static boolean isEncoded(String password) {
        if (password == null) {
            return false;
        }
        
        return password.startsWith(BCRYPT_PREFIX) 
            || password.startsWith(NOOP_PREFIX)
            || password.startsWith("$2a$") 
            || password.startsWith("$2b$") 
            || password.startsWith("$2y$");
    }
    
    /**
     * 获取密码的编码器类型
     * 
     * @param encodedPassword 加密后的密码
     * @return 编码器类型（bcrypt/noop/unknown）
     */
    public static String getEncoderType(String encodedPassword) {
        if (encodedPassword == null) {
            return "unknown";
        }
        
        if (encodedPassword.startsWith(BCRYPT_PREFIX) 
            || encodedPassword.startsWith("$2a$") 
            || encodedPassword.startsWith("$2b$") 
            || encodedPassword.startsWith("$2y$")) {
            return "bcrypt";
        }
        
        if (encodedPassword.startsWith(NOOP_PREFIX)) {
            return "noop";
        }
        
        return "unknown";
    }
    
}
