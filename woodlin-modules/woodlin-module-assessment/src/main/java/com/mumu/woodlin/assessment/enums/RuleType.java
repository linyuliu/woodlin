package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 规则类型
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum RuleType {

    /** 显示/隐藏逻辑：根据已答条目动态显示/隐藏题目或章节 */
    DISPLAY("display", "显示逻辑"),
    /** 跳转逻辑：根据条件跳转到指定题目或章节 */
    BRANCH("branch", "跳转逻辑"),
    /** 作答校验：对作答内容做约束校验 */
    VALIDATION("validation", "作答校验"),
    /** 计分规则：维度原始分/加权分的计算逻辑 */
    SCORE("score", "计分规则"),
    /** 常模匹配：根据人口学信息选取常模集 */
    NORM_MATCH("norm_match", "常模匹配"),
    /** 报告生成：映射分数区间到文字解读/标签 */
    REPORT("report", "报告生成"),
    /** 资格条件：判断受试者是否满足参加本测评的资格 */
    ELIGIBILITY("eligibility", "资格条件"),
    /** 终止逻辑：满足条件时提前结束作答 */
    TERMINATE("terminate", "终止逻辑");

    private final String code;
    private final String label;
}
