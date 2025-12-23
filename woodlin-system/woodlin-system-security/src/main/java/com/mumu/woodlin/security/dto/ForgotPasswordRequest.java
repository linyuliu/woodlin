package com.mumu.woodlin.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 忘记密码重置请求
 *
 * @author mumu
 * @description 用于忘记密码场景的密码重置，需要验证码验证
 * @since 2025-01-15
 */
@Data
@Accessors(chain = true)
@Schema(description = "忘记密码重置请求")
public class ForgotPasswordRequest {
    
    /**
     * 用户名或手机号或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名或手机号或邮箱", example = "admin")
    private String username;
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "123456")
    private String code;
    
    /**
     * 验证码类型（sms-短信，email-邮件）
     */
    @NotBlank(message = "验证码类型不能为空")
    @Schema(description = "验证码类型", example = "sms", allowableValues = {"sms", "email"})
    private String codeType;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "NewPassw0rd")
    private String newPassword;
    
    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "NewPassw0rd")
    private String confirmPassword;
    
}
