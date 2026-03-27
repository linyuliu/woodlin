package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学历枚举（参考GB/T 4658-2006 学历代码）
 *
 * @author mumu
 * @description 教育程度标准代码，按照国家教育部标准
 * @since 2025-12-26
 */
@Getter
@AllArgsConstructor
public enum EducationLevel implements DictEnum {

    /**
     * 研究生教育
     */
    DOCTORAL(10, "博士研究生", "Doctoral"),
    MASTER(11, "硕士研究生", "Master"),
    
    /**
     * 大学本科
     */
    BACHELOR(20, "大学本科", "Bachelor"),
    
    /**
     * 大学专科
     */
    COLLEGE(30, "大学专科", "College"),
    
    /**
     * 中等教育
     */
    TECHNICAL_SECONDARY(40, "中等专科", "Technical Secondary"),
    VOCATIONAL_HIGH(41, "职业高中", "Vocational High School"),
    SENIOR_HIGH(42, "普通高中", "Senior High School"),
    JUNIOR_HIGH(43, "初中", "Junior High School"),
    
    /**
     * 初等教育
     */
    PRIMARY(50, "小学", "Primary School"),
    
    /**
     * 其他
     */
    OTHER(90, "其他", "Other"),
    UNKNOWN(99, "未知", "Unknown");

    /**
     * 学历代码
     */
    private final Integer code;

    /**
     * 学历名称（中文）
     */
    private final String name;

    /**
     * 学历名称（英文）
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

    public static EducationLevel fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (EducationLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return UNKNOWN;
    }
}
