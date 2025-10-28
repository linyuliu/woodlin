package com.mumu.woodlin.common.response;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import com.mumu.woodlin.common.enums.ResultCode;

/**
 * 统一响应结果封装 - 简化版
 * 
 * @author mumu
 * @description 简化的响应结果封装，提供R.ok()等便捷方法
 * @param <T> 响应数据类型
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应结果")
public class R<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应状态码
     */
    @Schema(description = "响应状态码", example = "200")
    private Integer code;
    
    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;
    
    /**
     * 响应时间戳
     */
    @Schema(description = "响应时间戳")
    private LocalDateTime timestamp;
    
    /**
     * 私有构造函数
     */
    private R() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 私有构造函数
     */
    private R(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 私有构造函数
     */
    private R(ResultCode resultCode, T data) {
        this(resultCode.getCode(), resultCode.getMessage(), data);
    }
    
    /**
     * 成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS, null);
    }
    
    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS, data);
    }
    
    /**
     * 成功响应（自定义消息）
     * 
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> R<T> ok(String message) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, null);
    }
    
    /**
     * 成功响应（自定义消息和数据）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> R<T> fail() {
        return new R<>(ResultCode.INTERNAL_SERVER_ERROR, null);
    }
    
    /**
     * 失败响应（自定义消息）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> R<T> fail(String message) {
        return new R<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }
    
    /**
     * 失败响应（使用结果码枚举）
     * 
     * @param resultCode 结果码枚举
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode, null);
    }
    
    /**
     * 失败响应（使用结果码枚举和自定义消息）
     * 
     * @param resultCode 结果码枚举
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null);
    }
    
    /**
     * 失败响应（自定义状态码和消息）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }
    
    /**
     * 自定义响应
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> of(Integer code, String message, T data) {
        return new R<>(code, message, data);
    }
    
    /**
     * 判断是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == ResultCode.SUCCESS.getCode();
    }
    
    /**
     * 判断是否失败
     * 
     * @return 是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }
}