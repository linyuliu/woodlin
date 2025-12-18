package com.mumu.woodlin.sql2api.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mumu.woodlin.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础设施数据源配置（通用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("infra_datasource")
@Schema(description = "基础设施数据源配置")
public class SqlDatasourceConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("datasource_code")
    private String datasourceCode;

    @TableField("datasource_name")
    private String datasourceName;

    @TableField("datasource_type")
    private String datasourceType;

    @TableField("driver_class")
    private String driverClass;

    @TableField("jdbc_url")
    private String jdbcUrl;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("test_sql")
    private String testSql;

    @TableField("status")
    private Integer status;

    @TableField("owner")
    private String owner;

    @TableField("biz_tags")
    private String bizTags;

    @TableField("remark")
    private String remark;

    @TableField("ext_config")
    private String extConfig;
}
