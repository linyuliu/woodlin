package com.mumu.woodlin.common.exception;

import lombok.Getter;

import java.io.Serial;

import static com.mumu.woodlin.common.constant.CommonConstant.FAIL_CODE;

/**
 * 业务异常类
 * 
 * @author mumu
 * @description 系统业务异常的基础类，用于处理业务逻辑中的异常情况
 * @since 2025-01-01
 */
@Getter
public class BusinessException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = FAIL_CODE;
        this.message = message;
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = FAIL_CODE;
        this.message = message;
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原因异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 静态工厂方法 - 创建业务异常
     * 
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }
    
    /**
     * 静态工厂方法 - 创建业务异常
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }
    
    /**
     * 静态工厂方法 - 创建业务异常
     * 
     * @param message 错误消息
     * @param cause 原因异常
     * @return 业务异常
     */
    public static BusinessException of(String message, Throwable cause) {
        return new BusinessException(message, cause);
    }
    
    /**
     * 静态工厂方法 - 创建业务异常
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原因异常
     * @return 业务异常
     */
    public static BusinessException of(Integer code, String message, Throwable cause) {
        return new BusinessException(code, message, cause);
    }
    
}