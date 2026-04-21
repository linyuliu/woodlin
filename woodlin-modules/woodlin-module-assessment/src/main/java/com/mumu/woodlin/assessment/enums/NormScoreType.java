package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 常模分数类型
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum NormScoreType {

    /** T分：均值50，标准差10 */
    T_SCORE("t_score", "T分"),
    /** Z分：均值0，标准差1 */
    Z_SCORE("z_score", "Z分"),
    PERCENTILE("percentile", "百分位"),
    /** 斯坦九分：1-9 */
    STANINE("stanine", "斯坦九分"),
    /** 斯坦分：1-10 */
    STEN("sten", "斯坦分"),
    GRADE_EQUIVALENT("grade_equivalent", "年级等值分"),
    /** 直接按原始分划等级 */
    RAW_GRADE("raw_grade", "原始等级");

    private final String code;
    private final String label;
}
