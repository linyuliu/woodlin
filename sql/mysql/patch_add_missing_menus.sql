-- =============================================
-- Woodlin 完整菜单权限补丁
-- 
-- 问题：数据库中只有"系统管理"模块的权限，缺少其他所有菜单模块
-- 解决：添加所有前端路由对应的菜单权限
-- 
-- 作者: mumu
-- 日期: 2025-12-27
-- =============================================

USE `woodlin`;

-- 删除旧的不完整权限数据（保留系统管理相关）
-- 注意：这会清除超过ID 500的权限，保留基础的系统管理权限
DELETE FROM `sys_role_permission` WHERE `permission_id` > 500;
DELETE FROM `sys_role_inherited_permission` WHERE `permission_id` > 500;
DELETE FROM `sys_permission` WHERE `permission_id` > 500;

-- =============================================
-- 第一部分：顶级菜单和子菜单权限
-- =============================================

-- 仪表板（ID: 1000）
INSERT INTO `sys_permission` VALUES 
(1000, 0, '仪表板', 'dashboard', 'C', 'dashboard', 'DashboardView', 'dashboard-outline', 1, '1', '0', '0', '1', '仪表板页面', 'system', NOW(), 'system', NOW(), '0');

-- 数据源管理（ID: 2000-2099）
INSERT INTO `sys_permission` VALUES 
(2000, 0, '数据源管理', 'datasource', 'M', 'datasource', NULL, 'server-outline', 2, '1', '0', '0', '1', '数据源管理目录', 'system', NOW(), 'system', NOW(), '0'),
(2001, 2000, '数据源列表', 'datasource:list', 'C', 'list', 'datasource/DatasourceList', 'list-outline', 1, '1', '0', '0', '1', '数据源列表页面', 'system', NOW(), 'system', NOW(), '0'),
(2002, 2000, '数据源监控', 'datasource:monitor', 'C', 'monitor', 'datasource/DatasourceMonitor', 'stats-chart-outline', 2, '1', '0', '0', '1', '数据源监控页面', 'system', NOW(), 'system', NOW(), '0');

-- 租户管理（ID: 3000-3099）
INSERT INTO `sys_permission` VALUES 
(3000, 0, '租户管理', 'tenant', 'M', 'tenant', NULL, 'home-outline', 3, '1', '0', '0', '1', '租户管理目录', 'system', NOW(), 'system', NOW(), '0'),
(3001, 3000, '租户列表', 'tenant:list', 'C', 'list', 'tenant/TenantView', 'list-outline', 1, '1', '0', '0', '1', '租户列表页面', 'system', NOW(), 'system', NOW(), '0');

-- 文件管理（ID: 4000-4099）
INSERT INTO `sys_permission` VALUES 
(4000, 0, '文件管理', 'file', 'M', 'file', NULL, 'folder-outline', 4, '1', '0', '0', '1', '文件管理目录', 'system', NOW(), 'system', NOW(), '0'),
(4001, 4000, '文件列表', 'file:list', 'C', 'list', 'file/FileList', 'documents-outline', 1, '1', '0', '0', '1', '文件列表页面', 'system', NOW(), 'system', NOW(), '0'),
(4002, 4000, '存储配置', 'file:storage', 'C', 'storage', 'file/FileStorage', 'archive-outline', 2, '1', '0', '0', '1', '存储配置页面', 'system', NOW(), 'system', NOW(), '0');

-- 任务管理（ID: 5000-5099）
INSERT INTO `sys_permission` VALUES 
(5000, 0, '任务管理', 'task', 'M', 'task', NULL, 'timer-outline', 5, '1', '0', '0', '1', '任务管理目录', 'system', NOW(), 'system', NOW(), '0'),
(5001, 5000, '任务列表', 'task:list', 'C', 'list', 'task/TaskList', 'list-outline', 1, '1', '0', '0', '1', '任务列表页面', 'system', NOW(), 'system', NOW(), '0'),
(5002, 5000, '任务日志', 'task:log', 'C', 'log', 'task/TaskLog', 'receipt-outline', 2, '1', '0', '0', '1', '任务日志页面', 'system', NOW(), 'system', NOW(), '0');

-- 开发工具（ID: 6000-6099）
INSERT INTO `sys_permission` VALUES 
(6000, 0, '开发工具', 'dev', 'M', 'dev', NULL, 'code-slash-outline', 6, '1', '0', '0', '1', '开发工具目录', 'system', NOW(), 'system', NOW(), '0'),
(6001, 6000, 'SQL转API', 'dev:sql2api', 'C', 'sql2api', 'sql2api/SqlApiEditor', 'terminal-outline', 1, '1', '0', '0', '1', 'SQL转API页面', 'system', NOW(), 'system', NOW(), '0'),
(6002, 6000, '代码生成', 'dev:generator', 'C', 'generator', 'dev/CodeGenerator', 'construct-outline', 2, '1', '0', '0', '1', '代码生成页面', 'system', NOW(), 'system', NOW(), '0');

-- =============================================
-- 第二部分：补充系统管理缺失的子菜单
-- =============================================

-- 字典管理（ID: 6）
INSERT INTO `sys_permission` VALUES 
(6, 1, '字典管理', 'system:dict', 'C', 'dict', 'system/DictView', 'book-outline', 5, '1', '0', '0', '1', '字典管理菜单', 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE 
  `permission_name` = '字典管理',
  `permission_code` = 'system:dict',
  `path` = 'dict',
  `component` = 'system/DictView',
  `icon` = 'book-outline';

-- 配置管理（ID: 7）
INSERT INTO `sys_permission` VALUES 
(7, 1, '配置管理', 'system:config', 'C', 'config', 'system/ConfigView', 'options-outline', 6, '1', '0', '0', '1', '配置管理菜单', 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE 
  `permission_name` = '配置管理',
  `permission_code` = 'system:config',
  `path` = 'config',
  `component` = 'system/ConfigView',
  `icon` = 'options-outline';

-- 系统设置（ID: 8）
INSERT INTO `sys_permission` VALUES 
(8, 1, '系统设置', 'system:settings', 'C', 'settings', 'system/SystemSettingsView', 'cog-outline', 7, '1', '0', '0', '1', '系统设置菜单', 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE 
  `permission_name` = '系统设置',
  `permission_code` = 'system:settings',
  `path` = 'settings',
  `component` = 'system/SystemSettingsView',
  `icon` = 'cog-outline';

-- =============================================
-- 第三部分：为超级管理员角色添加所有新权限
-- =============================================

-- 添加仪表板权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (1, 1000);

-- 添加数据源管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(1, 2000), (1, 2001), (1, 2002);

-- 添加租户管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(1, 3000), (1, 3001);

-- 添加文件管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(1, 4000), (1, 4001), (1, 4002);

-- 添加任务管理权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(1, 5000), (1, 5001), (1, 5002);

-- 添加开发工具权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(1, 6000), (1, 6001), (1, 6002);

-- 添加系统管理新增子菜单权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(1, 6), (1, 7), (1, 8);

-- =============================================
-- 第四部分：为普通角色添加查看权限
-- =============================================

-- 添加仪表板查看权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (2, 1000);

-- 添加各模块的查看权限（目录和列表页）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES 
(2, 2000), (2, 2001),  -- 数据源
(2, 3000), (2, 3001),  -- 租户
(2, 4000), (2, 4001),  -- 文件
(2, 5000), (2, 5001),  -- 任务
(2, 6000), (2, 6001);  -- 开发工具

-- =============================================
-- 第五部分：重建权限继承缓存（RBAC1）
-- =============================================

-- 清除旧的继承权限缓存
DELETE FROM `sys_role_inherited_permission`;

-- 重新生成所有角色的直接权限缓存
INSERT INTO `sys_role_inherited_permission` (`role_id`, `permission_id`, `is_inherited`, `inherited_from`, `tenant_id`) 
SELECT rp.`role_id`, rp.`permission_id`, '0', NULL, r.`tenant_id`
FROM `sys_role_permission` rp
INNER JOIN `sys_role` r ON rp.`role_id` = r.`role_id`
WHERE r.`deleted` = '0';

-- =============================================
-- 验证脚本执行结果
-- =============================================

-- 查看超级管理员的所有菜单权限数量
SELECT 
    '超级管理员菜单权限' AS description,
    COUNT(*) AS count
FROM `sys_role_permission` rp
INNER JOIN `sys_permission` p ON rp.`permission_id` = p.`permission_id`
WHERE rp.`role_id` = 1 
  AND p.`permission_type` IN ('M', 'C')
  AND p.`deleted` = '0';

-- 查看所有顶级菜单
SELECT 
    `permission_id`,
    `permission_name`,
    `permission_code`,
    `permission_type`,
    `path`,
    `icon`,
    `sort_order`
FROM `sys_permission`
WHERE `parent_id` = 0
  AND `deleted` = '0'
ORDER BY `sort_order`;

-- 完成
SELECT '菜单权限补丁执行完成！请重新登录查看所有菜单。' AS message;
