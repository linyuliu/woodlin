package com.mumu.woodlin.system.codegen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码生成模板文件
 *
 * @author yulin
 * @since 2026-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFile {

    private String name;

    private String content;
}
