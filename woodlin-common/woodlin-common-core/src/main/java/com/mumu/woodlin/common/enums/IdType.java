package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 证件类型枚举（GB/T 2261.4 证件类型代码）
 *
 * @author mumu
 * @description 常用证件类型标准代码
 * @since 2025-12-26
 */
@Getter
@AllArgsConstructor
public enum IdType implements DictEnum {

    /**
     * 居民身份证
     */
    ID_CARD(1, "居民身份证", "ID Card", "^\\d{15}$|^\\d{17}[\\dXx]$"),
    
    /**
     * 护照
     */
    PASSPORT(2, "护照", "Passport", "^[EeKkGgDdSsPpHh]\\d{8}$|^14\\d{7}$|^15\\d{7}$|^G\\d{8}$|^P\\d{7}$"),
    
    /**
     * 军官证
     */
    OFFICER_CARD(3, "军官证", "Officer Card", "^\\d{6,20}$"),
    
    /**
     * 士兵证
     */
    SOLDIER_CARD(4, "士兵证", "Soldier Card", "^\\d{6,20}$"),
    
    /**
     * 港澳居民来往内地通行证
     */
    HK_MACAO_PASS(5, "港澳居民来往内地通行证", "Home Return Permit", "^[HhMm]\\d{8,10}$"),
    
    /**
     * 台湾居民来往大陆通行证
     */
    TAIWAN_PASS(6, "台湾居民来往大陆通行证", "Taiwan Compatriot Pass", "^\\d{8}$|^\\d{10}$"),
    
    /**
     * 外国人居留证
     */
    FOREIGN_RESIDENCE(7, "外国人居留证", "Foreign Residence Permit", "^[A-Za-z0-9]+$"),
    
    /**
     * 外国人永久居留身份证
     */
    FOREIGN_PERMANENT_RESIDENCE(8, "外国人永久居留身份证", "Foreign Permanent Residence ID", "^[A-Za-z0-9]+$"),
    
    /**
     * 港澳台居民居住证
     */
    HMT_RESIDENCE(9, "港澳台居民居住证", "HMT Residence Permit", "^\\d{18}$"),
    
    /**
     * 其他
     */
    OTHER(99, "其他", "Other", ".*");

    /**
     * 证件类型代码
     */
    private final Integer code;

    /**
     * 证件类型名称（中文）
     */
    private final String name;

    /**
     * 证件类型名称（英文）
     */
    private final String nameEn;

    /**
     * 证件号码正则表达式（用于验证）
     */
    private final String pattern;

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

    /**
     * 验证证件号码格式是否正确
     *
     * @param idNumber 证件号码
     * @return 是否符合格式
     */
    public boolean validate(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return false;
        }
        return idNumber.matches(pattern);
    }

    public static IdType fromCode(Integer code) {
        if (code == null) {
            return ID_CARD;
        }
        for (IdType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
