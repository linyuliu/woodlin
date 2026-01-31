package com.mumu.woodlin.common.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典类型实体
 *
 * <p>添加 MyBatis-Plus 元数据注解，便于 CRUD 管理接口直接复用。</p>
 *
 * @author mumu
 * @since 2025-12-27
 */
@Data
@TableName("sys_dict_type")
@Schema(description = "字典类型")
public class SysDictType {

    /** 字典主键 */
    @TableId(value = "dict_id", type = IdType.ASSIGN_ID)
    @Schema(description = "字典主键")
    private Long dictId;

    /** 字典名称 */
    @TableField("dict_name")
    @Schema(description = "字典名称")
    private String dictName;

    /** 字典类型 */
    @TableField("dict_type")
    @Schema(description = "字典类型")
    private String dictType;

    /** 字典分类（system-系统字典，business-业务字典，custom-自定义字典） */
    @TableField("dict_category")
    @Schema(description = "字典分类")
    private String dictCategory;

    /** 状态（1-启用，0-禁用） */
    @TableField("status")
    @Schema(description = "状态", example = "1")
    private String status;

    /** 备注 */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /** 租户ID（NULL表示通用字典） */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
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
