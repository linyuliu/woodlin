package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 测评类型
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum AssessmentType {

    /** 心理量表（含维度、反向题、常模等） */
    SCALE("scale", "心理量表"),
    /** 试卷/考试（含难度、区分度、计时等） */
    EXAM("exam", "试卷/考试"),
    /** 问卷调查（以收集数据为主，计分可选） */
    SURVEY("survey", "问卷调查");

    private final String code;
    private final String label;
}
