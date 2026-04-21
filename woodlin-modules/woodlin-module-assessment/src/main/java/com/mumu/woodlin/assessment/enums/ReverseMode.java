package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 反向计分模式
 *
 * <p>反向题处理方式：NONE 表示正向，FORMULA 表示按公式 (max + min - x) 自动反向，
 * TABLE 表示按选项映射表人工指定反向值。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum ReverseMode {

    NONE("none", "不反向"),
    /** 公式反向：score = maxScore + minScore - rawScore */
    FORMULA("formula", "公式反向"),
    /** 映射表反向：通过 option.score_reverse_value 字段指定 */
    TABLE("table", "映射表反向");

    private final String code;
    private final String label;
}
