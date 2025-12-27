package com.mumu.woodlin.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行政区划实体（树形结构）
 *
 * @author mumu
 * @description GB/T 2260标准的行政区划数据，支持树形结构存储和查询
 * @since 2025-12-27
 */
@Data
public class SysRegion {

    /**
     * 区划主键
     */
    private Long regionId;

    /**
     * 区划代码（GB/T 2260标准6位代码）
     */
    private String regionCode;

    /**
     * 区划名称
     */
    private String regionName;

    /**
     * 父区划代码
     */
    private String parentCode;

    /**
     * 区划层级（1-省级，2-市级，3-区县级，4-街道级）
     */
    private Integer regionLevel;

    /**
     * 区划类型（province-省，city-市，district-区县，street-街道）
     */
    private String regionType;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 拼音缩写
     */
    private String pinyinAbbr;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否直辖市/特别行政区（1-是，0-否）
     */
    private String isMunicipality;

    /**
     * 状态（1-启用，0-禁用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标识（0-正常，1-删除）
     */
    private String deleted;
}
