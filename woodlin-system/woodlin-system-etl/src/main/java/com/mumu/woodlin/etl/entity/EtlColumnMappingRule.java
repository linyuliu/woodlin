package com.mumu.woodlin.etl.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * ETL 字段映射规则实体。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_column_mapping_rule")
@Schema(description = "ETL字段映射规则")
public class EtlColumnMappingRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 映射规则主键ID。
     */
    @TableId(value = "mapping_id", type = IdType.ASSIGN_ID)
    @Schema(description = "映射规则主键ID")
    private Long mappingId;

    /**
     * ETL任务ID。
     */
    @TableField("job_id")
    @Schema(description = "ETL任务ID")
    private Long jobId;

    /**
     * 源schema名称。
     */
    @TableField("source_schema_name")
    @Schema(description = "源schema名称")
    private String sourceSchemaName;

    /**
     * 源表名称。
     */
    @TableField("source_table_name")
    @Schema(description = "源表名称")
    private String sourceTableName;

    /**
     * 源字段名称。
     */
    @TableField("source_column_name")
    @Schema(description = "源字段名称")
    private String sourceColumnName;

    /**
     * 源字段类型。
     */
    @TableField("source_column_type")
    @Schema(description = "源字段类型")
    private String sourceColumnType;

    /**
     * 目标schema名称。
     */
    @TableField("target_schema_name")
    @Schema(description = "目标schema名称")
    private String targetSchemaName;

    /**
     * 目标表名称。
     */
    @TableField("target_table_name")
    @Schema(description = "目标表名称")
    private String targetTableName;

    /**
     * 目标字段名称。
     */
    @TableField("target_column_name")
    @Schema(description = "目标字段名称")
    private String targetColumnName;

    /**
     * 目标字段类型。
     */
    @TableField("target_column_type")
    @Schema(description = "目标字段类型")
    private String targetColumnType;

    /**
     * 映射动作。
     */
    @TableField("mapping_action")
    @Schema(description = "映射动作", example = "INSERT")
    private String mappingAction;

    /**
     * 顺序号。
     */
    @TableField("ordinal_position")
    @Schema(description = "顺序号")
    private Integer ordinalPosition;

    /**
     * 是否启用。
     */
    @TableField("enabled")
    @Schema(description = "是否启用", example = "1")
    private String enabled;

    /**
     * 租户ID。
     */
    @TableField("tenant_id")
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 备注。
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
