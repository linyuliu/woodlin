package com.mumu.woodlin.common.util;

import java.util.regex.Pattern;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 验证工具类
 * 
 * @author mumu
 * @description 提供常用的数据验证功能，包括邮箱、手机号、身份证等格式验证
 * @since 2025-01-01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidateUtil {
    
    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * 手机号正则表达式（中国大陆）
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );
    
    /**
     * 身份证号正则表达式（18位）
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"
    );
    
    /**
     * 用户名正则表达式（4-20位，字母、数字、下划线）
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{4,20}$"
    );
    
    /**
     * 密码正则表达式（8-20位，包含字母和数字）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$"
    );
    
    /**
     * IP地址正则表达式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );
    
    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱地址
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 验证手机号格式
     * 
     * @param mobile 手机号
     * @return 是否有效
     */
    public static boolean isValidMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile).matches();
    }
    
    /**
     * 验证身份证号格式
     * 
     * @param idCard 身份证号
     * @return 是否有效
     */
    public static boolean isValidIdCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard).matches();
    }
    
    /**
     * 验证用户名格式
     * 
     * @param username 用户名
     * @return 是否有效
     */
    public static boolean isValidUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * 验证密码格式
     * 
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 验证端口号
     * 
     * @param port 端口号
     * @return 是否有效
     */
    public static boolean isValidPort(Integer port) {
        return port != null && port >= 1 && port <= 65535;
    }
    
    /**
     * 验证URL格式
     * 
     * @param url URL地址
     * @return 是否有效
     */
    public static boolean isValidUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }
        return url.matches("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    }
    
}