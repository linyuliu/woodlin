package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举（GB/T 2261.1-2003 个人基本信息分类与代码 第1部分：人的性别代码）
 *
 * @author mumu
 * @description 性别枚举定义，遵循中国国家标准GB/T 2261.1-2003
 *              0-未知的性别, 1-男性, 2-女性, 9-未说明的性别
 *              参考：ISO 5218国际标准
 * @since 2025-01-01
 */
@Getter
@AllArgsConstructor
public enum Gender implements DictEnum {

    /**
     * 未知的性别（GB/T 2261.1代码：0）
     * 适用场景：不明、不清楚或未提供性别信息
     */
    UNKNOWN(0, "未知的性别", "Unknown"),

    /**
     * 男性（GB/T 2261.1代码：1）
     */
    MALE(1, "男", "Male"),

    /**
     * 女性（GB/T 2261.1代码：2）
     */
    FEMALE(2, "女", "Female"),

    /**
     * 未说明的性别（GB/T 2261.1代码：9）
     * 适用场景：不便于明示、不愿透露或其他原因未说明
     */
    NOT_APPLICABLE(9, "未说明的性别", "Not Applicable");

    /**
     * 性别代码（符合GB/T 2261.1标准）
     */
    private final Integer code;

    /**
     * 性别描述（中文）
     */
    private final String description;

    /**
     * 性别描述（英文，符合ISO 5218）
     */
    private final String descriptionEn;

    @Override
    public Object getValue() {
        return code;
    }

    @Override
    public String getLabel() {
        return description;
    }

    @Override
    public String getDesc() {
        return description + " / " + descriptionEn;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 性别代码（GB/T 2261.1标准代码）
     * @return 性别枚举
     */
    public static Gender fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (Gender gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据描述获取枚举
     *
     * @param description 性别描述
     * @return 性别枚举
     */
    public static Gender fromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        for (Gender gender : values()) {
            if (gender.getDescription().equals(description) || 
                gender.getDescriptionEn().equalsIgnoreCase(description)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
