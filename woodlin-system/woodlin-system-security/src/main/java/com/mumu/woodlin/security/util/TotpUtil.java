package com.mumu.woodlin.security.util;

import java.nio.ByteBuffer;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.codec.Base32;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TOTP 工具类
 *
 * @author mumu
 * @description 提供基于RFC6238的TOTP生成与校验能力，默认30s时间步长、6位数字
 * @since 2025-12-10
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TotpUtil {

    private static final int DEFAULT_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int DEFAULT_WINDOW = 1;

    /**
     * 校验TOTP验证码
     *
     * @param base32Secret Base32编码的密钥
     * @param code         待验证的验证码
     * @return 是否验证通过
     */
    public static boolean verifyCode(String base32Secret, String code) {
        return verifyCode(base32Secret, code, DEFAULT_WINDOW, Instant.now().getEpochSecond());
    }

    /**
     * 校验TOTP验证码（可指定时间与窗口，便于测试）
     *
     * @param base32Secret Base32编码的密钥
     * @param code         待验证的验证码
     * @param window       容忍的时间窗口步数
     * @param epochSeconds 指定时间戳（秒）
     * @return 是否验证通过
     */
    public static boolean verifyCode(String base32Secret, String code, int window, long epochSeconds) {
        if (StrUtil.isBlank(base32Secret) || StrUtil.isBlank(code)) {
            return false;
        }
        if (code.length() != DEFAULT_DIGITS || !code.chars().allMatch(Character::isDigit)) {
            return false;
        }

        try {
            long timeIndex = epochSeconds / TIME_STEP_SECONDS;
            for (int i = -window; i <= window; i++) {
                String expected = generateCode(base32Secret, timeIndex + i);
                if (code.equals(expected)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.warn("TOTP 验证失败: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 生成指定时间片的TOTP验证码
     *
     * @param base32Secret Base32编码密钥
     * @param timeIndex    时间片索引
     * @return 6位验证码
     */
    public static String generateCode(String base32Secret, long timeIndex) {
        byte[] key = new Base32().decode(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeIndex).array();

        byte[] hash = hmacSha1(key, data);
        int offset = hash[hash.length - 1] & 0x0f;
        int binary = ((hash[offset] & 0x7f) << 24)
            | ((hash[offset + 1] & 0xff) << 16)
            | ((hash[offset + 2] & 0xff) << 8)
            | (hash[offset + 3] & 0xff);

        int otp = binary % (int) Math.pow(10, DEFAULT_DIGITS);
        return String.format("%0" + DEFAULT_DIGITS + "d", otp);
    }

    private static byte[] hmacSha1(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            return mac.doFinal(data);
        } catch (Exception ex) {
            throw new IllegalStateException("生成HMAC-SHA1失败", ex);
        }
    }
}
