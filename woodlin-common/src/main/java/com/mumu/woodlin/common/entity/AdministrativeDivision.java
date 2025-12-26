package com.mumu.woodlin.common.entity;

import lombok.Data;

/**
 * 行政区划实体（GB/T 2260 中华人民共和国行政区划代码）
 *
 * @author mumu
 * @description 省市区三级行政区划数据，数据来源于民政部
 *              代码规则：
 *              - 省级：前2位有效，后4位为0（如110000）
 *              - 市级：前4位有效，后2位为0（如110100）
 *              - 区县级：全部6位有效（如110101）
 * @since 2025-12-26
 */
@Data
public class AdministrativeDivision {

    /**
     * 行政区划代码（6位）
     */
    private String code;

    /**
     * 行政区划名称
     */
    private String name;

    /**
     * 父级代码
     */
    private String parentCode;

    /**
     * 层级：1-省级，2-市级，3-区县级
     */
    private Integer level;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 是否直辖市/特别行政区
     */
    private Boolean isMunicipality;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
