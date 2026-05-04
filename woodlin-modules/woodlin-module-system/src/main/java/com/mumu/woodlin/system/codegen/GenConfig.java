package com.mumu.woodlin.system.codegen;

import lombok.Data;

/**
 * 代码生成配置
 *
 * @author yulin
 * @since 2026-06
 */
@Data
public class GenConfig {

    private String tableName;

    private Long dataSourceId;

    private String packageName;

    private String moduleName;

    private String businessName;

    private String functionName;

    private String author;
}
