package com.mumu.woodlin.sql2api.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础设施数据源配置（通用）
 * <p>
 * 该实体对应 infra_datasource 表，作为平台级基础设施能力提供数据源管理。
 * 所有业务模块（如 sql2api、ETL、报表等）仅通过 datasource_code 消费数据源，不维护连接信息。
 * </p>
 *
 * @author mumu
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("infra_datasource")
@Schema(description = "基础设施数据源配置")
public class SqlDatasourceConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("datasource_code")
    @Schema(description = "数据源唯一编码（工程级）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String datasourceCode;

    @TableField("datasource_name")
    @Schema(description = "数据源名称（展示用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String datasourceName;

    @TableField("datasource_type")
    @Schema(description = "数据源类型：MYSQL / PG / ORACLE / DM / CLICKHOUSE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String datasourceType;

    @TableField("driver_class")
    @Schema(description = "JDBC Driver", requiredMode = Schema.RequiredMode.REQUIRED)
    private String driverClass;

    @TableField("jdbc_url")
    @Schema(description = "JDBC URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jdbcUrl;

    @TableField("username")
    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @TableField("password")
    @Schema(description = "密码（加密存储）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @TableField("test_sql")
    @Schema(description = "连通性校验SQL")
    private String testSql;

    @TableField("status")
    @Schema(description = "状态：1-启用 0-禁用", defaultValue = "1")
    private Integer status;

    @TableField("owner")
    @Schema(description = "负责人")
    private String owner;

    @TableField("biz_tags")
    @Schema(description = "业务标签（逗号分隔）")
    private String bizTags;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @TableField("ext_config")
    @Schema(description = "扩展配置（连接池/方言/特殊参数，JSON格式）")
    private String extConfig;

    @Version
    @TableField("version")
    @Schema(description = "版本号（乐观锁）")
    private Integer version;
}
