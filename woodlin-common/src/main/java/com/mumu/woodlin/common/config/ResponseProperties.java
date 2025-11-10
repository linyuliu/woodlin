package com.mumu.woodlin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 响应配置属性
 * 
 * @author mumu
 * @description 控制全局响应结果中包含哪些字段，实现动态化配置
 * @since 2025-01-10
 */
@Data
@Component
@ConfigurationProperties(prefix = "woodlin.response")
public class ResponseProperties {
    
    /**
     * 是否包含时间戳字段
     */
    private Boolean includeTimestamp = true;
    
    /**
     * 是否包含请求ID字段
     */
    private Boolean includeRequestId = false;
    
    /**
     * 是否包含消息字段
     */
    private Boolean includeMessage = true;
    
    /**
     * 是否包含状态码字段
     */
    private Boolean includeCode = true;
    
    /**
     * 响应字段过滤模式
     * - NONE: 不过滤，包含所有字段（默认）
     * - MINIMAL: 最小模式，仅包含 code 和 data
     * - CUSTOM: 自定义模式，根据 include* 配置决定
     */
    private FilterMode filterMode = FilterMode.NONE;
    
    /**
     * 响应字段过滤模式枚举
     */
    public enum FilterMode {
        /**
         * 不过滤，包含所有字段
         */
        NONE,
        
        /**
         * 最小模式，仅包含 code 和 data
         */
        MINIMAL,
        
        /**
         * 自定义模式，根据配置决定包含哪些字段
         */
        CUSTOM
    }
    
    /**
     * 检查是否应该包含时间戳
     */
    public boolean shouldIncludeTimestamp() {
        return filterMode == FilterMode.NONE || 
               (filterMode == FilterMode.CUSTOM && includeTimestamp);
    }
    
    /**
     * 检查是否应该包含请求ID
     */
    public boolean shouldIncludeRequestId() {
        return filterMode == FilterMode.NONE || 
               (filterMode == FilterMode.CUSTOM && includeRequestId);
    }
    
    /**
     * 检查是否应该包含消息
     */
    public boolean shouldIncludeMessage() {
        return filterMode == FilterMode.NONE || 
               (filterMode == FilterMode.CUSTOM && includeMessage);
    }
    
    /**
     * 检查是否应该包含状态码
     */
    public boolean shouldIncludeCode() {
        return filterMode == FilterMode.NONE || 
               filterMode == FilterMode.MINIMAL ||
               (filterMode == FilterMode.CUSTOM && includeCode);
    }
}
