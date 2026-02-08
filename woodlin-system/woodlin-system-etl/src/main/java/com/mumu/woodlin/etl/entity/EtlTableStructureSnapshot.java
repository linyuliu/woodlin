package com.mumu.woodlin.etl.entity;

import java.io.Serial;
import java.time.LocalDateTime;

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
 * ETL 表结构快照实体。
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_etl_table_structure_snapshot")
@Schema(description = "ETL表结构快照")
public class EtlTableStructureSnapshot extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 快照ID。
     */
    @TableId(value = "snapshot_id", type = IdType.ASSIGN_ID)
    @Schema(description = "快照ID")
    private Long snapshotId;

    /**
     * ETL任务ID。
     */
    @TableField("job_id")
    @Schema(description = "ETL任务ID")
    private Long jobId;

    /**
     * 数据源名称。
     */
    @TableField("datasource_name")
    @Schema(description = "数据源名称")
    private String datasourceName;

    /**
     * schema 名称。
     */
    @TableField("schema_name")
    @Schema(description = "schema名称")
    private String schemaName;

    /**
     * 表名称。
     */
    @TableField("table_name")
    @Schema(description = "表名称")
    private String tableName;

    /**
     * 字段数量。
     */
    @TableField("column_count")
    @Schema(description = "字段数量")
    private Integer columnCount;

    /**
     * 主键字段列表。
     */
    @TableField("primary_key_columns")
    @Schema(description = "主键字段列表")
    private String primaryKeyColumns;

    /**
     * 结构摘要。
     */
    @TableField("structure_digest")
    @Schema(description = "结构摘要")
    private String structureDigest;

    /**
     * 快照时间。
     */
    @TableField("snapshot_time")
    @Schema(description = "快照时间")
    private LocalDateTime snapshotTime;

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
