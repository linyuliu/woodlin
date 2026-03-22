-- =============================================================
-- Woodlin MySQL Migration
-- Name: 20260318_tenant_permission_and_datasource_cleanup
-- Desc: 租户按钮权限补齐 + 清理数据源 metadata 独立权限
-- Author: codex
-- Date: 2026-03-18
-- =============================================================

USE `woodlin`;

-- =========================
-- 1) 备份受影响数据
-- =========================
CREATE TABLE IF NOT EXISTS `bak_20260318_sys_permission` LIKE `sys_permission`;
CREATE TABLE IF NOT EXISTS `bak_20260318_sys_role_permission` LIKE `sys_role_permission`;
CREATE TABLE IF NOT EXISTS `bak_20260318_sys_role_inherited_permission` LIKE `sys_role_inherited_permission`;

DELETE FROM `bak_20260318_sys_permission`
WHERE `permission_id` IN (2105, 3101, 3102, 3103);
INSERT INTO `bak_20260318_sys_permission`
SELECT *
FROM `sys_permission`
WHERE `permission_id` IN (2105, 3101, 3102, 3103);

DELETE FROM `bak_20260318_sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);
INSERT INTO `bak_20260318_sys_role_permission`
SELECT *
FROM `sys_role_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);

DELETE FROM `bak_20260318_sys_role_inherited_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);
INSERT INTO `bak_20260318_sys_role_inherited_permission`
SELECT *
FROM `sys_role_inherited_permission`
WHERE `role_id` = 1 OR `permission_id` IN (2105, 3101, 3102, 3103);

-- =========================
-- 2) 执行迁移
-- =========================
START TRANSACTION;

-- 2.1 删除多余的数据源 metadata 独立权限
DELETE FROM `sys_role_inherited_permission` WHERE `permission_id` = 2105;
DELETE FROM `sys_role_permission` WHERE `permission_id` = 2105;
DELETE FROM `sys_permission` WHERE `permission_id` = 2105;

-- 2.2 补齐租户按钮权限
INSERT INTO `sys_permission` (
    `permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`,
    `path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`,
    `visible`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`
) VALUES
    (3101, 3001, '租户新增', 'tenant:add',    'F', '', NULL, '#', 1, '1', '0', '0', '1', '新增租户按钮', 'admin', NOW(), 'admin', NOW(), '0'),
    (3102, 3001, '租户修改', 'tenant:edit',   'F', '', NULL, '#', 2, '1', '0', '0', '1', '修改租户按钮', 'admin', NOW(), 'admin', NOW(), '0'),
    (3103, 3001, '租户删除', 'tenant:remove', 'F', '', NULL, '#', 3, '1', '0', '0', '1', '删除租户按钮', 'admin', NOW(), 'admin', NOW(), '0')
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

-- 2.3 超级管理员同步拥有当前全部权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.`permission_id`
FROM `sys_permission` p
WHERE p.`deleted` = '0'
ON DUPLICATE KEY UPDATE
    `permission_id` = VALUES(`permission_id`);

-- 2.4 刷新超级管理员继承权限缓存
DELETE FROM `sys_role_inherited_permission` WHERE `role_id` = 1;
INSERT INTO `sys_role_inherited_permission`
    (`role_id`, `permission_id`, `is_inherited`, `inherited_from`, `tenant_id`, `update_time`)
SELECT
    rp.`role_id`,
    rp.`permission_id`,
    '0',
    NULL,
    r.`tenant_id`,
    NOW()
FROM `sys_role_permission` rp
INNER JOIN `sys_role` r ON rp.`role_id` = r.`role_id`
WHERE rp.`role_id` = 1;

COMMIT;

-- =========================
-- 3) 校验查询（执行后手工检查）
-- =========================
-- SELECT permission_id, permission_code FROM sys_permission WHERE permission_id IN (2105, 3101, 3102, 3103);
-- SELECT permission_id FROM sys_role_permission WHERE role_id = 1 AND permission_id IN (3101, 3102, 3103) ORDER BY permission_id;
