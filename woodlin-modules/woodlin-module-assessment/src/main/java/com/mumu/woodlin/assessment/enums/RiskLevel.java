package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 风险/作弊等级
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum RiskLevel {

    NONE("none", "无风险"),
    LOW("low", "低风险"),
    MEDIUM("medium", "中等风险"),
    HIGH("high", "高风险"),
    /** 已经人工核查确认为作弊 */
    CONFIRMED("confirmed", "已确认作弊");

    private final String code;
    private final String label;
}
