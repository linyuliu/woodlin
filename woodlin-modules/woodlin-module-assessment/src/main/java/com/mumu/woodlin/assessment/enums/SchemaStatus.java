package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Schema/版本状态
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum SchemaStatus {

    DRAFT("draft", "草稿"),
    COMPILED("compiled", "已编译（DSL/Schema已通过校验）"),
    PUBLISHED("published", "已发布（对应活跃发布实例）"),
    DEPRECATED("deprecated", "已废弃（不再可引用）"),
    ARCHIVED("archived", "已归档");

    private final String code;
    private final String label;
}
