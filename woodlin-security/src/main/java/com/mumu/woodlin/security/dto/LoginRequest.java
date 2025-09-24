package com.mumu.woodlin.security.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO
 * 
 * @author mumu
 * @description 登录请求参数
 * @since 2025-01-01
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码长度不能超过100个字符")
    @Schema(description = "密码", example = "123456")
    private String password;
    
    /**
     * 验证码
     */
    @Schema(description = "验证码")
    private String captcha;
    
    /**
     * 验证码UUID
     */
    @Schema(description = "验证码UUID")
    private String uuid;
    
}