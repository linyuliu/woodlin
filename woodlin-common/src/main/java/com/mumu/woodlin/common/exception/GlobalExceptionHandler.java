package com.mumu.woodlin.common.exception;

import cn.hutool.core.util.StrUtil;
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
import com.mumu.woodlin.common.response.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

import static com.mumu.woodlin.common.constant.CommonConstant.*;

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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数验证异常（RequestBody）
     * 
     * @param e 方法参数验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数验证异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(BAD_REQUEST_CODE, StrUtil.isBlank(message) ? "参数验证失败" : message);
    }
    
    /**
     * 处理参数绑定异常
     * 
     * @param e 绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(BAD_REQUEST_CODE, StrUtil.isBlank(message) ? "参数绑定失败" : message);
    }
    
    /**
     * 处理约束违反异常（RequestParam）
     * 
     * @param e 约束违反异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束违反异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(BAD_REQUEST_CODE, StrUtil.isBlank(message) ? "参数验证失败" : message);
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
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = String.format("缺少必需参数: %s", e.getParameterName());
        log.warn("缺少请求参数异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(BAD_REQUEST_CODE, message);
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
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数类型错误: %s", e.getName());
        log.warn("参数类型不匹配异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(BAD_REQUEST_CODE, message);
    }
    
    /**
     * 处理HTTP请求方法不支持异常
     * 
     * @param e HTTP请求方法不支持异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("不支持的请求方法: %s", e.getMethod());
        log.warn("HTTP请求方法不支持异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(405, message);
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
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String message = String.format("请求路径不存在: %s", e.getRequestURL());
        log.warn("未找到处理器异常: {} - 请求路径: {}", message, request.getRequestURI());
        return Result.fail(NOT_FOUND_CODE, message);
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
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问拒绝异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return Result.fail(FORBIDDEN_CODE, "访问被拒绝");
    }
    
    /**
     * 处理运行时异常
     * 
     * @param e 运行时异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.fail("系统运行异常，请联系管理员");
    }
    
    /**
     * 处理其他异常
     * 
     * @param e 异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.fail("系统异常，请联系管理员");
    }
    
}