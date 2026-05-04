package com.mumu.woodlin.system.codegen;

import lombok.Data;

/**
 * 代码生成 - 表字段信息
 *
 * @author yulin
 * @since 2026-06
 */
@Data
public class GenColumnInfo {

    private String columnName;

    private String columnComment;

    private String columnType;

    private String javaType;

    private String javaField;

    private String isPk;

    private String isRequired;

    private String isInsert;

    private String isEdit;

    private String isList;

    private String isQuery;

    private String queryType;

    private String htmlType;
}
