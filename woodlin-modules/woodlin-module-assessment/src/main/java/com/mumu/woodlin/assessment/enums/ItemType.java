package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 题目类型
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum ItemType {

    SINGLE_CHOICE("single_choice", "单选题"),
    MULTIPLE_CHOICE("multiple_choice", "多选题"),
    MATRIX_SINGLE("matrix_single", "矩阵单选"),
    MATRIX_MULTIPLE("matrix_multiple", "矩阵多选"),
    RATING("rating", "评分/李克特量表"),
    SHORT_TEXT("short_text", "简答题"),
    LONG_TEXT("long_text", "长文本"),
    FILL_BLANK("fill_blank", "填空题"),
    SORT("sort", "排序题"),
    SLIDER("slider", "滑块题"),
    /** 收集性别/年龄/地区等人口学信息，通常不计入总分 */
    DEMOGRAPHIC("demographic", "人口学信息题"),
    /** 纯说明文字，不需要作答 */
    STATEMENT("statement", "说明性文字");

    private final String code;
    private final String label;
}
