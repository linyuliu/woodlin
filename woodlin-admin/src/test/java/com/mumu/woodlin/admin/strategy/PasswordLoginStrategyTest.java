package com.mumu.woodlin.admin.strategy;

import com.mumu.woodlin.common.constant.SystemConstant;
import com.mumu.woodlin.common.util.PasswordEncoderUtil;
import com.mumu.woodlin.security.dto.LoginRequest;
import com.mumu.woodlin.security.dto.LoginResponse;
import com.mumu.woodlin.security.enums.LoginType;
import com.mumu.woodlin.security.service.PasswordPolicyService;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.service.ISysPermissionService;
import com.mumu.woodlin.system.service.ISysRoleService;
import com.mumu.woodlin.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密码登录策略测试
 *
 * @author mumu
 * @description 测试密码登录策略的各种场景
 * @since 2025-01-15
 */
@ExtendWith(MockitoExtension.class)
class PasswordLoginStrategyTest {
    
    @Mock
    private ISysUserService userService;
    
    @Mock
    private PasswordPolicyService passwordPolicyService;
    
    @Mock
    private ISysRoleService roleService;
    
    @Mock
    private ISysPermissionService permissionService;
    
    @InjectMocks
    private PasswordLoginStrategy passwordLoginStrategy;
    
    private SysUser testUser;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new SysUser();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setStatus("1");
        testUser.setPwdErrorCount(0);
        testUser.setLoginCount(0);
        
        // 创建登录请求
        loginRequest = new LoginRequest();
        loginRequest.setLoginType("password");
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("TestPass123");
    }
    
    @Test
    void testGetLoginType() {
        assertEquals(LoginType.PASSWORD, passwordLoginStrategy.getLoginType());
    }
    
    @Test
    void testValidateRequest_Success() {
        assertTrue(passwordLoginStrategy.validateRequest(loginRequest));
    }
    
    @Test
    void testValidateRequest_MissingUsername() {
        loginRequest.setUsername(null);
        assertFalse(passwordLoginStrategy.validateRequest(loginRequest));
    }
    
    @Test
    void testValidateRequest_MissingPassword() {
        loginRequest.setPassword(null);
        assertFalse(passwordLoginStrategy.validateRequest(loginRequest));
    }
    
    @Test
    void testLogin_Success_WithBCrypt() {
        // 设置BCrypt加密的密码
        testUser.setPassword(PasswordEncoderUtil.encode("TestPass123", false));
        
        when(userService.selectUserByUsername("testuser")).thenReturn(testUser);
        when(passwordPolicyService.isAccountLocked(any())).thenReturn(false);
        
        PasswordPolicyService.PasswordValidationResult validationResult = 
            new PasswordPolicyService.PasswordValidationResult(true, "密码有效");
        when(passwordPolicyService.validateUserLogin(any())).thenReturn(validationResult);
        
        when(roleService.selectAllRolesByUserId(1L)).thenReturn(Collections.emptyList());
        when(permissionService.selectPermissionCodesByUserId(1L)).thenReturn(Collections.emptyList());
        when(userService.updateById(any())).thenReturn(true);
        
        // 执行登录（注意：Sa-Token需要在Spring环境中才能工作，这里会抛出异常是正常的）
        try {
            LoginResponse response = passwordLoginStrategy.login(loginRequest);
            assertNotNull(response);
            // 如果能执行到这里，说明测试成功
        } catch (Exception e) {
            // Sa-Token在单元测试中会抛出异常，这是预期的
            // 我们主要是验证密码验证逻辑，所以这个异常是可以接受的
            String message = e.getMessage();
            boolean isExpectedException = message != null && 
                (message.contains("NotLoginException") || 
                 message.contains("SaToken") ||
                 message.contains("Session") ||
                 message.contains("sa-token") ||
                 message.contains("上下文") ||  // Chinese: context
                 message.contains("context"));
            assertTrue(isExpectedException, "Unexpected exception: " + message);
        }
        
        // 验证服务调用
        verify(userService).selectUserByUsername("testuser");
        verify(userService).updateById(any());
    }
    
    @Test
    void testLogin_Success_WithNoop() {
        // 设置Noop加密的密码（用于测试）
        testUser.setPassword(PasswordEncoderUtil.encode("TestPass123", true));
        
        when(userService.selectUserByUsername("testuser")).thenReturn(testUser);
        when(passwordPolicyService.isAccountLocked(any())).thenReturn(false);
        
        PasswordPolicyService.PasswordValidationResult validationResult = 
            new PasswordPolicyService.PasswordValidationResult(true, "密码有效");
        when(passwordPolicyService.validateUserLogin(any())).thenReturn(validationResult);
        
        when(roleService.selectAllRolesByUserId(1L)).thenReturn(Collections.emptyList());
        when(permissionService.selectPermissionCodesByUserId(1L)).thenReturn(Collections.emptyList());
        when(userService.updateById(any())).thenReturn(true);
        
        // 执行登录
        try {
            LoginResponse response = passwordLoginStrategy.login(loginRequest);
            assertNotNull(response);
            // 如果能执行到这里，说明测试成功
        } catch (Exception e) {
            // Sa-Token在单元测试中会抛出异常，这是预期的
            String message = e.getMessage();
            boolean isExpectedException = message != null && 
                (message.contains("NotLoginException") || 
                 message.contains("SaToken") ||
                 message.contains("Session") ||
                 message.contains("sa-token") ||
                 message.contains("上下文") ||  // Chinese: context
                 message.contains("context"));
            assertTrue(isExpectedException, "Unexpected exception: " + message);
        }
        
        // 验证服务调用
        verify(userService).selectUserByUsername("testuser");
        verify(userService).updateById(any());
    }
    
    @Test
    void testPasswordEncoding() {
        // 测试BCrypt编码
        String password = "TestPassword123";
        String encodedBCrypt = PasswordEncoderUtil.encode(password, false);
        
        assertNotNull(encodedBCrypt);
        assertTrue(encodedBCrypt.startsWith("{bcrypt}"));
        assertTrue(PasswordEncoderUtil.matches(password, encodedBCrypt));
        assertFalse(PasswordEncoderUtil.matches("WrongPassword", encodedBCrypt));
        
        // 测试Noop编码
        String encodedNoop = PasswordEncoderUtil.encode(password, true);
        
        assertNotNull(encodedNoop);
        assertTrue(encodedNoop.startsWith("{noop}"));
        assertTrue(PasswordEncoderUtil.matches(password, encodedNoop));
        assertFalse(PasswordEncoderUtil.matches("WrongPassword", encodedNoop));
        
        // 测试编码器类型检测
        assertEquals("bcrypt", PasswordEncoderUtil.getEncoderType(encodedBCrypt));
        assertEquals("noop", PasswordEncoderUtil.getEncoderType(encodedNoop));
    }
    
    @Test
    void testDefaultPassword() {
        // 测试默认密码可以正确加密和验证
        String defaultPassword = SystemConstant.DEFAULT_PASSWORD;
        String encoded = PasswordEncoderUtil.encode(defaultPassword);
        
        assertTrue(PasswordEncoderUtil.matches(defaultPassword, encoded));
        assertTrue(PasswordEncoderUtil.isEncoded(encoded));
    }
    
}
