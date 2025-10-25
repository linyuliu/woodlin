package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author mumu
 * @description 用户状态枚举定义
 * @since 2025-01-01
 */
@Getter
@AllArgsConstructor
public enum UserStatus implements DictEnum {

    /**
     * 启用
     */
    ENABLE("1", "启用"),

    /**
     * 禁用
     */
    DISABLE("0", "禁用");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
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
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 用户状态枚举
     */
    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return DISABLE;
    }

    /**
     * 根据描述获取枚举
     *
     * @param description 状态描述
     * @return 用户状态枚举
     */
    public static UserStatus fromDescription(String description) {
        for (UserStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        return DISABLE;
    }
}
