package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * 
 * @author mumu
 * @description 统一的响应状态码定义
 * @since 2025-01-01
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_FAILED(422, "参数校验失败"),
    
    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误 1xxx
    BUSINESS_ERROR(1000, "业务处理失败"),
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_ALREADY_EXISTS(1003, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1004, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(1005, "手机号已存在"),
    PASSWORD_ERROR(1006, "密码错误"),
    USER_DISABLED(1007, "用户已被禁用"),
    USER_LOCKED(1008, "用户已被锁定"),
    ACCOUNT_LOCKED(1009, "账号已被锁定"),
    PASSWORD_EXPIRED(1010, "密码已过期"),
    PASSWORD_POLICY_VIOLATION(1011, "密码不符合策略要求"),
    OLD_PASSWORD_ERROR(1012, "旧密码错误"),
    PASSWORD_MISMATCH(1013, "密码和确认密码不一致"),
    CAPTCHA_ERROR(1014, "验证码错误"),
    CAPTCHA_EXPIRED(1015, "验证码已过期"),
    
    // 权限错误 2xxx
    PERMISSION_DENIED(2001, "权限不足"),
    ROLE_NOT_FOUND(2002, "角色不存在"),
    PERMISSION_NOT_FOUND(2003, "权限不存在"),
    
    // 文件错误 3xxx
    FILE_UPLOAD_ERROR(3001, "文件上传失败"),
    FILE_NOT_FOUND(3002, "文件不存在"),
    FILE_SIZE_EXCEEDED(3003, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED(3004, "文件类型不支持"),
    
    // 数据错误 4xxx
    DATA_NOT_FOUND(4001, "数据不存在"),
    DATA_ALREADY_EXISTS(4002, "数据已存在"),
    DATA_INTEGRITY_ERROR(4003, "数据完整性错误"),
    
    // 系统错误 5xxx
    SYSTEM_CONFIG_ERROR(5001, "系统配置错误"),
    EXTERNAL_SERVICE_ERROR(5002, "外部服务调用失败"),
    DATABASE_ERROR(5003, "数据库操作失败");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态消息
     */
    private final String message;
}