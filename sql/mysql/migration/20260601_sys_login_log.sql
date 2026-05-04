-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260601_sys_login_log
-- Desc: 创建登录日志表 sys_login_log
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================
USE `woodlin`;

DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`
(
    `login_id`       bigint(20)    NOT NULL COMMENT '日志主键',
    `username`       varchar(50)   DEFAULT NULL COMMENT '用户名',
    `ipaddr`         varchar(128)  DEFAULT NULL COMMENT '登录IP地址',
    `login_location` varchar(255)  DEFAULT NULL COMMENT '登录地点',
    `browser`        varchar(64)   DEFAULT NULL COMMENT '浏览器',
    `os`             varchar(64)   DEFAULT NULL COMMENT '操作系统',
    `msg`            varchar(255)  DEFAULT NULL COMMENT '提示消息',
    `status`         char(1)       DEFAULT '0' COMMENT '登录状态（0-成功，1-失败）',
    `login_time`     datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `tenant_id`      varchar(64)   DEFAULT NULL COMMENT '租户ID',
    `deleted`        char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`login_id`),
    KEY `idx_login_username` (`username`),
    KEY `idx_login_status` (`status`),
    KEY `idx_login_time` (`login_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='登录日志表';
