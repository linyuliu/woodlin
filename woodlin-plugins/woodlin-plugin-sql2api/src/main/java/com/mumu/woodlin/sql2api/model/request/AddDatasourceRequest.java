package com.mumu.woodlin.sql2api.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据源配置请求（admin CRUD 用）
 */
@Data
public class AddDatasourceRequest {

    @NotBlank(message = "数据源编码不能为空")
    private String datasourceCode;

    @NotBlank(message = "数据源名称不能为空")
    private String datasourceName;

    @NotBlank(message = "数据源类型不能为空")
    private String datasourceType;

    @NotBlank(message = "JDBC URL 不能为空")
    private String jdbcUrl;

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "驱动类不能为空")
    private String driverClass;

    /**
     * 主键（更新时使用）
     */
    private Long id;

    /**
     * 连通性校验 SQL
     */
    private String testSql;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 业务标签（逗号分隔）
     */
    private String bizTags;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展配置（JSON）
     */
    private String extConfig;
}
