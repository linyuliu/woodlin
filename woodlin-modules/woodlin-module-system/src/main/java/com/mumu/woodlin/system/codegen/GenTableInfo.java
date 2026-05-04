package com.mumu.woodlin.system.codegen;

import lombok.Data;

/**
 * 代码生成 - 数据库表信息
 *
 * @author yulin
 * @since 2026-06
 */
@Data
public class GenTableInfo {

    private String tableName;

    private String tableComment;

    private String createTime;

    private String updateTime;
}
