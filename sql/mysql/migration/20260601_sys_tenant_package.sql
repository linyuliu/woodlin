-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260601_sys_tenant_package
-- Desc: 创建租户套餐表 sys_tenant_package
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================
USE `woodlin`;

DROP TABLE IF EXISTS `sys_tenant_package`;
CREATE TABLE `sys_tenant_package`
(
    `package_id`   bigint(20)    NOT NULL COMMENT '套餐ID',
    `package_name` varchar(100)  NOT NULL COMMENT '套餐名称',
    `menu_ids`     text          DEFAULT NULL COMMENT '关联菜单ID集合（逗号分隔）',
    `status`       char(1)       DEFAULT '1' COMMENT '套餐状态（1-启用，0-禁用）',
    `remark`       varchar(255)  DEFAULT NULL COMMENT '备注',
    `create_by`    varchar(64)   DEFAULT NULL COMMENT '创建者',
    `create_time`  datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)   DEFAULT NULL COMMENT '更新者',
    `update_time`  datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`package_id`),
    KEY `idx_package_name` (`package_name`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='租户套餐表';
