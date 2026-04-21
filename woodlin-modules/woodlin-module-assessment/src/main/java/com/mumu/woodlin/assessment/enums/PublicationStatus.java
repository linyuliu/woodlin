package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 发布实例状态
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum PublicationStatus {

    DRAFT("draft", "草稿"),
    UNDER_REVIEW("under_review", "审核中"),
    PUBLISHED("published", "已发布/进行中"),
    PAUSED("paused", "已暂停"),
    CLOSED("closed", "已关闭"),
    ARCHIVED("archived", "已归档");

    private final String code;
    private final String label;
}
