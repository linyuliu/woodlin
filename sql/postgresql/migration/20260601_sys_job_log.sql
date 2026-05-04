-- =============================================================
-- Woodlin PostgreSQL Migration
-- Name: 20260601_sys_job_log
-- Desc: 创建定时任务调度日志表 sys_job_log
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================

DROP TABLE IF EXISTS sys_job_log;
CREATE TABLE sys_job_log
(
    log_id        bigint        NOT NULL,
    job_name      varchar(64)   DEFAULT NULL,
    job_group     varchar(64)   DEFAULT NULL,
    invoke_target varchar(500)  DEFAULT NULL,
    status        char(1)       DEFAULT '0',
    message       text,
    start_time    timestamp     DEFAULT NULL,
    stop_time     timestamp     DEFAULT NULL,
    elapsed_time  bigint        DEFAULT NULL,
    tenant_id     varchar(64)   DEFAULT NULL,
    create_time   timestamp     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id)
);

CREATE INDEX idx_sys_job_log_name ON sys_job_log (job_name);
CREATE INDEX idx_sys_job_log_group ON sys_job_log (job_group);
CREATE INDEX idx_sys_job_log_status ON sys_job_log (status);
CREATE INDEX idx_sys_job_log_start_time ON sys_job_log (start_time);

COMMENT ON TABLE sys_job_log IS '定时任务调度日志表';
COMMENT ON COLUMN sys_job_log.log_id IS '日志ID';
COMMENT ON COLUMN sys_job_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_job_log.job_group IS '任务组名';
COMMENT ON COLUMN sys_job_log.invoke_target IS '调用目标字符串';
COMMENT ON COLUMN sys_job_log.status IS '执行状态（0-成功，1-失败）';
COMMENT ON COLUMN sys_job_log.message IS '执行消息或错误信息';
COMMENT ON COLUMN sys_job_log.start_time IS '开始时间';
COMMENT ON COLUMN sys_job_log.stop_time IS '结束时间';
COMMENT ON COLUMN sys_job_log.elapsed_time IS '耗时（毫秒）';
COMMENT ON COLUMN sys_job_log.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_job_log.create_time IS '创建时间';
