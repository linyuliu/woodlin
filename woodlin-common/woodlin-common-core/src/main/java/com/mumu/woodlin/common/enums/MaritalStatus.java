package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 婚姻状况枚举（GB/T 2261.2-2003 个人基本信息分类与代码 第2部分：婚姻状况代码）
 *
 * @author mumu
 * @description 婚姻状况标准代码
 * @since 2025-12-26
 */
@Getter
@AllArgsConstructor
public enum MaritalStatus implements DictEnum {

    /**
     * 未婚
     */
    UNMARRIED(10, "未婚", "Unmarried"),
    
    /**
     * 已婚
     */
    MARRIED(20, "已婚", "Married"),
    
    /**
     * 丧偶
     */
    WIDOWED(30, "丧偶", "Widowed"),
    
    /**
     * 离婚
     */
    DIVORCED(40, "离婚", "Divorced"),
    
    /**
     * 未说明的婚姻状况
     */
    UNKNOWN(90, "未说明", "Unknown");

    /**
     * 婚姻状况代码（GB/T 2261.2标准）
     */
    private final Integer code;

    /**
     * 婚姻状况名称（中文）
     */
    private final String name;

    /**
     * 婚姻状况名称（英文）
     */
    private final String nameEn;

    @Override
    public Object getValue() {
        return code;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String getDesc() {
        return name + " / " + nameEn;
    }

    public static MaritalStatus fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (MaritalStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
