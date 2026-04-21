package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 章节/页面显示模式
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum SectionDisplayMode {

    /** 每章节独立一页，需点击"下一步"翻页 */
    PAGED("paged", "分页模式"),
    /** 所有题目连续滚动显示 */
    CONTINUOUS("continuous", "连续滚动");

    private final String code;
    private final String label;
}
