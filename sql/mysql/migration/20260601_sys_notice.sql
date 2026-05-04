-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260601_sys_notice
-- Desc: 创建通知公告表 sys_notice
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================
USE `woodlin`;

DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`
(
    `notice_id`      bigint(20)    NOT NULL COMMENT '公告ID',
    `notice_title`   varchar(50)   NOT NULL COMMENT '公告标题',
    `notice_type`    char(1)       DEFAULT NULL COMMENT '公告类型（1-通知，2-公告）',
    `notice_content` longtext      DEFAULT NULL COMMENT '公告内容',
    `status`         char(1)       DEFAULT '0' COMMENT '公告状态（0-正常，1-关闭）',
    `remark`         varchar(255)  DEFAULT NULL COMMENT '备注',
    `create_by`      varchar(64)   DEFAULT NULL COMMENT '创建者',
    `create_time`    datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      varchar(64)   DEFAULT NULL COMMENT '更新者',
    `update_time`    datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`notice_id`),
    KEY `idx_notice_type` (`notice_type`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='通知公告表';
