package com.mumu.woodlin.security.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证码服务
 * 
 * @author mumu
 * @description 提供图形验证码的生成、验证等功能，基于Redis存储验证码信息
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    /**
     * 验证码 Redis Key 前缀
     */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    /**
     * 验证码过期时间（分钟）
     */
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码宽度
     */
    private static final int CAPTCHA_WIDTH = 130;
    
    /**
     * 验证码高度
     */
    private static final int CAPTCHA_HEIGHT = 48;
    
    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 4;
    
    /**
     * 生成验证码
     * 
     * @return 验证码信息（包含验证码ID和Base64图片）
     */
    public CaptchaInfo generateCaptcha() {
        // 生成验证码ID
        String captchaId = IdUtil.simpleUUID();
        
        // 创建验证码
        SpecCaptcha captcha = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, CAPTCHA_LENGTH);
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        
        // 获取验证码文本
        String captchaText = captcha.text().toLowerCase();
        
        // 存储到Redis，设置过期时间
        String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
        stringRedisTemplate.opsForValue().set(redisKey, captchaText, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 获取Base64图片
        String captchaImage = captcha.toBase64();
        
        log.debug("生成验证码: ID={}, Text={}", captchaId, captchaText);
        
        return new CaptchaInfo(captchaId, captchaImage);
    }
    
    /**
     * 验证验证码
     * 
     * @param captchaId 验证码ID
     * @param captchaCode 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        if (StrUtil.isBlank(captchaId) || StrUtil.isBlank(captchaCode)) {
            log.warn("验证码验证失败: 验证码ID或验证码为空");
            return false;
        }
        
        String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
        String storedCaptcha = stringRedisTemplate.opsForValue().get(redisKey);
        
        if (StrUtil.isBlank(storedCaptcha)) {
            log.warn("验证码验证失败: 验证码不存在或已过期, captchaId={}", captchaId);
            return false;
        }
        
        // 验证码只能使用一次，验证后立即删除
        stringRedisTemplate.delete(redisKey);
        
        boolean isValid = storedCaptcha.equals(captchaCode.toLowerCase());
        if (!isValid) {
            log.warn("验证码验证失败: 验证码错误, captchaId={}, expected={}, actual={}", 
                    captchaId, storedCaptcha, captchaCode.toLowerCase());
        } else {
            log.debug("验证码验证成功: captchaId={}", captchaId);
        }
        
        return isValid;
    }
    
    /**
     * 清除验证码
     * 
     * @param captchaId 验证码ID
     */
    public void clearCaptcha(String captchaId) {
        if (StrUtil.isNotBlank(captchaId)) {
            String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
            stringRedisTemplate.delete(redisKey);
            log.debug("清除验证码: captchaId={}", captchaId);
        }
    }
    
    /**
     * 验证码信息
     */
    public record CaptchaInfo(String captchaId, String captchaImage) {
        
        /**
         * 获取验证码ID
         * 
         * @return 验证码ID
         */
        public String getCaptchaId() {
            return captchaId;
        }
        
        /**
         * 获取验证码图片（Base64格式）
         * 
         * @return Base64验证码图片
         */
        public String getCaptchaImage() {
            return captchaImage;
        }
    }
    
}