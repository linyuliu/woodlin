package com.mumu.woodlin.sql2api.dsl;

import lombok.Data;
import lombok.Builder;

import java.util.Map;

/**
 * SQL参数定义
 * 
 * @author mumu
 * @description 简化的DSL参数定义，用于动态SQL参数绑定
 * @since 2025-01-01
 */
@Data
@Builder
public class SqlParam {
    
    /**
     * 参数名称
     */
    private String name;
    
    /**
     * 参数类型 (String, Integer, Long, Date, etc.)
     */
    private String type;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 参数描述
     */
    private String description;
    
    /**
     * 验证规则 (正则表达式或自定义验证器)
     */
    private String validation;
    
    /**
     * 示例值
     */
    private Object example;
    
    /**
     * 从请求参数中提取值
     * 
     * @param requestParams 请求参数
     * @return 提取的值
     */
    public Object extractValue(Map<String, Object> requestParams) {
        Object value = requestParams.get(name);
        
        // 如果没有值且有默认值，使用默认值
        if (value == null && defaultValue != null) {
            return defaultValue;
        }
        
        // 必填校验
        if (required && value == null) {
            throw new IllegalArgumentException("参数 " + name + " 不能为空");
        }
        
        // 类型转换
        return convertType(value);
    }
    
    /**
     * 类型转换
     */
    private Object convertType(Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            return switch (type.toLowerCase()) {
                case "string" -> String.valueOf(value);
                case "integer", "int" -> Integer.valueOf(String.valueOf(value));
                case "long" -> Long.valueOf(String.valueOf(value));
                case "double" -> Double.valueOf(String.valueOf(value));
                case "float" -> Float.valueOf(String.valueOf(value));
                case "boolean", "bool" -> Boolean.valueOf(String.valueOf(value));
                default -> value;
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("参数 " + name + " 类型转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证参数值
     */
    public boolean validate(Object value) {
        if (validation == null || validation.isEmpty()) {
            return true;
        }
        
        if (value == null) {
            return !required;
        }
        
        // 使用正则表达式验证
        return String.valueOf(value).matches(validation);
    }
}
