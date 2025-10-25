package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author mumu
 * @description 性别枚举定义
 * @since 2025-01-01
 */
@Getter
@AllArgsConstructor
public enum Gender implements DictEnum {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女");

    /**
     * 性别代码
     */
    private final Integer code;

    /**
     * 性别描述
     */
    private final String description;

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
        return description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 性别代码
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
        for (Gender gender : values()) {
            if (gender.getDescription().equals(description)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
