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
 * SQL2API 动态数据源配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sql2api_datasource")
@Schema(description = "SQL2API 数据源配置")
public class Sql2ApiDatasource extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "datasource_id", type = IdType.ASSIGN_ID)
    private Long datasourceId;

    @TableField("datasource_name")
    private String datasourceName;

    @TableField("code")
    private String code;

    @TableField("database_type")
    private String databaseType;

    @TableField("driver_class")
    private String driverClass;

    @TableField("jdbc_url")
    private String jdbcUrl;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("test_query")
    private String testQuery;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("datasource_desc")
    private String datasourceDesc;
}
