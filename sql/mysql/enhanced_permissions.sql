-- =============================================
-- Enhanced Permission/Menu Data for Woodlin System
-- This file adds comprehensive menu entries that match the frontend routes
-- Author: mumu
-- Date: 2025-12-23
-- =============================================

USE `woodlin`;

-- Delete existing permission data (be careful in production!)
DELETE FROM `sys_role_inherited_permission`;
DELETE FROM `sys_role_permission`;
DELETE FROM `sys_permission`;

-- =============================================
-- Complete Permission/Menu Structure
-- =============================================

INSERT INTO `sys_permission` VALUES 
-- ===== Level 0: Dashboard (Root Level) =====
(1, 0, '仪表板', 'dashboard:view', 'C', 'dashboard', 'DashboardView', 'dashboard-outline', 0, '1', '0', '0', '1', '仪表板页面', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Level 1: System Management =====
(10, 0, '系统管理', 'system:view', 'M', 'system', NULL, 'settings-outline', 1, '1', '0', '0', '1', '系统管理目录', 'system', NOW(), 'system', NOW(), '0'),

-- Level 2: System sub-menus
(11, 10, '用户管理', 'system:user:view', 'C', 'user', 'system/UserView', 'people-outline', 1, '1', '0', '0', '1', '用户管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(12, 10, '角色管理', 'system:role:view', 'C', 'role', 'system/RoleView', 'shield-outline', 2, '1', '0', '0', '1', '角色管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(13, 10, '部门管理', 'system:dept:view', 'C', 'dept', 'system/DeptView', 'business-outline', 3, '1', '0', '0', '1', '部门管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(14, 10, '权限管理', 'system:permission:view', 'C', 'permission', 'system/permission/PermissionManagementView', 'key-outline', 4, '1', '0', '0', '1', '权限管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(15, 10, '字典管理', 'system:dict:view', 'C', 'dict', 'system/DictView', 'book-outline', 5, '1', '0', '0', '1', '字典管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(16, 10, '配置管理', 'system:config:view', 'C', 'config', 'system/ConfigView', 'options-outline', 6, '1', '0', '0', '1', '配置管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(17, 10, '系统设置', 'system:settings:view', 'C', 'settings', 'system/SystemSettingsView', 'cog-outline', 7, '1', '0', '0', '1', '系统设置菜单', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Level 1: Datasource Management =====
(20, 0, '数据源管理', 'datasource:view', 'M', 'datasource', NULL, 'server-outline', 2, '1', '0', '0', '1', '数据源管理目录', 'system', NOW(), 'system', NOW(), '0'),

-- Level 2: Datasource sub-menus
(21, 20, '数据源列表', 'datasource:list:view', 'C', 'list', 'datasource/DatasourceList', 'list-outline', 1, '1', '0', '0', '1', '数据源列表菜单', 'system', NOW(), 'system', NOW(), '0'),
(22, 20, '数据源监控', 'datasource:monitor:view', 'C', 'monitor', 'datasource/DatasourceMonitor', 'stats-chart-outline', 2, '1', '0', '0', '1', '数据源监控菜单', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Level 1: Tenant Management =====
(30, 0, '租户管理', 'tenant:view', 'M', 'tenant', NULL, 'home-outline', 3, '1', '0', '0', '1', '租户管理目录', 'system', NOW(), 'system', NOW(), '0'),

-- Level 2: Tenant sub-menus
(31, 30, '租户列表', 'tenant:list:view', 'C', 'list', 'tenant/TenantView', 'list-outline', 1, '1', '0', '0', '1', '租户列表菜单', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Level 1: File Management =====
(40, 0, '文件管理', 'file:view', 'M', 'file', NULL, 'folder-outline', 4, '1', '0', '0', '1', '文件管理目录', 'system', NOW(), 'system', NOW(), '0'),

-- Level 2: File sub-menus
(41, 40, '文件列表', 'file:list:view', 'C', 'list', 'file/FileList', 'documents-outline', 1, '1', '0', '0', '1', '文件列表菜单', 'system', NOW(), 'system', NOW(), '0'),
(42, 40, '存储配置', 'file:storage:view', 'C', 'storage', 'file/FileStorage', 'archive-outline', 2, '1', '0', '0', '1', '存储配置菜单', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Level 1: Task Management =====
(50, 0, '任务管理', 'task:view', 'M', 'task', NULL, 'timer-outline', 5, '1', '0', '0', '1', '任务管理目录', 'system', NOW(), 'system', NOW(), '0'),

-- Level 2: Task sub-menus
(51, 50, '任务列表', 'task:list:view', 'C', 'list', 'task/TaskList', 'list-outline', 1, '1', '0', '0', '1', '任务列表菜单', 'system', NOW(), 'system', NOW(), '0'),
(52, 50, '任务日志', 'task:log:view', 'C', 'log', 'task/TaskLog', 'receipt-outline', 2, '1', '0', '0', '1', '任务日志菜单', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Level 1: Development Tools =====
(60, 0, '开发工具', 'dev:view', 'M', 'dev', NULL, 'code-slash-outline', 6, '1', '0', '0', '1', '开发工具目录', 'system', NOW(), 'system', NOW(), '0'),

-- Level 2: Dev sub-menus
(61, 60, 'SQL转API', 'dev:sql2api:view', 'C', 'sql2api', 'sql2api/SqlApiEditor', 'terminal-outline', 1, '1', '0', '0', '1', 'SQL转API菜单', 'system', NOW(), 'system', NOW(), '0'),
(62, 60, '代码生成', 'dev:generator:view', 'C', 'generator', 'dev/CodeGenerator', 'construct-outline', 2, '1', '0', '0', '1', '代码生成菜单', 'system', NOW(), 'system', NOW(), '0'),

-- ===== Button Permissions (Functions) =====
-- User management buttons
(111, 11, '用户查询', 'system:user:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(112, 11, '用户新增', 'system:user:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(113, 11, '用户修改', 'system:user:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(114, 11, '用户删除', 'system:user:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(115, 11, '用户导出', 'system:user:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(116, 11, '用户导入', 'system:user:import', 'F', '', '', '#', 6, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(117, 11, '重置密码', 'system:user:resetPwd', 'F', '', '', '#', 7, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- Role management buttons
(121, 12, '角色查询', 'system:role:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(122, 12, '角色新增', 'system:role:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(123, 12, '角色修改', 'system:role:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(124, 12, '角色删除', 'system:role:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(125, 12, '角色导出', 'system:role:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- Dept management buttons
(131, 13, '部门查询', 'system:dept:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(132, 13, '部门新增', 'system:dept:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(133, 13, '部门修改', 'system:dept:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(134, 13, '部门删除', 'system:dept:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- Permission management buttons
(141, 14, '权限查询', 'system:permission:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(142, 14, '权限新增', 'system:permission:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(143, 14, '权限修改', 'system:permission:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(144, 14, '权限删除', 'system:permission:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0');

-- =============================================
-- Role Permission Assignment
-- =============================================

-- Admin role gets ALL permissions
INSERT INTO `sys_role_permission` 
SELECT 1, permission_id FROM `sys_permission`;

-- Common role gets only view permissions (all menus and directories, no buttons)
INSERT INTO `sys_role_permission` 
SELECT 2, permission_id FROM `sys_permission` WHERE permission_type IN ('M', 'C');

-- =============================================
-- Role Inherited Permission Cache (RBAC1)
-- =============================================

-- Initialize role inherited permission cache
INSERT INTO `sys_role_inherited_permission` (`role_id`, `permission_id`, `is_inherited`, `inherited_from`, `tenant_id`) 
SELECT rp.`role_id`, rp.`permission_id`, '0', NULL, r.`tenant_id`
FROM `sys_role_permission` rp
INNER JOIN `sys_role` r ON rp.`role_id` = r.`role_id`
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_inherited_permission` rip 
    WHERE rip.`role_id` = rp.`role_id` AND rip.`permission_id` = rp.`permission_id`
);

-- =============================================
-- Verification
-- =============================================
SELECT 
    '权限总数' as '统计项', 
    COUNT(*) as '数量' 
FROM `sys_permission`
UNION ALL
SELECT 
    '管理员权限数' as '统计项', 
    COUNT(*) as '数量' 
FROM `sys_role_permission` 
WHERE role_id = 1
UNION ALL
SELECT 
    '普通用户权限数' as '统计项', 
    COUNT(*) as '数量' 
FROM `sys_role_permission` 
WHERE role_id = 2;
