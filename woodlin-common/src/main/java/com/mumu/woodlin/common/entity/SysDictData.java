package com.mumu.woodlin.common.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典数据实体
 *
 * <p>补充 MyBatis-Plus 注解，便于直接进行 CRUD 操作。</p>
 *
 * @author mumu
 * @since 2025-12-27
 */
@Data
@TableName("sys_dict_data")
@Schema(description = "字典数据")
public class SysDictData {

    /** 字典数据主键 */
    @TableId(value = "data_id", type = IdType.ASSIGN_ID)
    @Schema(description = "字典数据主键")
    private Long dataId;

    /** 字典类型 */
    @TableField("dict_type")
    @Schema(description = "字典类型")
    private String dictType;

    /** 字典标签 */
    @TableField("dict_label")
    @Schema(description = "字典标签")
    private String dictLabel;

    /** 字典键值 */
    @TableField("dict_value")
    @Schema(description = "字典键值")
    private String dictValue;

    /** 字典描述 */
    @TableField("dict_desc")
    @Schema(description = "字典描述")
    private String dictDesc;

    /** 字典排序 */
    @TableField("dict_sort")
    @Schema(description = "字典排序")
    private Integer dictSort;

    /** 样式属性（其他样式扩展） */
    @TableField("css_class")
    private String cssClass;

    /** 表格回显样式 */
    @TableField("list_class")
    private String listClass;

    /** 是否默认（1-是，0-否） */
    @TableField("is_default")
    private String isDefault;

    /** 状态（1-启用，0-禁用） */
    @TableField("status")
    private String status;

    /** 扩展数据（JSON格式） */
    @TableField("extra_data")
    private String extraData;

    /** 租户ID（NULL表示通用字典） */
    @TableField("tenant_id")
    private String tenantId;

    /** 创建者 */
    @TableField("create_by")
    private String createBy;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新者 */
    @TableField("update_by")
    private String updateBy;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 删除标识（0-正常，1-删除） */
    @TableField("deleted")
    private String deleted;
}
