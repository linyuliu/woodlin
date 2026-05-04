-- =============================================================
-- Woodlin PostgreSQL Migration
-- Name: 20260601_sys_notice
-- Desc: 创建通知公告表 sys_notice
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================

DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice
(
    notice_id      bigint        NOT NULL,
    notice_title   varchar(50)   NOT NULL,
    notice_type    char(1)       DEFAULT NULL,
    notice_content text          DEFAULT NULL,
    status         char(1)       DEFAULT '0',
    remark         varchar(255)  DEFAULT NULL,
    create_by      varchar(64)   DEFAULT NULL,
    create_time    timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by      varchar(64)   DEFAULT NULL,
    update_time    timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted        char(1)       DEFAULT '0',
    PRIMARY KEY (notice_id)
);

CREATE INDEX idx_sys_notice_type ON sys_notice (notice_type);
CREATE INDEX idx_sys_notice_status ON sys_notice (status);

COMMENT ON TABLE sys_notice IS '通知公告表';
COMMENT ON COLUMN sys_notice.notice_id IS '公告ID';
COMMENT ON COLUMN sys_notice.notice_title IS '公告标题';
COMMENT ON COLUMN sys_notice.notice_type IS '公告类型（1-通知，2-公告）';
COMMENT ON COLUMN sys_notice.notice_content IS '公告内容';
COMMENT ON COLUMN sys_notice.status IS '公告状态（0-正常，1-关闭）';
COMMENT ON COLUMN sys_notice.remark IS '备注';
