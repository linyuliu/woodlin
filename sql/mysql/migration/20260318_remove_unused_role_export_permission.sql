-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260318_remove_unused_role_export_permission
-- Desc: 清理无实际接口/按钮支撑的角色导出权限
-- Author: codex
-- Date: 2026-03-18
-- =============================================================

USE `woodlin`;

-- =========================
-- 1) 备份受影响数据
-- =========================
CREATE TABLE IF NOT EXISTS `bak_20260318_role_export_sys_permission` LIKE `sys_permission`;
CREATE TABLE IF NOT EXISTS `bak_20260318_role_export_sys_role_permission` LIKE `sys_role_permission`;
CREATE TABLE IF NOT EXISTS `bak_20260318_role_export_sys_role_inherited_permission` LIKE `sys_role_inherited_permission`;

DELETE FROM `bak_20260318_role_export_sys_permission`
WHERE `permission_id` = 204;
INSERT INTO `bak_20260318_role_export_sys_permission`
SELECT *
FROM `sys_permission`
WHERE `permission_id` = 204;

DELETE FROM `bak_20260318_role_export_sys_role_permission`
WHERE `permission_id` = 204;
INSERT INTO `bak_20260318_role_export_sys_role_permission`
SELECT *
FROM `sys_role_permission`
WHERE `permission_id` = 204;

DELETE FROM `bak_20260318_role_export_sys_role_inherited_permission`
WHERE `permission_id` = 204;
INSERT INTO `bak_20260318_role_export_sys_role_inherited_permission`
SELECT *
FROM `sys_role_inherited_permission`
WHERE `permission_id` = 204;

-- =========================
-- 2) 执行迁移
-- =========================
START TRANSACTION;

DELETE FROM `sys_role_inherited_permission` WHERE `permission_id` = 204;
DELETE FROM `sys_role_permission` WHERE `permission_id` = 204;
DELETE FROM `sys_permission` WHERE `permission_id` = 204;

COMMIT;

-- =========================
-- 3) 校验查询（执行后手工检查）
-- =========================
-- SELECT permission_id, permission_code FROM sys_permission WHERE permission_id = 204;
-- SELECT role_id, permission_id FROM sys_role_permission WHERE permission_id = 204;
-- SELECT role_id, permission_id FROM sys_role_inherited_permission WHERE permission_id = 204;
