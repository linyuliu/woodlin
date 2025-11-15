package com.mumu.woodlin.security.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.security.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类
 *
 * @author mumu
 * @description 提供短信验证码发送和验证功能的实现
 *              使用Redis存储验证码，支持过期时间设置
 *              当前为模拟实现，实际使用时需要接入第三方短信服务商
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    /**
     * 短信验证码Redis键前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";
    
    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;
    
    @Override
    public boolean sendSmsCode(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            log.warn("发送短信验证码失败: 手机号为空");
            return false;
        }
        
        // 生成6位随机数字验证码
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        
        // 存储到Redis，设置5分钟过期
        String key = SMS_CODE_PREFIX + mobile;
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // TODO: 集成第三方短信服务商发送短信
        // 例如：阿里云、腾讯云、华为云等短信服务
        // smsClient.sendSms(mobile, code);
        
        log.info("短信验证码已生成: mobile={}, code={} (实际生产环境不应打印验证码)", mobile, code);
        log.info("模拟短信发送: 向手机号 {} 发送验证码 {}", mobile, code);
        
        return true;
    }
    
    @Override
    public boolean verifySmsCode(String mobile, String code) {
        if (StrUtil.isBlank(mobile) || StrUtil.isBlank(code)) {
            log.warn("验证短信验证码失败: 手机号或验证码为空");
            return false;
        }
        
        String key = SMS_CODE_PREFIX + mobile;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("验证短信验证码失败: 验证码不存在或已过期, mobile={}", mobile);
            return false;
        }
        
        boolean valid = code.equals(storedCode);
        if (!valid) {
            log.warn("验证短信验证码失败: 验证码错误, mobile={}", mobile);
        } else {
            log.info("验证短信验证码成功: mobile={}", mobile);
        }
        
        return valid;
    }
    
    @Override
    public void clearSmsCode(String mobile) {
        if (StrUtil.isNotBlank(mobile)) {
            String key = SMS_CODE_PREFIX + mobile;
            stringRedisTemplate.delete(key);
            log.debug("清除短信验证码: mobile={}", mobile);
        }
    }
}
