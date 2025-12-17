package com.mumu.woodlin.sql2api.model.request;

import java.util.List;

import com.mumu.woodlin.sql2api.model.DatabaseDocFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据库文档导出请求
 */
@Data
public class DatabaseDocExportRequest {

    /**
     * 目标数据源名称
     */
    @NotBlank(message = "数据源名称不能为空")
    private String datasourceName;

    /**
     * 导出格式
     */
    @NotNull(message = "导出格式不能为空")
    private DatabaseDocFormat format;

    /**
     * 指定需要导出的表，可为空表示导出全部
     */
    private List<String> tables;
}
