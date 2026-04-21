package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 随机化策略
 *
 * <p>控制在作答运行时题目和选项是否乱序展示。
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum RandomStrategy {

    /** 不随机，按原始顺序展示 */
    NONE("none", "不随机"),
    /** 随机题目顺序（选项顺序不变） */
    RANDOM_ITEMS("random_items", "随机题目顺序"),
    /** 随机选项顺序（题目顺序不变） */
    RANDOM_OPTIONS("random_options", "随机选项顺序"),
    /** 同时随机题目和选项顺序 */
    RANDOM_BOTH("random_both", "随机题目和选项");

    private final String code;
    private final String label;
}
