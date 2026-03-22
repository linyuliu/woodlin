-- =============================================================
-- Woodlin MySQL Migration Rollback
-- Name: 20260318_remove_unused_role_export_permission_rollback
-- Desc: 回滚角色导出权限清理
-- Author: codex
-- Date: 2026-03-18
-- =============================================================

USE `woodlin`;

START TRANSACTION;

INSERT INTO `sys_permission`
SELECT *
FROM `bak_20260318_role_export_sys_permission`
WHERE `permission_id` = 204
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

INSERT INTO `sys_role_permission`
SELECT *
FROM `bak_20260318_role_export_sys_role_permission`
WHERE `permission_id` = 204
ON DUPLICATE KEY UPDATE
    `permission_id` = VALUES(`permission_id`);

INSERT INTO `sys_role_inherited_permission`
SELECT *
FROM `bak_20260318_role_export_sys_role_inherited_permission`
WHERE `permission_id` = 204
ON DUPLICATE KEY UPDATE
    `is_inherited` = VALUES(`is_inherited`),
    `inherited_from` = VALUES(`inherited_from`),
    `tenant_id` = VALUES(`tenant_id`),
    `update_time` = VALUES(`update_time`);

COMMIT;
