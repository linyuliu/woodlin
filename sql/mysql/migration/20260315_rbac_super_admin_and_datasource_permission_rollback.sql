-- =============================================================
-- Woodlin MySQL Rollback
-- Name: 20260315_rbac_super_admin_and_datasource_permission_rollback
-- Desc: 回滚 20260315_rbac_super_admin_and_datasource_permission 迁移
-- Note: 依赖备份表 bak_20260315_*
-- Author: codex
-- Date: 2026-03-15
-- =============================================================

USE `woodlin`;

START TRANSACTION;

-- 1) 回滚超级管理员角色（role_id=1）
DELETE FROM `sys_role` WHERE `role_id` = 1;
INSERT INTO `sys_role`
SELECT * FROM `bak_20260315_sys_role` WHERE `role_id` = 1;

-- 2) 回滚权限点（2101~2105）
DELETE FROM `sys_permission` WHERE `permission_id` IN (2101, 2102, 2103, 2104, 2105);
INSERT INTO `sys_permission`
SELECT * FROM `bak_20260315_sys_permission`
WHERE `permission_id` IN (2101, 2102, 2103, 2104, 2105);

-- 3) 回滚角色权限关系（role_id=1 + 2101~2105）
DELETE FROM `sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2101, 2102, 2103, 2104, 2105);
INSERT INTO `sys_role_permission`
SELECT * FROM `bak_20260315_sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2101, 2102, 2103, 2104, 2105);

-- 4) 回滚超级管理员继承权限缓存
DELETE FROM `sys_role_inherited_permission` WHERE `role_id` = 1;
INSERT INTO `sys_role_inherited_permission`
SELECT * FROM `bak_20260315_sys_role_inherited_permission`
WHERE `role_id` = 1;

COMMIT;

-- 建议校验
-- SELECT role_id, role_code, role_name, status FROM sys_role WHERE role_id = 1;
-- SELECT permission_id, permission_code FROM sys_permission WHERE permission_id BETWEEN 2101 AND 2105 ORDER BY permission_id;
-- SELECT COUNT(*) FROM sys_role_permission WHERE role_id = 1;
