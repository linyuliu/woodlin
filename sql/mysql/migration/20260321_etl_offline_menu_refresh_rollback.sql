-- =============================================================
-- Woodlin MySQL Rollback
-- Name: 20260321_etl_offline_menu_refresh_rollback
-- Desc: 回滚 ETL 离线菜单与组件路径调整
-- Author: codex
-- Date: 2026-03-21
-- =============================================================

USE `woodlin`;

START TRANSACTION;

DELETE FROM `sys_permission`
WHERE `permission_id` IN (7000, 7001, 7002);

INSERT INTO `sys_permission`
SELECT *
FROM `bak_20260321_etl_menu_sys_permission`
WHERE `permission_id` IN (7000, 7001, 7002)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `permission_name` = VALUES(`permission_name`),
    `permission_code` = VALUES(`permission_code`),
    `permission_type` = VALUES(`permission_type`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_frame` = VALUES(`is_frame`),
    `is_cache` = VALUES(`is_cache`),
    `visible` = VALUES(`visible`),
    `remark` = VALUES(`remark`),
    `create_by` = VALUES(`create_by`),
    `create_time` = VALUES(`create_time`),
    `update_by` = VALUES(`update_by`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

COMMIT;
