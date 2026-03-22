-- =============================================================
-- Woodlin MySQL Migration Rollback
-- Name: 20260318_tenant_permission_and_datasource_cleanup_rollback
-- Desc: 回滚租户按钮权限补齐和数据源 metadata 权限清理
-- Author: codex
-- Date: 2026-03-18
-- =============================================================

USE `woodlin`;

START TRANSACTION;

DELETE FROM `sys_role_inherited_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);

DELETE FROM `sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);

DELETE FROM `sys_permission`
WHERE `permission_id` IN (3101, 3102, 3103);

INSERT IGNORE INTO `sys_permission`
SELECT *
FROM `bak_20260318_sys_permission`
WHERE `permission_id` = 2105;

INSERT IGNORE INTO `sys_role_permission`
SELECT *
FROM `bak_20260318_sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);

INSERT IGNORE INTO `sys_role_inherited_permission`
SELECT *
FROM `bak_20260318_sys_role_inherited_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);

COMMIT;
