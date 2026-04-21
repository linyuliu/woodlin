package com.mumu.woodlin.assessment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 作答会话状态
 *
 * @author mumu
 * @since 2025-01-01
 */
@Getter
@RequiredArgsConstructor
public enum SessionStatus {

    NOT_STARTED("not_started", "未开始"),
    IN_PROGRESS("in_progress", "作答中"),
    PAUSED("paused", "已暂停/断点续答"),
    COMPLETED("completed", "已完成"),
    EXPIRED("expired", "已过期"),
    ABANDONED("abandoned", "已放弃"),
    /** 因风控或人工审核被强制作废 */
    INVALIDATED("invalidated", "已作废");

    private final String code;
    private final String label;
}
