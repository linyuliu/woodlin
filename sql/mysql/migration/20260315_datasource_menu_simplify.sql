-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260315_datasource_menu_simplify
-- Desc: 数据源菜单收敛（下线监控菜单，按钮挂载到列表页）
-- Author: codex
-- Date: 2026-03-15
-- =============================================================

USE `woodlin`;

-- =========================
-- 1) 备份受影响数据
-- =========================
DROP TABLE IF EXISTS `bak_20260315_ds_simplify_sys_permission`;
CREATE TABLE `bak_20260315_ds_simplify_sys_permission` LIKE `sys_permission`;
DROP TABLE IF EXISTS `bak_20260315_ds_simplify_sys_role_permission`;
CREATE TABLE `bak_20260315_ds_simplify_sys_role_permission` LIKE `sys_role_permission`;
DROP TABLE IF EXISTS `bak_20260315_ds_simplify_sys_role_inherited_permission`;
CREATE TABLE `bak_20260315_ds_simplify_sys_role_inherited_permission` LIKE `sys_role_inherited_permission`;

DELETE FROM `bak_20260315_ds_simplify_sys_permission`
WHERE `permission_id` IN (2002, 2101, 2102, 2103, 2104, 2105);
INSERT INTO `bak_20260315_ds_simplify_sys_permission`
SELECT *
FROM `sys_permission`
WHERE `permission_id` IN (2002, 2101, 2102, 2103, 2104, 2105);

DELETE FROM `bak_20260315_ds_simplify_sys_role_permission`
WHERE `permission_id` = 2002;
INSERT INTO `bak_20260315_ds_simplify_sys_role_permission`
SELECT *
FROM `sys_role_permission`
WHERE `permission_id` = 2002;

DELETE FROM `bak_20260315_ds_simplify_sys_role_inherited_permission`
WHERE `permission_id` = 2002;
INSERT INTO `bak_20260315_ds_simplify_sys_role_inherited_permission`
SELECT *
FROM `sys_role_inherited_permission`
WHERE `permission_id` = 2002;

-- =========================
-- 2) 执行迁移
-- =========================
START TRANSACTION;

-- 2.1 下线数据源监控菜单（保留记录，逻辑删除）
UPDATE `sys_permission`
SET `status` = '0',
    `visible` = '0',
    `deleted` = '1',
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `permission_id` = 2002;

-- 2.2 数据源按钮统一挂在“数据源列表”菜单下
UPDATE `sys_permission`
SET `parent_id` = 2001,
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `permission_id` IN (2101, 2102, 2103, 2104, 2105)
  AND `parent_id` <> 2001;

-- 2.3 清理监控菜单的角色授权与继承缓存
DELETE FROM `sys_role_permission` WHERE `permission_id` = 2002;
DELETE FROM `sys_role_inherited_permission` WHERE `permission_id` = 2002;

COMMIT;

-- =========================
-- 3) 校验查询（执行后手工检查）
-- =========================
-- SELECT permission_id, permission_code, parent_id, status, visible, deleted
-- FROM sys_permission
-- WHERE permission_id IN (2002, 2101, 2102, 2103, 2104, 2105)
-- ORDER BY permission_id;
--
-- SELECT COUNT(*) AS role_permission_monitor
-- FROM sys_role_permission
-- WHERE permission_id = 2002;
--
-- SELECT COUNT(*) AS inherited_permission_monitor
-- FROM sys_role_inherited_permission
-- WHERE permission_id = 2002;
