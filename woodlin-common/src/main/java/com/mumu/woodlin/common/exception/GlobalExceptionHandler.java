package com.mumu.woodlin.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.response.R;

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
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return R.fail(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.VALIDATION_FAILED, StrUtil.isBlank(message) ? "参数校验失败" : message);
    }
    
    /**
     * 处理参数绑定异常（@RequestParam）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, StrUtil.isBlank(message) ? "参数绑定失败" : message);
    }
    
    /**
     * 处理约束违反异常（@Validated）
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
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        log.warn("缺少请求参数异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("HTTP消息不可读异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, "请求体格式错误或不可读");
    }
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("请求方法 %s 不支持，支持的方法: %s", 
            e.getMethod(), String.join(", ", e.getSupportedMethods()));
        log.warn("请求方法不支持异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.METHOD_NOT_ALLOWED, message);
    }
    
    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        String message = String.format("不支持的媒体类型: %s", e.getContentType());
        log.warn("媒体类型不支持异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理文件上传大小超出限制异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        String message = String.format("上传文件大小超出限制: %s", e.getMaxUploadSize());
        log.warn("文件上传大小超出限制: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.FILE_SIZE_EXCEEDED, message);
    }
    
    /**
     * 处理未找到处理器异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String message = String.format("请求路径不存在: %s %s", e.getHttpMethod(), e.getRequestURL());
        log.warn("未找到处理器异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.NOT_FOUND, message);
    }
    
    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问拒绝异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return R.fail(ResultCode.FORBIDDEN, "访问被拒绝，权限不足");
    }
    
    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数 %s 类型不匹配，需要 %s 类型", 
            e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型不匹配异常: {} - 请求路径: {}", message, request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("数据库异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return R.fail(ResultCode.DATABASE_ERROR, "数据库操作失败");
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "系统内部错误");
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        return R.fail(ResultCode.BAD_REQUEST, StrUtil.isNotBlank(e.getMessage()) ? e.getMessage() : "参数错误");
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "系统运行时错误");
    }
    
    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "系统内部错误");
    }
}