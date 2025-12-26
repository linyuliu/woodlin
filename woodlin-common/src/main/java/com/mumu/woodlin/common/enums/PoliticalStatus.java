package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 政治面貌枚举（GB/T 4762-1984 政治面貌代码）
 *
 * @author mumu
 * @description 政治面貌标准代码
 * @since 2025-12-26
 */
@Getter
@AllArgsConstructor
public enum PoliticalStatus implements DictEnum {

    /**
     * 中共党员
     */
    CPC_MEMBER(1, "中共党员", "CPC Member"),
    
    /**
     * 中共预备党员
     */
    CPC_PROBATIONARY(2, "中共预备党员", "CPC Probationary Member"),
    
    /**
     * 共青团员
     */
    CYLC_MEMBER(3, "共青团员", "CYLC Member"),
    
    /**
     * 民革会员
     */
    RCC_MEMBER(4, "民革会员", "Revolutionary Committee Member"),
    
    /**
     * 民盟盟员
     */
    CDL_MEMBER(5, "民盟盟员", "Democratic League Member"),
    
    /**
     * 民建会员
     */
    CDCA_MEMBER(6, "民建会员", "Democratic National Construction Association Member"),
    
    /**
     * 民进会员
     */
    CAPD_MEMBER(7, "民进会员", "Association for Promoting Democracy Member"),
    
    /**
     * 农工党党员
     */
    CAPWD_MEMBER(8, "农工党党员", "Peasants and Workers Democratic Party Member"),
    
    /**
     * 致公党党员
     */
    CPPIC_MEMBER(9, "致公党党员", "Party for Public Interest Member"),
    
    /**
     * 九三学社社员
     */
    JSSS_MEMBER(10, "九三学社社员", "Jiusan Society Member"),
    
    /**
     * 台盟盟员
     */
    TALC_MEMBER(11, "台盟盟员", "Taiwan Democratic Self-Government League Member"),
    
    /**
     * 无党派民主人士
     */
    NON_PARTISAN(12, "无党派民主人士", "Non-partisan Democrat"),
    
    /**
     * 群众
     */
    MASS(13, "群众", "Mass");

    /**
     * 政治面貌代码
     */
    private final Integer code;

    /**
     * 政治面貌名称（中文）
     */
    private final String name;

    /**
     * 政治面貌名称（英文）
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

    public static PoliticalStatus fromCode(Integer code) {
        if (code == null) {
            return MASS;
        }
        for (PoliticalStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return MASS;
    }
}
