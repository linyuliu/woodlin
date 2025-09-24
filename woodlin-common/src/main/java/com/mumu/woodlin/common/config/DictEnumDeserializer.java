package com.mumu.woodlin.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.mumu.woodlin.common.enums.DictEnum;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 字典枚举反序列化器
 * 
 * @author mumu
 * @description 支持字典枚举的反序列化，可以处理简单值和label-value对象结构
 * @since 2025-01-01
 */
public class DictEnumDeserializer<T extends Enum<T> & DictEnum> extends JsonDeserializer<T> implements ContextualDeserializer {
    
    private Class<T> enumClass;
    
    public DictEnumDeserializer() {
    }
    
    public DictEnumDeserializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        Class<?> rawClass = ctxt.getContextualType().getRawClass();
        if (rawClass.isEnum() && DictEnum.class.isAssignableFrom(rawClass)) {
            return new DictEnumDeserializer((Class) rawClass);
        }
        return this;
    }
    
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (enumClass == null) {
            throw new IOException("无法确定枚举类型");
        }
        
        JsonToken currentToken = p.getCurrentToken();
        
        if (currentToken == JsonToken.VALUE_STRING || currentToken == JsonToken.VALUE_NUMBER_INT) {
            // 处理简单值: "1" 或 1
            Object value = currentToken == JsonToken.VALUE_STRING ? p.getValueAsString() : p.getValueAsInt();
            return findEnumByValue(enumClass, value);
        } else if (currentToken == JsonToken.START_OBJECT) {
            // 处理对象结构: {"value":"1","label":"启用"}
            JsonNode node = p.getCodec().readTree(p);
            if (node.has("value")) {
                JsonNode valueNode = node.get("value");
                Object value = valueNode.isTextual() ? valueNode.asText() : valueNode.asInt();
                return findEnumByValue(enumClass, value);
            }
        }
        
        throw new IOException("无法反序列化枚举值: " + currentToken);
    }
    
    /**
     * 根据value找到对应的枚举实例
     */
    private T findEnumByValue(Class<T> enumClass, Object value) throws IOException {
        try {
            T[] enumConstants = enumClass.getEnumConstants();
            
            for (T enumConstant : enumConstants) {
                DictEnum dictEnum = enumConstant;
                Object enumValue = dictEnum.getValue();
                
                // 处理不同类型的值比较
                if (valuesEqual(enumValue, value)) {
                    return enumConstant;
                }
            }
            
            // 如果没有找到，尝试使用传统的valueOf方法
            if (value instanceof String) {
                try {
                    Method valueOf = enumClass.getMethod("valueOf", String.class);
                    return enumClass.cast(valueOf.invoke(null, value));
                } catch (Exception ignored) {
                    // 忽略错误，继续下面的逻辑
                }
            }
            
            throw new IOException("找不到对应的枚举值: " + value + " in " + enumClass.getSimpleName());
            
        } catch (Exception e) {
            throw new IOException("反序列化枚举时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 比较两个值是否相等，处理不同数据类型
     */
    private boolean valuesEqual(Object enumValue, Object inputValue) {
        if (enumValue == null && inputValue == null) {
            return true;
        }
        if (enumValue == null || inputValue == null) {
            return false;
        }
        
        // 如果类型相同，直接比较
        if (enumValue.getClass().equals(inputValue.getClass())) {
            return enumValue.equals(inputValue);
        }
        
        // 处理字符串和数字之间的转换
        if (enumValue instanceof String && inputValue instanceof Number) {
            return enumValue.equals(inputValue.toString());
        }
        if (enumValue instanceof Number && inputValue instanceof String) {
            try {
                return enumValue.toString().equals(inputValue);
            } catch (Exception e) {
                return false;
            }
        }
        
        // 其他情况，转换为字符串比较
        return enumValue.toString().equals(inputValue.toString());
    }
}