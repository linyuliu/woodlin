package com.mumu.woodlin.security.service;

import com.mumu.woodlin.security.config.PasswordPolicyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * 密码策略服务
 * 
 * @author mumu
 * @description 提供密码策略验证功能，包括密码强度、过期检查等
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    
    private final PasswordPolicyProperties passwordPolicyProperties;
    
    /**
     * 密码验证结果
     */
    public static class PasswordValidationResult {
        private boolean valid;
        private String message;
        private boolean requireChange;
        private boolean expiringSoon;
        private long daysUntilExpiration;
        
        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public PasswordValidationResult(boolean valid, String message, boolean requireChange, boolean expiringSoon, long daysUntilExpiration) {
            this.valid = valid;
            this.message = message;
            this.requireChange = requireChange;
            this.expiringSoon = expiringSoon;
            this.daysUntilExpiration = daysUntilExpiration;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public boolean isRequireChange() { return requireChange; }
        public boolean isExpiringSoon() { return expiringSoon; }
        public long getDaysUntilExpiration() { return daysUntilExpiration; }
    }
    
    /**
     * 验证密码是否符合策略要求
     * 
     * @param password 密码
     * @return 验证结果
     */
    public PasswordValidationResult validatePassword(String password) {
        if (!passwordPolicyProperties.getEnabled()) {
            return new PasswordValidationResult(true, "密码策略已禁用");
        }
        
        if (StrUtil.isBlank(password)) {
            return new PasswordValidationResult(false, "密码不能为空");
        }
        
        // 长度检查
        if (password.length() < passwordPolicyProperties.getMinLength()) {
            return new PasswordValidationResult(false, 
                String.format("密码长度不能少于%d位", passwordPolicyProperties.getMinLength()));
        }
        
        if (password.length() > passwordPolicyProperties.getMaxLength()) {
            return new PasswordValidationResult(false, 
                String.format("密码长度不能超过%d位", passwordPolicyProperties.getMaxLength()));
        }
        
        // 强密码策略检查
        if (passwordPolicyProperties.getStrongPasswordRequired()) {
            if (passwordPolicyProperties.getRequireDigits() && !Pattern.matches(".*\\d.*", password)) {
                return new PasswordValidationResult(false, "密码必须包含数字");
            }
            
            if (passwordPolicyProperties.getRequireLowercase() && !Pattern.matches(".*[a-z].*", password)) {
                return new PasswordValidationResult(false, "密码必须包含小写字母");
            }
            
            if (passwordPolicyProperties.getRequireUppercase() && !Pattern.matches(".*[A-Z].*", password)) {
                return new PasswordValidationResult(false, "密码必须包含大写字母");
            }
            
            if (passwordPolicyProperties.getRequireSpecialChars() && 
                !Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>?].*", password)) {
                return new PasswordValidationResult(false, "密码必须包含特殊字符");
            }
        }
        
        return new PasswordValidationResult(true, "密码验证通过");
    }
    
    /**
     * 用户密码信息接口
     */
    public interface UserPasswordInfo {
        Boolean getIsFirstLogin();
        LocalDateTime getPwdChangeTime();
        LocalDateTime getCreateTime();
        Integer getPwdExpireDays();
        LocalDateTime getLockTime();
        Integer getPwdErrorCount();
    }
    
    /**
     * 检查用户登录时的密码策略
     * 
     * @param user 用户信息
     * @return 验证结果
     */
    public PasswordValidationResult validateUserLogin(UserPasswordInfo user) {
        if (!passwordPolicyProperties.getEnabled()) {
            return new PasswordValidationResult(true, "密码策略已禁用");
        }
        
        boolean requireChange = false;
        boolean expiringSoon = false;
        long daysUntilExpiration = 0;
        String message = "登录成功";
        
        // 检查首次登录
        if (passwordPolicyProperties.getRequireChangeOnFirstLogin() && 
            Boolean.TRUE.equals(user.getIsFirstLogin())) {
            requireChange = true;
            message = "首次登录需要修改密码";
        }
        
        // 检查密码过期
        if (isPasswordExpired(user)) {
            requireChange = true;
            message = "密码已过期，需要修改密码";
        } else if (isPasswordExpiringSoon(user)) {
            expiringSoon = true;
            daysUntilExpiration = getDaysUntilPasswordExpiration(user);
            message = String.format("密码将在%d天后过期", daysUntilExpiration);
        }
        
        // 检查账号锁定
        if (isAccountLocked(user)) {
            return new PasswordValidationResult(false, "账号已被锁定，请稍后再试");
        }
        
        return new PasswordValidationResult(true, message, requireChange, expiringSoon, daysUntilExpiration);
    }
    
    /**
     * 检查密码是否已过期
     * 
     * @param user 用户信息
     * @return 是否过期
     */
    public boolean isPasswordExpired(UserPasswordInfo user) {
        if (!passwordPolicyProperties.getEnabled()) {
            return false;
        }
        
        int expireDays = getPasswordExpireDays(user);
        if (expireDays <= 0) {
            return false; // 永不过期
        }
        
        LocalDateTime pwdChangeTime = user.getPwdChangeTime();
        if (pwdChangeTime == null) {
            // 如果没有密码修改时间，使用创建时间
            pwdChangeTime = user.getCreateTime();
        }
        
        if (pwdChangeTime == null) {
            return false; // 无法确定时间
        }
        
        LocalDateTime expiryTime = pwdChangeTime.plusDays(expireDays);
        return LocalDateTime.now().isAfter(expiryTime);
    }
    
    /**
     * 检查密码是否即将过期
     * 
     * @param user 用户信息
     * @return 是否即将过期
     */
    public boolean isPasswordExpiringSoon(UserPasswordInfo user) {
        if (!passwordPolicyProperties.getEnabled()) {
            return false;
        }
        
        int expireDays = getPasswordExpireDays(user);
        if (expireDays <= 0) {
            return false; // 永不过期
        }
        
        long daysUntilExpiration = getDaysUntilPasswordExpiration(user);
        return daysUntilExpiration > 0 && daysUntilExpiration <= passwordPolicyProperties.getWarningDays();
    }
    
    /**
     * 获取密码过期剩余天数
     * 
     * @param user 用户信息
     * @return 剩余天数，负数表示已过期
     */
    public long getDaysUntilPasswordExpiration(UserPasswordInfo user) {
        int expireDays = getPasswordExpireDays(user);
        if (expireDays <= 0) {
            return Long.MAX_VALUE; // 永不过期
        }
        
        LocalDateTime pwdChangeTime = user.getPwdChangeTime();
        if (pwdChangeTime == null) {
            pwdChangeTime = user.getCreateTime();
        }
        
        if (pwdChangeTime == null) {
            return Long.MAX_VALUE;
        }
        
        LocalDateTime expiryTime = pwdChangeTime.plusDays(expireDays);
        return ChronoUnit.DAYS.between(LocalDateTime.now(), expiryTime);
    }
    
    /**
     * 检查账号是否被锁定
     * 
     * @param user 用户信息
     * @return 是否被锁定
     */
    public boolean isAccountLocked(UserPasswordInfo user) {
        if (user.getLockTime() == null) {
            return false;
        }
        
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(passwordPolicyProperties.getLockDurationMinutes());
        return LocalDateTime.now().isBefore(unlockTime);
    }
    
    /**
     * 获取用户的密码过期天数
     * 优先使用用户个人设置，其次使用系统配置
     * 
     * @param user 用户信息
     * @return 过期天数
     */
    private int getPasswordExpireDays(UserPasswordInfo user) {
        if (user.getPwdExpireDays() != null) {
            return user.getPwdExpireDays();
        }
        return passwordPolicyProperties.getExpireDays();
    }
    
    /**
     * 处理密码错误
     * 
     * @param user 用户信息
     * @return 是否需要锁定账号
     */
    public boolean handlePasswordError(UserPasswordInfo user) {
        if (!passwordPolicyProperties.getEnabled()) {
            return false;
        }
        
        int errorCount = user.getPwdErrorCount() == null ? 0 : user.getPwdErrorCount();
        errorCount++;
        
        if (errorCount >= passwordPolicyProperties.getMaxErrorCount()) {
            // 锁定账号
            return true;
        }
        
        return false;
    }
    
}