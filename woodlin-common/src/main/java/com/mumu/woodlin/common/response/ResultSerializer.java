package com.mumu.woodlin.common.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mumu.woodlin.common.config.ResponseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Result响应自定义序列化器
 * 
 * @author mumu
 * @description 根据配置和请求上下文动态控制响应字段的序列化，实现响应结构的灵活配置
 * 支持全局配置和请求级别的动态控制
 * @since 2025-01-10
 */
@Component
@RequiredArgsConstructor
public class ResultSerializer extends JsonSerializer<Result<?>> {
    
    private final ResponseProperties responseProperties;
    
    @Override
    public void serialize(Result<?> result, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        
        // 根据配置和请求上下文决定是否包含各个字段
        if (shouldIncludeField("code", responseProperties.shouldIncludeCode()) && result.getCode() != null) {
            gen.writeNumberField("code", result.getCode());
        }
        
        if (shouldIncludeField("message", responseProperties.shouldIncludeMessage()) && result.getMessage() != null) {
            gen.writeStringField("message", result.getMessage());
        }
        
        // data 字段始终包含（如果不为null），除非请求明确排除
        if (shouldIncludeField("data", true) && result.getData() != null) {
            gen.writeObjectField("data", result.getData());
        }
        
        if (shouldIncludeField("timestamp", responseProperties.shouldIncludeTimestamp()) && result.getTimestamp() != null) {
            gen.writeObjectField("timestamp", result.getTimestamp());
        }
        
        if (shouldIncludeField("requestId", responseProperties.shouldIncludeRequestId()) && result.getRequestId() != null) {
            gen.writeStringField("requestId", result.getRequestId());
        }
        
        gen.writeEndObject();
    }
    
    /**
     * 判断字段是否应该包含
     * 优先使用请求级别的配置，其次使用全局配置
     * 
     * @param fieldName 字段名
     * @param globalSetting 全局配置
     * @return 是否包含
     */
    private boolean shouldIncludeField(String fieldName, boolean globalSetting) {
        // 如果有请求级别的配置，使用请求级别的配置
        if (ResponseFieldContext.hasRequestLevelConfig()) {
            return ResponseFieldContext.shouldIncludeField(fieldName);
        }
        // 否则使用全局配置
        return globalSetting;
    }
}
