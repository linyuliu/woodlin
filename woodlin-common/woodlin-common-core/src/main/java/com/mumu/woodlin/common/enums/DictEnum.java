package com.mumu.woodlin.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

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
     * 获取字典描述（用于补充说明）
     *
     * @return 字典标签
     */
    String getDesc();

    /**
     * 获取字典项结构（用于JSON序列化）
     *
     * @return 字典项
     */
    @JsonValue
    default DictItem toDictItem() {
        return new DictItem(getValue(), getLabel(), getDesc());
    }

    /**
     * 字典项数据结构
     */
    @Data
    class DictItem {
        private final Object value;
        private final String label;
        private final String desc;

    }
}
