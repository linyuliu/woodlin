package com.mumu.woodlin.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典类型实体
 *
 * @author mumu
 * @description 动态字典类型表实体，用于管理系统中的各种字典分类
 * @since 2025-12-27
 */
@Data
public class SysDictType {

    /**
     * 字典主键
     */
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典分类（system-系统字典，business-业务字典，custom-自定义字典）
     */
    private String dictCategory;

    /**
     * 状态（1-启用，0-禁用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

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
