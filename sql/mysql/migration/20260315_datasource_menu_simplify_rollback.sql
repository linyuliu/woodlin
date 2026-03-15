-- =============================================================
-- Woodlin MySQL Rollback
-- Name: 20260315_datasource_menu_simplify_rollback
-- Desc: 回滚数据源菜单收敛变更
-- Author: codex
-- Date: 2026-03-15
-- =============================================================

USE `woodlin`;

START TRANSACTION;

-- 1) 回滚 sys_permission 相关记录
DELETE FROM `sys_permission`
WHERE `permission_id` IN (2002, 2101, 2102, 2103, 2104, 2105);
INSERT INTO `sys_permission`
SELECT *
FROM `bak_20260315_ds_simplify_sys_permission`
WHERE `permission_id` IN (2002, 2101, 2102, 2103, 2104, 2105);

-- 2) 回滚 sys_role_permission（监控菜单授权）
DELETE FROM `sys_role_permission` WHERE `permission_id` = 2002;
INSERT INTO `sys_role_permission`
SELECT *
FROM `bak_20260315_ds_simplify_sys_role_permission`
WHERE `permission_id` = 2002;

-- 3) 回滚 sys_role_inherited_permission（监控菜单缓存）
DELETE FROM `sys_role_inherited_permission` WHERE `permission_id` = 2002;
INSERT INTO `sys_role_inherited_permission`
SELECT *
FROM `bak_20260315_ds_simplify_sys_role_inherited_permission`
WHERE `permission_id` = 2002;

COMMIT;
