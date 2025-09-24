package com.mumu.woodlin.security.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录响应DTO
 * 
 * @author mumu
 * @description 登录响应信息，包含令牌和密码策略信息
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "登录响应")
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String token;
    
    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";
    
    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌过期时间（秒）")
    private Long expiresIn;
    
    /**
     * 是否需要修改密码
     */
    @Schema(description = "是否需要修改密码")
    private Boolean requirePasswordChange = false;
    
    /**
     * 密码是否即将过期
     */
    @Schema(description = "密码是否即将过期")
    private Boolean passwordExpiringSoon = false;
    
    /**
     * 密码过期剩余天数
     */
    @Schema(description = "密码过期剩余天数")
    private Long daysUntilPasswordExpiration;
    
    /**
     * 提示消息
     */
    @Schema(description = "提示消息")
    private String message;
    
}