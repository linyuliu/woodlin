package com.mumu.woodlin.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 登录响应DTO
 *
 * @author mumu
 * @description 登录响应信息。前端约定字段：token / expire / user / roles / permissions。
 *              同时保留密码策略辅助字段，供前端展示密码即将过期等提示。
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * 访问令牌（Bearer token）
     */
    @Schema(description = "访问令牌")
    private String token;

    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌过期时间（秒）")
    private Long expire;

    /**
     * 当前登录用户基本信息
     */
    @Schema(description = "用户信息")
    private UserInfo user;

    /**
     * 角色编码列表
     */
    @Schema(description = "角色编码列表")
    private List<String> roles;

    /**
     * 权限标识列表（按钮级别，供 v-permission 使用）
     */
    @Schema(description = "权限标识列表")
    private List<String> permissions;

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
     * 提示消息（密码策略相关）
     */
    @Schema(description = "提示消息")
    private String message;

    /**
     * 用户基本信息（内嵌 DTO，仅暴露前端必要字段）
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "用户基本信息")
    public static class UserInfo {

        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像URL")
        private String avatar;

        @Schema(description = "部门ID")
        private Long deptId;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "租户ID")
        private String tenantId;
    }
}