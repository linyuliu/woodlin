package com.mumu.woodlin.security.service;

import com.mumu.woodlin.security.config.PasswordPolicyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码策略服务测试
 * 
 * @author mumu
 * @description 测试密码策略服务的各种功能
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceTest {
    
    private PasswordPolicyService passwordPolicyService;
    private PasswordPolicyProperties properties;
    
    @BeforeEach
    void setUp() {
        properties = new PasswordPolicyProperties();
        passwordPolicyService = new PasswordPolicyService(properties);
    }
    
    @Test
    void testValidatePassword_WhenPolicyDisabled_ShouldReturnValid() {
        // 给定
        properties.setEnabled(false);
        
        // 当
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword("123");
        
        // 则
        assertTrue(result.isValid());
        assertEquals("密码策略已禁用", result.getMessage());
    }
    
    @Test
    void testValidatePassword_WhenPasswordEmpty_ShouldReturnInvalid() {
        // 给定
        properties.setEnabled(true);
        
        // 当
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword("");
        
        // 则
        assertFalse(result.isValid());
        assertEquals("密码不能为空", result.getMessage());
    }
    
    @Test
    void testValidatePassword_WhenPasswordTooShort_ShouldReturnInvalid() {
        // 给定
        properties.setEnabled(true);
        properties.setMinLength(6);
        
        // 当
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword("12345");
        
        // 则
        assertFalse(result.isValid());
        assertEquals("密码长度不能少于6位", result.getMessage());
    }
    
    @Test
    void testValidatePassword_WhenPasswordTooLong_ShouldReturnInvalid() {
        // 给定
        properties.setEnabled(true);
        properties.setMaxLength(10);
        
        // 当
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword("12345678901");
        
        // 则
        assertFalse(result.isValid());
        assertEquals("密码长度不能超过10位", result.getMessage());
    }
    
    @Test
    void testValidatePassword_WhenStrongPasswordRequired_ShouldValidateComplexity() {
        // 给定
        properties.setEnabled(true);
        properties.setStrongPasswordRequired(true);
        properties.setRequireDigits(true);
        properties.setRequireLowercase(true);
        properties.setRequireUppercase(true);
        properties.setRequireSpecialChars(true);
        properties.setMinLength(8);
        
        // 当 - 测试缺少数字
        PasswordPolicyService.PasswordValidationResult result1 = passwordPolicyService.validatePassword("Abcdefg@");
        assertFalse(result1.isValid());
        assertEquals("密码必须包含数字", result1.getMessage());
        
        // 当 - 测试缺少小写字母
        PasswordPolicyService.PasswordValidationResult result2 = passwordPolicyService.validatePassword("ABCDEFG1@");
        assertFalse(result2.isValid());
        assertEquals("密码必须包含小写字母", result2.getMessage());
        
        // 当 - 测试缺少大写字母
        PasswordPolicyService.PasswordValidationResult result3 = passwordPolicyService.validatePassword("abcdefg1@");
        assertFalse(result3.isValid());
        assertEquals("密码必须包含大写字母", result3.getMessage());
        
        // 当 - 测试缺少特殊字符
        PasswordPolicyService.PasswordValidationResult result4 = passwordPolicyService.validatePassword("Abcdefg1");
        assertFalse(result4.isValid());
        assertEquals("密码必须包含特殊字符", result4.getMessage());
        
        // 当 - 测试符合所有要求
        PasswordPolicyService.PasswordValidationResult result5 = passwordPolicyService.validatePassword("Abcdefg1@");
        assertTrue(result5.isValid());
        assertEquals("密码验证通过", result5.getMessage());
    }
    
    @Test
    void testIsPasswordExpired_WhenExpireDaysIsZero_ShouldNotExpire() {
        // 给定
        properties.setEnabled(true);
        properties.setExpireDays(0);
        
        TestUserPasswordInfo user = new TestUserPasswordInfo();
        user.setPwdChangeTime(LocalDateTime.now().minusDays(365));
        user.setCreateTime(LocalDateTime.now().minusDays(365));
        
        // 当
        boolean result = passwordPolicyService.isPasswordExpired(user);
        
        // 则
        assertFalse(result);
    }
    
    @Test
    void testIsPasswordExpired_WhenPasswordExpired_ShouldReturnTrue() {
        // 给定
        properties.setEnabled(true);
        properties.setExpireDays(30);
        
        TestUserPasswordInfo user = new TestUserPasswordInfo();
        user.setPwdChangeTime(LocalDateTime.now().minusDays(31));
        user.setCreateTime(LocalDateTime.now().minusDays(31));
        
        // 当
        boolean result = passwordPolicyService.isPasswordExpired(user);
        
        // 则
        assertTrue(result);
    }
    
    @Test
    void testIsPasswordExpired_WhenPasswordNotExpired_ShouldReturnFalse() {
        // 给定
        properties.setEnabled(true);
        properties.setExpireDays(30);
        
        TestUserPasswordInfo user = new TestUserPasswordInfo();
        user.setPwdChangeTime(LocalDateTime.now().minusDays(15));
        user.setCreateTime(LocalDateTime.now().minusDays(15));
        
        // 当
        boolean result = passwordPolicyService.isPasswordExpired(user);
        
        // 则
        assertFalse(result);
    }
    
    @Test
    void testIsAccountLocked_WhenLockTimeInFuture_ShouldReturnTrue() {
        // 给定
        properties.setEnabled(true);
        properties.setLockDurationMinutes(30);
        
        TestUserPasswordInfo user = new TestUserPasswordInfo();
        user.setLockTime(LocalDateTime.now().minusMinutes(15));
        
        // 当
        boolean result = passwordPolicyService.isAccountLocked(user);
        
        // 则
        assertTrue(result);
    }
    
    @Test
    void testIsAccountLocked_WhenLockTimeExpired_ShouldReturnFalse() {
        // 给定
        properties.setEnabled(true);
        properties.setLockDurationMinutes(30);
        
        TestUserPasswordInfo user = new TestUserPasswordInfo();
        user.setLockTime(LocalDateTime.now().minusMinutes(31));
        
        // 当
        boolean result = passwordPolicyService.isAccountLocked(user);
        
        // 则
        assertFalse(result);
    }
    
    @Test
    void testValidateUserLogin_WhenFirstLogin_ShouldRequireChange() {
        // 给定
        properties.setEnabled(true);
        properties.setRequireChangeOnFirstLogin(true);
        
        TestUserPasswordInfo user = new TestUserPasswordInfo();
        user.setIsFirstLogin(true);
        
        // 当
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validateUserLogin(user);
        
        // 则
        assertTrue(result.isValid());
        assertTrue(result.isRequireChange());
        assertEquals("首次登录需要修改密码", result.getMessage());
    }
    
    /**
     * 测试用户密码信息实现
     */
    private static class TestUserPasswordInfo implements PasswordPolicyService.UserPasswordInfo {
        private Boolean isFirstLogin;
        private LocalDateTime pwdChangeTime;
        private LocalDateTime createTime;
        private Integer pwdExpireDays;
        private LocalDateTime lockTime;
        private Integer pwdErrorCount;
        
        @Override
        public Boolean getIsFirstLogin() {
            return isFirstLogin;
        }
        
        @Override
        public LocalDateTime getPwdChangeTime() {
            return pwdChangeTime;
        }
        
        @Override
        public LocalDateTime getCreateTime() {
            return createTime;
        }
        
        @Override
        public Integer getPwdExpireDays() {
            return pwdExpireDays;
        }
        
        @Override
        public LocalDateTime getLockTime() {
            return lockTime;
        }
        
        @Override
        public Integer getPwdErrorCount() {
            return pwdErrorCount;
        }
        
        // Setters for test setup
        public void setIsFirstLogin(Boolean isFirstLogin) {
            this.isFirstLogin = isFirstLogin;
        }
        
        public void setPwdChangeTime(LocalDateTime pwdChangeTime) {
            this.pwdChangeTime = pwdChangeTime;
        }
        
        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
        
        public void setPwdExpireDays(Integer pwdExpireDays) {
            this.pwdExpireDays = pwdExpireDays;
        }
        
        public void setLockTime(LocalDateTime lockTime) {
            this.lockTime = lockTime;
        }
        
        public void setPwdErrorCount(Integer pwdErrorCount) {
            this.pwdErrorCount = pwdErrorCount;
        }
    }
}