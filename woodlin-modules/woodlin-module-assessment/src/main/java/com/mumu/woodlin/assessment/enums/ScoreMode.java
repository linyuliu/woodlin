package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 维度计分模式
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum ScoreMode {

    SUM("sum", "求和"),
    MEAN("mean", "均值"),
    MAX("max", "最大值"),
    MIN("min", "最小值"),
    WEIGHTED_SUM("weighted_sum", "加权求和"),
    /** 使用 DSL 脚本自定义计分逻辑 */
    CUSTOM_DSL("custom_dsl", "自定义DSL");

    private final String code;
    private final String label;
}
