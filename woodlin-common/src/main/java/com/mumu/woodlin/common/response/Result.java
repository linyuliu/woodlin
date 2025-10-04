package com.mumu.woodlin.common.response;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.mumu.woodlin.common.constant.CommonConstant.FAIL_CODE;
import static com.mumu.woodlin.common.constant.CommonConstant.SUCCESS_CODE;

/**
 * 统一响应结果封装
 * 
 * @author mumu
 * @description 系统统一响应结果封装，用于API接口返回标准化数据格式
 * @param <T> 响应数据类型
 * @since 2025-01-01
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {
    
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
     * 请求ID（用于链路追踪）
     */
    @Schema(description = "请求ID")
    private String requestId;
    
    /**
     * 私有构造函数，防止直接实例化
     */
    protected Result() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 私有构造函数
     */
    private Result(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "操作成功", null);
    }
    
    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }
    
    /**
     * 成功响应（自定义消息）
     * 
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(SUCCESS_CODE, message, null);
    }
    
    /**
     * 成功响应（自定义消息和数据）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail() {
        return new Result<>(FAIL_CODE, "操作失败", null);
    }
    
    /**
     * 失败响应（自定义消息）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(FAIL_CODE, message, null);
    }
    
    /**
     * 失败响应（自定义状态码和消息）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
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
    public static <T> Result<T> of(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    /**
     * 判断是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == SUCCESS_CODE;
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