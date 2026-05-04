-- Migration: Add dynamic routing fields to sys_permission
-- Adds: show_in_tabs, active_menu, redirect
-- Date: 2026-05-05
-- Author: yulin

ALTER TABLE sys_permission
    ADD COLUMN show_in_tabs char(1) DEFAULT '1',
    ADD COLUMN active_menu varchar(255) DEFAULT NULL,
    ADD COLUMN redirect varchar(255) DEFAULT NULL;

COMMENT ON COLUMN sys_permission.show_in_tabs IS '是否在标签页显示（1-显示，0-隐藏）';
COMMENT ON COLUMN sys_permission.active_menu IS '高亮菜单路径';
COMMENT ON COLUMN sys_permission.redirect IS '重定向路径';
