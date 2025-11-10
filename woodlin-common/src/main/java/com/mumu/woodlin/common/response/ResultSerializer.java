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
 * @description 根据配置动态控制响应字段的序列化，实现响应结构的灵活配置
 * @since 2025-01-10
 */
@Component
@RequiredArgsConstructor
public class ResultSerializer extends JsonSerializer<Result<?>> {
    
    private final ResponseProperties responseProperties;
    
    @Override
    public void serialize(Result<?> result, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        
        // 根据配置决定是否包含各个字段
        if (responseProperties.shouldIncludeCode() && result.getCode() != null) {
            gen.writeNumberField("code", result.getCode());
        }
        
        if (responseProperties.shouldIncludeMessage() && result.getMessage() != null) {
            gen.writeStringField("message", result.getMessage());
        }
        
        // data 字段始终包含（如果不为null）
        if (result.getData() != null) {
            gen.writeObjectField("data", result.getData());
        }
        
        if (responseProperties.shouldIncludeTimestamp() && result.getTimestamp() != null) {
            gen.writeObjectField("timestamp", result.getTimestamp());
        }
        
        if (responseProperties.shouldIncludeRequestId() && result.getRequestId() != null) {
            gen.writeStringField("requestId", result.getRequestId());
        }
        
        gen.writeEndObject();
    }
}
