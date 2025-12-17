package com.mumu.woodlin.sql2api.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增数据源请求
 *
 * @author mumu
 * @since 2025-01-01
 */
@Data
public class AddDatasourceRequest {

    /**
     * 数据源名称
     */
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    /**
     * JDBC 连接串
     */
    @NotBlank(message = "数据源URL不能为空")
    private String url;

    /**
     * 数据库用户名
     */
    @NotBlank(message = "数据库用户名不能为空")
    private String username;

    /**
     * 数据库密码
     */
    @NotBlank(message = "数据库密码不能为空")
    private String password;

    /**
     * 驱动类名称，可选
     */
    private String driverClassName;
}
