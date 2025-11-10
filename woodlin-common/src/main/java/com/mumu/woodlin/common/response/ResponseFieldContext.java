package com.mumu.woodlin.common.response;

import java.util.Set;

/**
 * 响应字段上下文持有者
 * 
 * @author mumu
 * @description 用于在请求生命周期内存储响应字段配置，支持请求级别的动态控制
 * @since 2025-01-10
 */
public class ResponseFieldContext {
    
    private static final ThreadLocal<Set<String>> INCLUDE_FIELDS = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> EXCLUDE_FIELDS = new ThreadLocal<>();
    
    /**
     * 设置需要包含的字段
     * 
     * @param fields 字段集合，如 ["code", "data", "message"]
     */
    public static void setIncludeFields(Set<String> fields) {
        INCLUDE_FIELDS.set(fields);
    }
    
    /**
     * 设置需要排除的字段
     * 
     * @param fields 字段集合，如 ["timestamp", "requestId"]
     */
    public static void setExcludeFields(Set<String> fields) {
        EXCLUDE_FIELDS.set(fields);
    }
    
    /**
     * 获取需要包含的字段
     * 
     * @return 字段集合
     */
    public static Set<String> getIncludeFields() {
        return INCLUDE_FIELDS.get();
    }
    
    /**
     * 获取需要排除的字段
     * 
     * @return 字段集合
     */
    public static Set<String> getExcludeFields() {
        return EXCLUDE_FIELDS.get();
    }
    
    /**
     * 检查字段是否应该包含
     * 
     * @param fieldName 字段名称
     * @return 是否包含
     */
    public static boolean shouldIncludeField(String fieldName) {
        Set<String> includeFields = INCLUDE_FIELDS.get();
        Set<String> excludeFields = EXCLUDE_FIELDS.get();
        
        // 如果设置了排除字段，且字段在排除列表中，则不包含
        if (excludeFields != null && excludeFields.contains(fieldName)) {
            return false;
        }
        
        // 如果设置了包含字段，只包含列表中的字段
        if (includeFields != null) {
            return includeFields.contains(fieldName);
        }
        
        // 默认包含
        return true;
    }
    
    /**
     * 清除上下文
     */
    public static void clear() {
        INCLUDE_FIELDS.remove();
        EXCLUDE_FIELDS.remove();
    }
    
    /**
     * 检查是否有请求级别的配置
     * 
     * @return 是否有配置
     */
    public static boolean hasRequestLevelConfig() {
        return INCLUDE_FIELDS.get() != null || EXCLUDE_FIELDS.get() != null;
    }
}
