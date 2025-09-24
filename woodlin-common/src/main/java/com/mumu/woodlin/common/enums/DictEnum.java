package com.mumu.woodlin.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 字典枚举接口
 * 
 * @author mumu
 * @description 为需要返回label-value结构的枚举提供统一接口
 * @since 2025-01-01
 */
public interface DictEnum {
    
    /**
     * 获取字典值（用于数据库存储和后端逻辑）
     * 
     * @return 字典值
     */
    Object getValue();
    
    /**
     * 获取字典标签（用于前端显示）
     * 
     * @return 字典标签
     */
    String getLabel();
    
    /**
     * 获取字典项结构（用于JSON序列化）
     * 
     * @return 字典项
     */
    @JsonValue
    default DictItem toDictItem() {
        return new DictItem(getValue(), getLabel());
    }
    
    /**
     * 字典项数据结构
     */
    class DictItem {
        private final Object value;
        private final String label;
        
        public DictItem(Object value, String label) {
            this.value = value;
            this.label = label;
        }
        
        public Object getValue() {
            return value;
        }
        
        public String getLabel() {
            return label;
        }
    }
}