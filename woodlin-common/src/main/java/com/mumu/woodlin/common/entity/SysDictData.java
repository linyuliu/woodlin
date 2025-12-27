package com.mumu.woodlin.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典数据实体
 *
 * @author mumu
 * @description 动态字典数据表实体，存储具体的字典项数据
 * @since 2025-12-27
 */
@Data
public class SysDictData {

    /**
     * 字典数据主键
     */
    private Long dataId;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典描述
     */
    private String dictDesc;

    /**
     * 字典排序
     */
    private Integer dictSort;

    /**
     * 样式属性（其他样式扩展）
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（1-是，0-否）
     */
    private String isDefault;

    /**
     * 状态（1-启用，0-禁用）
     */
    private String status;

    /**
     * 扩展数据（JSON格式）
     */
    private String extraData;

    /**
     * 租户ID（NULL表示通用字典）
     */
    private String tenantId;

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
