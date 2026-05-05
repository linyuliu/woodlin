-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260321_etl_offline_menu_refresh
-- Desc: ETL 离线菜单与组件路径对齐新前端向导
-- Author: codex
-- Date: 2026-03-21
-- =============================================================

USE `woodlin`;

DROP TABLE IF EXISTS `bak_20260321_etl_menu_sys_permission`;
CREATE TABLE `bak_20260321_etl_menu_sys_permission` LIKE `sys_permission`;

DELETE FROM `bak_20260321_etl_menu_sys_permission`
WHERE `permission_id` IN (7000, 7001, 7002);
INSERT INTO `bak_20260321_etl_menu_sys_permission`
SELECT *
FROM `sys_permission`
WHERE `permission_id` IN (7000, 7001, 7002);

START TRANSACTION;

UPDATE `sys_permission`
SET `permission_name` = '同步任务',
    `icon` = 'git-compare-outline',
    `remark` = 'ETL同步任务目录',
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `permission_id` = 7000;

UPDATE `sys_permission`
SET `permission_name` = '离线同步',
    `permission_code` = 'etl:offline',
    `path` = 'offline',
    `component` = 'etl/offline',
    `icon` = 'swap-horizontal-outline',
    `remark` = '离线同步页面',
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `permission_id` = 7001;

UPDATE `sys_permission`
SET `path` = 'offline/logs',
    `component` = 'etl/offline/logs',
    `remark` = '离线同步执行历史页面',
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `permission_id` = 7002;

COMMIT;

-- SELECT permission_id, permission_name, permission_code, path, component, icon
-- FROM sys_permission
-- WHERE permission_id IN (7000, 7001, 7002)
-- ORDER BY permission_id;
