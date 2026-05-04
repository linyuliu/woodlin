-- Migration: Add dynamic routing fields to sys_permission
-- Adds: show_in_tabs, active_menu, redirect
-- Date: 2026-05-05
-- Author: yulin

ALTER TABLE `sys_permission`
    ADD COLUMN `show_in_tabs` char(1) DEFAULT '1'
        COMMENT '是否在标签页显示（1-显示，0-隐藏）' AFTER `visible`,
    ADD COLUMN `active_menu` varchar(255) DEFAULT NULL
        COMMENT '高亮菜单路径（详情页等场景下高亮父菜单，如 /system/user）' AFTER `show_in_tabs`,
    ADD COLUMN `redirect` varchar(255) DEFAULT NULL
        COMMENT '重定向路径（目录类型菜单可配置）' AFTER `active_menu`;
