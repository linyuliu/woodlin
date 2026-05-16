-- Migration: Add dynamic routing fields to sys_permission
-- Adds: show_in_tabs, active_menu, redirect
-- Date: 2026-05-05
-- Author: yulin

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_permission'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_permission' AND COLUMN_NAME = 'show_in_tabs'
        ),
        'ALTER TABLE `sys_permission` ADD COLUMN `show_in_tabs` char(1) DEFAULT ''1'' COMMENT ''是否在标签页显示（1-显示，0-隐藏）'' AFTER `visible`',
        'SELECT ''skip sys_permission.show_in_tabs'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_permission'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_permission' AND COLUMN_NAME = 'active_menu'
        ),
        'ALTER TABLE `sys_permission` ADD COLUMN `active_menu` varchar(255) DEFAULT NULL COMMENT ''高亮菜单路径（详情页等场景下高亮父菜单，如 /system/user）'' AFTER `show_in_tabs`',
        'SELECT ''skip sys_permission.active_menu'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_permission'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_permission' AND COLUMN_NAME = 'redirect'
        ),
        'ALTER TABLE `sys_permission` ADD COLUMN `redirect` varchar(255) DEFAULT NULL COMMENT ''重定向路径（目录类型菜单可配置）'' AFTER `active_menu`',
        'SELECT ''skip sys_permission.redirect'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
