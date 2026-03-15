-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260315_rbac_super_admin_and_datasource_permission
-- Desc: 超级管理员全权限收敛 + 数据源按钮权限补齐（带备份）
-- Author: codex
-- Date: 2026-03-15
-- =============================================================

USE `woodlin`;

-- =========================
-- 1) 备份受影响数据
-- =========================
CREATE TABLE IF NOT EXISTS `bak_20260315_sys_role` LIKE `sys_role`;
CREATE TABLE IF NOT EXISTS `bak_20260315_sys_permission` LIKE `sys_permission`;
CREATE TABLE IF NOT EXISTS `bak_20260315_sys_role_permission` LIKE `sys_role_permission`;
CREATE TABLE IF NOT EXISTS `bak_20260315_sys_role_inherited_permission` LIKE `sys_role_inherited_permission`;

-- 备份超级管理员角色信息（role_id=1）
DELETE FROM `bak_20260315_sys_role` WHERE `role_id` = 1;
INSERT INTO `bak_20260315_sys_role`
SELECT * FROM `sys_role` WHERE `role_id` = 1;

-- 备份将变更的数据源权限点
DELETE FROM `bak_20260315_sys_permission`
WHERE `permission_id` IN (2101, 2102, 2103, 2104, 2105);
INSERT INTO `bak_20260315_sys_permission`
SELECT * FROM `sys_permission`
WHERE `permission_id` IN (2101, 2102, 2103, 2104, 2105);

-- 备份受影响角色权限关系（role_id=1 以及新权限ID相关）
DELETE FROM `bak_20260315_sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2101, 2102, 2103, 2104, 2105);
INSERT INTO `bak_20260315_sys_role_permission`
SELECT * FROM `sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2101, 2102, 2103, 2104, 2105);

-- 备份超级管理员继承权限缓存
DELETE FROM `bak_20260315_sys_role_inherited_permission`
WHERE `role_id` = 1;
INSERT INTO `bak_20260315_sys_role_inherited_permission`
SELECT * FROM `sys_role_inherited_permission`
WHERE `role_id` = 1;

-- =========================
-- 2) 执行迁移
-- =========================
START TRANSACTION;

-- 2.1 超级管理员角色码统一为 admin
UPDATE `sys_role`
SET `role_code` = 'admin',
    `status` = '1',
    `update_time` = NOW()
WHERE `role_id` = 1;

-- 2.2 补齐数据源按钮/API权限点（仅最小能力）
INSERT INTO `sys_permission` (
    `permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`,
    `path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`,
    `visible`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`
) VALUES
    (2101, 2000, '新增数据源', 'datasource:add',      'F', '', NULL, '#', 11, '1', '0', '0', '1', '数据源新增按钮权限',   'admin', NOW(), 'admin', NOW(), '0'),
    (2102, 2000, '编辑数据源', 'datasource:edit',     'F', '', NULL, '#', 12, '1', '0', '0', '1', '数据源编辑按钮权限',   'admin', NOW(), 'admin', NOW(), '0'),
    (2103, 2000, '删除数据源', 'datasource:remove',   'F', '', NULL, '#', 13, '1', '0', '0', '1', '数据源删除按钮权限',   'admin', NOW(), 'admin', NOW(), '0'),
    (2104, 2000, '测试连接',   'datasource:test',     'F', '', NULL, '#', 14, '1', '0', '0', '1', '数据源测试连接权限',   'admin', NOW(), 'admin', NOW(), '0'),
    (2105, 2000, '读取元数据', 'datasource:metadata', 'F', '', NULL, '#', 15, '1', '0', '0', '1', '数据源读取元数据权限', 'admin', NOW(), 'admin', NOW(), '0')
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
    `update_by` = 'admin',
    `update_time` = NOW(),
    `deleted` = '0';

-- 2.3 超级管理员补齐全量权限（当前全部 sys_permission）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.`permission_id`
FROM `sys_permission` p
WHERE p.`deleted` = '0'
ON DUPLICATE KEY UPDATE
    `permission_id` = VALUES(`permission_id`);

-- 2.4 同步超级管理员继承权限缓存
DELETE FROM `sys_role_inherited_permission` WHERE `role_id` = 1;
INSERT INTO `sys_role_inherited_permission`
    (`role_id`, `permission_id`, `is_inherited`, `inherited_from`, `tenant_id`, `update_time`)
SELECT
    1,
    rp.`permission_id`,
    '0',
    NULL,
    NULL,
    NOW()
FROM `sys_role_permission` rp
WHERE rp.`role_id` = 1;

COMMIT;

-- =========================
-- 3) 校验查询（执行后手工检查）
-- =========================
-- SELECT role_id, role_code, role_name, status FROM sys_role WHERE role_id = 1;
-- SELECT permission_id, permission_code FROM sys_permission WHERE permission_id BETWEEN 2101 AND 2105 ORDER BY permission_id;
-- SELECT COUNT(*) AS role1_permission_count FROM sys_role_permission WHERE role_id = 1;
-- SELECT COUNT(*) AS all_permission_count FROM sys_permission WHERE deleted = '0';
