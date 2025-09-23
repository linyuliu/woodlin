package com.mumu.woodlin.common.exception;

import cn.hutool.core.util.StrUtil;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author mumu
 * @description 统一处理系统中的各种异常，提供标准化的错误响应格式
 * @since 2025-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * 
     * @param e 业务异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return R.fail(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常
     * 
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.VALIDATION_FAILED, StrUtil.isBlank(message) ? "参数校验失败" : message);
    }
    
    /**
     * 处理绑定异常
     * 
     * @param e 绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("绑定异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, StrUtil.isBlank(message) ? "参数绑定失败" : message);
    }
    
    /**
     * 处理约束违反异常
     * 
     * @param e 约束违反异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.VALIDATION_FAILED, StrUtil.isBlank(message) ? "约束校验失败" : message);
    }
    
    /**
     * 处理缺少请求参数异常
     * 
     * @param e 缺少请求参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        log.warn("缺少请求参数异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理请求方法不支持异常
     * 
     * @param e 请求方法不支持异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("请求方法 %s 不支持", e.getMethod());
        log.warn("请求方法不支持异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.METHOD_NOT_ALLOWED, message);
    }
    
    /**
     * 处理未找到处理器异常
     * 
     * @param e 未找到处理器异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String message = String.format("请求路径不存在: %s", e.getRequestURL());
        log.warn("未找到处理器异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.NOT_FOUND, message);
    }
    
    /**
     * 处理访问拒绝异常
     * 
     * @param e 访问拒绝异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问拒绝异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return R.fail(ResultCode.FORBIDDEN, "访问被拒绝");
    }
    
    /**
     * 处理方法参数类型不匹配异常
     * 
     * @param e 方法参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数类型不匹配: %s", e.getName());
        log.warn("参数类型不匹配异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理所有未捕获的异常
     * 
     * @param e 异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "系统内部错误");
    }
}