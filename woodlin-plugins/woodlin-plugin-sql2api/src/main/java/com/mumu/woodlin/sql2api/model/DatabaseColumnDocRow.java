package com.mumu.woodlin.sql2api.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 数据库字段导出行
 */
@Data
@Builder
public class DatabaseColumnDocRow {

    @ExcelProperty("数据库")
    private String databaseName;

    @ExcelProperty("表名")
    private String tableName;

    @ExcelProperty("表注释")
    private String tableComment;

    @ExcelProperty("列名")
    private String columnName;

    @ExcelProperty("数据类型")
    private String dataType;

    @ExcelProperty("可为空")
    private String nullable;

    @ExcelProperty("默认值")
    private String defaultValue;

    @ExcelProperty("主键")
    private String primaryKey;

    @ExcelProperty("列注释")
    private String comment;
}
