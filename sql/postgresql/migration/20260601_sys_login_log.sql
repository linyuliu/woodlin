-- =============================================================
-- Woodlin PostgreSQL Migration
-- Name: 20260601_sys_login_log
-- Desc: 创建登录日志表 sys_login_log
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================

DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log
(
    login_id       bigint        NOT NULL,
    username       varchar(50)   DEFAULT NULL,
    ipaddr         varchar(128)  DEFAULT NULL,
    login_location varchar(255)  DEFAULT NULL,
    browser        varchar(64)   DEFAULT NULL,
    os             varchar(64)   DEFAULT NULL,
    msg            varchar(255)  DEFAULT NULL,
    status         char(1)       DEFAULT '0',
    login_time     timestamp     DEFAULT CURRENT_TIMESTAMP,
    tenant_id      varchar(64)   DEFAULT NULL,
    deleted        char(1)       DEFAULT '0',
    PRIMARY KEY (login_id)
);

CREATE INDEX idx_sys_login_log_username ON sys_login_log (username);
CREATE INDEX idx_sys_login_log_status ON sys_login_log (status);
CREATE INDEX idx_sys_login_log_time ON sys_login_log (login_time);

COMMENT ON TABLE sys_login_log IS '登录日志表';
COMMENT ON COLUMN sys_login_log.login_id IS '日志主键';
COMMENT ON COLUMN sys_login_log.username IS '用户名';
COMMENT ON COLUMN sys_login_log.ipaddr IS '登录IP地址';
COMMENT ON COLUMN sys_login_log.login_location IS '登录地点';
COMMENT ON COLUMN sys_login_log.browser IS '浏览器';
COMMENT ON COLUMN sys_login_log.os IS '操作系统';
COMMENT ON COLUMN sys_login_log.msg IS '提示消息';
COMMENT ON COLUMN sys_login_log.status IS '登录状态（0-成功，1-失败）';
COMMENT ON COLUMN sys_login_log.login_time IS '登录时间';
COMMENT ON COLUMN sys_login_log.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_login_log.deleted IS '删除标识（0-正常，1-删除）';
