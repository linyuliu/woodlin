package com.mumu.woodlin.security.handler;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.mumu.woodlin.common.response.Result;

import jakarta.servlet.http.HttpServletRequest;

import static com.mumu.woodlin.common.constant.CommonConstant.*;

/**
 * Sa-Token 异常处理器
 * 
 * @author mumu
 * @description 统一处理Sa-Token框架抛出的认证和授权异常
 * @since 2025-01-01
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {
    
    /**
     * 拦截：未登录异常
     * 
     * @param e 未登录异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handlerNotLoginException(NotLoginException e, HttpServletRequest request) {
        // 判断场景值，定制化异常信息
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供登录凭证";
            case NotLoginException.INVALID_TOKEN -> "登录凭证无效";
            case NotLoginException.TOKEN_TIMEOUT -> "登录凭证已过期";
            case NotLoginException.BE_REPLACED -> "您的账号已在其他地方登录";
            case NotLoginException.KICK_OUT -> "您的账号已被强制下线";
            default -> "登录状态异常";
        };
        
        log.warn("用户未登录: {} - 请求路径: {} - 异常类型: {}", message, request.getRequestURI(), e.getType());
        return Result.fail(UNAUTHORIZED_CODE, message);
    }
    
    /**
     * 拦截：缺少权限异常
     * 
     * @param e 缺少权限异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handlerNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String message = "权限不足，无法访问";
        log.warn("权限不足: 缺少权限 [{}] - 请求路径: {}", e.getPermission(), request.getRequestURI());
        return Result.fail(FORBIDDEN_CODE, message);
    }
    
    /**
     * 拦截：缺少角色异常
     * 
     * @param e 缺少角色异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handlerNotRoleException(NotRoleException e, HttpServletRequest request) {
        String message = "权限不足，无法访问";
        log.warn("角色不足: 缺少角色 [{}] - 请求路径: {}", e.getRole(), request.getRequestURI());
        return Result.fail(FORBIDDEN_CODE, message);
    }
    
    /**
     * 拦截：服务封禁异常
     * 
     * @param e 服务封禁异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(DisableServiceException.class)
    public Result<Void> handlerDisableServiceException(DisableServiceException e, HttpServletRequest request) {
        String message = "当前账号已被封禁";
        log.warn("账号被封禁: 服务类型 [{}] - 请求路径: {}", e.getService(), request.getRequestURI());
        return Result.fail(FORBIDDEN_CODE, message);
    }
    
}