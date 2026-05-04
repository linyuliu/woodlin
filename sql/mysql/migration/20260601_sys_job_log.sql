-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260601_sys_job_log
-- Desc: 创建定时任务调度日志表 sys_job_log
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================
USE `woodlin`;

DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`
(
    `log_id`        bigint(20)    NOT NULL COMMENT '日志ID',
    `job_name`      varchar(64)   DEFAULT NULL COMMENT '任务名称',
    `job_group`     varchar(64)   DEFAULT NULL COMMENT '任务组名',
    `invoke_target` varchar(500)  DEFAULT NULL COMMENT '调用目标字符串',
    `status`        char(1)       DEFAULT '0' COMMENT '执行状态（0-成功，1-失败）',
    `message`       text          COMMENT '执行消息或错误信息',
    `start_time`    datetime      DEFAULT NULL COMMENT '开始时间',
    `stop_time`     datetime      DEFAULT NULL COMMENT '结束时间',
    `elapsed_time`  bigint(20)    DEFAULT NULL COMMENT '耗时（毫秒）',
    `tenant_id`     varchar(64)   DEFAULT NULL COMMENT '租户ID',
    `create_time`   datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_job_log_name` (`job_name`),
    KEY `idx_job_log_group` (`job_group`),
    KEY `idx_job_log_status` (`status`),
    KEY `idx_job_log_start_time` (`start_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='定时任务调度日志表';
