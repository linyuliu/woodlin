-- Migration: Full module menu seed data
-- Inserts complete menu tree covering all business modules.
-- Date: 2026-05-05
-- Author: yulin
--
-- Columns: permission_id, parent_id, permission_name, permission_code,
--   permission_type, path, component, icon, sort_order, status,
--   is_frame, is_cache, visible, show_in_tabs, active_menu, redirect, deleted

-- Clean legacy seed data from the old frontend route tree
DELETE FROM `sys_role_inherited_permission` WHERE `permission_id` IN (
  1,2,3,4,5,6,7,8,9,
  103,104,105,106,
  902,903,904,905,906,907,908,909,910,911,912,
  2000,2001,2101,2102,2103,2104,2105,
  3000,3001,3101,3102,3103,
  4000,4001,4002,
  5000,5001,5002,
  6000,6001,6002,
  7000,7001,7002,7101,7102,7103,7104,7105,7106,7107,7108,7109,7110,7201,7202
);
DELETE FROM `sys_role_permission` WHERE `permission_id` IN (
  1,2,3,4,5,6,7,8,9,
  103,104,105,106,
  902,903,904,905,906,907,908,909,910,911,912,
  2000,2001,2101,2102,2103,2104,2105,
  3000,3001,3101,3102,3103,
  4000,4001,4002,
  5000,5001,5002,
  6000,6001,6002,
  7000,7001,7002,7101,7102,7103,7104,7105,7106,7107,7108,7109,7110,7201,7202
);
DELETE FROM `sys_permission` WHERE `permission_id` IN (
  1,2,3,4,5,6,7,8,9,
  103,104,105,106,
  902,903,904,905,906,907,908,909,910,911,912,
  2000,2001,2101,2102,2103,2104,2105,
  3000,3001,3101,3102,3103,
  4000,4001,4002,
  5000,5001,5002,
  6000,6001,6002,
  7000,7001,7002,7101,7102,7103,7104,7105,7106,7107,7108,7109,7110,7201,7202
);

-- Clean any existing seed data to allow idempotent re-run
DELETE FROM `sys_role_permission` WHERE `permission_id` IN (
  100,101,102,
  200,201,2011,2012,2013,2014,2015,2016,2017,2018,
  202,2021,2022,2023,2024,2025,2026,
  203,2031,2032,2033,2034,
  204,2041,2042,2043,2044,
  205,2051,2052,2053,2054,
  206,2061,2062,2063,2064,
  207,2071,2072,2073,2074,
  208,2081,2082,
  300,301,3011,3012,3013,3014,302,3021,3022,3023,3024,
  400,401,4011,4012,4013,4014,402,4021,4022,4023,4024,
  403,4031,4032,4033,4034,404,4041,
  500,501,5011,5012,5013,5014,
  600,601,6011,6012,6013,
  700,701,7011,7012,7013,7014,702,7021,7022,7023,7024,
  800,801,8011,8012,8013,8014,802,8021,8022,
  900,901,9011,9012,9013,
  1000,1001,10011,10012,10013,10014,1002,10021,10022,10023,
  1100,1101,11011,11012,11013,11014,1102,11021,11022,
  1200,1201,12011,12012,1202,12021,12022,1203,12031,12032,1204,12041,1205,12051,
  1300,1301,1302,
  1400,1401,1402,1403
);
DELETE FROM `sys_permission` WHERE `permission_id` IN (
  100,101,102,
  200,201,2011,2012,2013,2014,2015,2016,2017,2018,
  202,2021,2022,2023,2024,2025,2026,
  203,2031,2032,2033,2034,
  204,2041,2042,2043,2044,
  205,2051,2052,2053,2054,
  206,2061,2062,2063,2064,
  207,2071,2072,2073,2074,
  208,2081,2082,
  300,301,3011,3012,3013,3014,302,3021,3022,3023,3024,
  400,401,4011,4012,4013,4014,402,4021,4022,4023,4024,
  403,4031,4032,4033,4034,404,4041,
  500,501,5011,5012,5013,5014,
  600,601,6011,6012,6013,
  700,701,7011,7012,7013,7014,702,7021,7022,7023,7024,
  800,801,8011,8012,8013,8014,802,8021,8022,
  900,901,9011,9012,9013,
  1000,1001,10011,10012,10013,10014,1002,10021,10022,10023,
  1100,1101,11011,11012,11013,11014,1102,11021,11022,
  1200,1201,12011,12012,1202,12021,12022,1203,12031,12032,1204,12041,1205,12051,
  1300,1301,1302,
  1400,1401,1402,1403
);

-- =============================================
-- INSERT sys_permission
-- Columns: id, parent_id, name, code, type, path, component, icon,
--          sort, status, is_frame, is_cache, visible, show_in_tabs,
--          active_menu, redirect, deleted
-- Types: M=目录, C=菜单, F=按钮
-- =============================================
INSERT INTO `sys_permission`
  (`permission_id`,`parent_id`,`permission_name`,`permission_code`,`permission_type`,
   `path`,`component`,`icon`,`sort_order`,`status`,
   `is_frame`,`is_cache`,`visible`,`show_in_tabs`,`active_menu`,`redirect`,`deleted`)
VALUES
-- =========================================
-- 仪表盘
-- =========================================
(100, 0, '仪表盘', 'dashboard', 'M',
 '/dashboard', 'Layout', 'vicons:antd:DashboardOutlined', 1, '1',
 '0','0','1','1',NULL,'/dashboard/workplace','0'),
(101, 100, '工作台', 'dashboard:workplace', 'C',
 '/dashboard/workplace', 'dashboard/workplace/index', 'vicons:antd:HomeOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(102, 100, '分析页', 'dashboard:analysis', 'C',
 '/dashboard/analysis', 'dashboard/analysis/index', 'vicons:antd:BarChartOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),

-- =========================================
-- 系统管理
-- =========================================
(200, 0, '系统管理', 'system', 'M',
 '/system', 'Layout', 'vicons:antd:SettingOutlined', 2, '1',
 '0','0','1','1',NULL,'/system/user','0'),

-- 用户管理
(201, 200, '用户管理', 'system:user', 'C',
 '/system/user', 'system/user/index', 'vicons:antd:UserOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2011, 201, '用户查询', 'system:user:list', 'F', '', '', '', 1, '1', '0','0','1','1',NULL,NULL,'0'),
(2012, 201, '用户新增', 'system:user:add',  'F', '', '', '', 2, '1', '0','0','1','1',NULL,NULL,'0'),
(2013, 201, '用户编辑', 'system:user:edit', 'F', '', '', '', 3, '1', '0','0','1','1',NULL,NULL,'0'),
(2014, 201, '用户删除', 'system:user:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(2015, 201, '用户导出', 'system:user:export','F','','','',5,'1','0','0','1','1',NULL,NULL,'0'),
(2016, 201, '用户导入', 'system:user:import','F','','','',6,'1','0','0','1','1',NULL,NULL,'0'),
(2017, 201, '重置密码', 'system:user:resetPwd','F','','','',7,'1','0','0','1','1',NULL,NULL,'0'),
(2018, 201, '分配角色', 'system:user:assignRole','F','','','',8,'1','0','0','1','1',NULL,NULL,'0'),

-- 角色管理
(202, 200, '角色管理', 'system:role', 'C',
 '/system/role', 'system/role/index', 'vicons:antd:TeamOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2021, 202, '角色查询', 'system:role:list',       'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2022, 202, '角色新增', 'system:role:add',        'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(2023, 202, '角色编辑', 'system:role:edit',       'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(2024, 202, '角色删除', 'system:role:remove',     'F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(2025, 202, '菜单授权', 'system:role:assignMenu', 'F','','','',5,'1','0','0','1','1',NULL,NULL,'0'),
(2026, 202, '用户授权', 'system:role:assignUser', 'F','','','',6,'1','0','0','1','1',NULL,NULL,'0'),

-- 菜单管理
(203, 200, '菜单管理', 'system:menu', 'C',
 '/system/menu', 'system/menu/index', 'vicons:antd:MenuOutlined', 3, '1',
 '0','0','1','1',NULL,NULL,'0'),
(2031, 203, '菜单查询', 'system:menu:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2032, 203, '菜单新增', 'system:menu:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(2033, 203, '菜单编辑', 'system:menu:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(2034, 203, '菜单删除', 'system:menu:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- 部门管理
(204, 200, '部门管理', 'system:dept', 'C',
 '/system/dept', 'system/dept/index', 'vicons:antd:ApartmentOutlined', 4, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2041, 204, '部门查询', 'system:dept:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2042, 204, '部门新增', 'system:dept:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(2043, 204, '部门编辑', 'system:dept:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(2044, 204, '部门删除', 'system:dept:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- 字典管理
(205, 200, '字典管理', 'system:dict', 'C',
 '/system/dict', 'system/dict/index', 'vicons:antd:BookOutlined', 5, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2051, 205, '字典查询', 'system:dict:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2052, 205, '字典新增', 'system:dict:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(2053, 205, '字典编辑', 'system:dict:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(2054, 205, '字典删除', 'system:dict:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- 参数配置
(206, 200, '参数配置', 'system:config', 'C',
 '/system/config', 'system/config/index', 'vicons:antd:ControlOutlined', 6, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2061, 206, '配置查询', 'system:config:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2062, 206, '配置新增', 'system:config:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(2063, 206, '配置编辑', 'system:config:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(2064, 206, '配置删除', 'system:config:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- 通知公告
(207, 200, '通知公告', 'system:notice', 'C',
 '/system/notice', 'system/notice/index', 'vicons:antd:NotificationOutlined', 7, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2071, 207, '公告查询', 'system:notice:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2072, 207, '公告新增', 'system:notice:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(2073, 207, '公告编辑', 'system:notice:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(2074, 207, '公告删除', 'system:notice:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- 行政区划
(208, 200, '行政区划', 'system:region', 'C',
 '/system/region', 'system/region/index', 'vicons:antd:GlobalOutlined', 8, '1',
 '0','1','1','1',NULL,NULL,'0'),
(2081, 208, '区划查询', 'system:region:list', 'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(2082, 208, '区划同步', 'system:region:sync', 'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 租户管理
-- =========================================
(300, 0, '租户管理', 'tenant', 'M',
 '/tenant', 'Layout', 'vicons:antd:CloudOutlined', 3, '1',
 '0','0','1','1',NULL,'/tenant/list','0'),
(301, 300, '租户列表', 'tenant:list', 'C',
 '/tenant/list', 'tenant/tenant/index', 'vicons:antd:ClusterOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(3011, 301, '租户查询', 'tenant:list:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(3012, 301, '租户新增', 'tenant:list:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(3013, 301, '租户编辑', 'tenant:list:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(3014, 301, '租户删除', 'tenant:list:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(302, 300, '租户套餐', 'tenant:package', 'C',
 '/tenant/package', 'tenant/package/index', 'vicons:antd:GiftOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(3021, 302, '套餐查询', 'tenant:package:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(3022, 302, '套餐新增', 'tenant:package:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(3023, 302, '套餐编辑', 'tenant:package:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(3024, 302, '套餐删除', 'tenant:package:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- OpenAPI 管理
-- =========================================
(400, 0, 'OpenAPI管理', 'openapi', 'M',
 '/openapi', 'Layout', 'vicons:antd:ApiOutlined', 4, '1',
 '0','0','1','1',NULL,'/openapi/app','0'),
(401, 400, 'API应用', 'openapi:app', 'C',
 '/openapi/app', 'openapi/app/index', 'vicons:antd:AppstoreOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(4011, 401, '应用查询', 'openapi:app:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(4012, 401, '应用新增', 'openapi:app:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(4013, 401, '应用编辑', 'openapi:app:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(4014, 401, '应用删除', 'openapi:app:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(402, 400, 'API凭证', 'openapi:credential', 'C',
 '/openapi/credential', 'openapi/credential/index', 'vicons:antd:KeyOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(4021, 402, '凭证查询', 'openapi:credential:list',   'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(4022, 402, '凭证签发', 'openapi:credential:issue',  'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(4023, 402, '凭证吊销', 'openapi:credential:revoke', 'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(4024, 402, '凭证查看', 'openapi:credential:view',   'F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(403, 400, 'API策略', 'openapi:policy', 'C',
 '/openapi/policy', 'openapi/policy/index', 'vicons:antd:SafetyCertificateOutlined', 3, '1',
 '0','1','1','1',NULL,NULL,'0'),
(4031, 403, '策略查询', 'openapi:policy:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(4032, 403, '策略新增', 'openapi:policy:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(4033, 403, '策略编辑', 'openapi:policy:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(4034, 403, '策略删除', 'openapi:policy:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(404, 400, '调用概览', 'openapi:overview', 'C',
 '/openapi/overview', 'openapi/overview/index', 'vicons:antd:AreaChartOutlined', 4, '1',
 '0','1','1','1',NULL,NULL,'0'),
(4041, 404, '概览查看', 'openapi:overview:view','F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 数据源管理
-- =========================================
(500, 0, '数据源管理', 'datasource', 'M',
 '/datasource', 'Layout', 'vicons:antd:DatabaseOutlined', 5, '1',
 '0','0','1','1',NULL,'/datasource/list','0'),
(501, 500, '数据源', 'datasource:list', 'C',
 '/datasource/list', 'datasource/index', 'vicons:antd:HddOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(5011, 501, '数据源查询', 'datasource:list:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(5012, 501, '数据源新增', 'datasource:list:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(5013, 501, '数据源编辑', 'datasource:list:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(5014, 501, '数据源删除', 'datasource:list:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- SQL2API
-- =========================================
(600, 0, 'SQL2API', 'sql2api', 'M',
 '/sql2api', 'Layout', 'vicons:antd:CodeOutlined', 6, '1',
 '0','0','1','1',NULL,'/sql2api/design','0'),
(601, 600, 'API设计器', 'sql2api:design', 'C',
 '/sql2api/design', 'sql2api/index', 'vicons:antd:EditOutlined', 1, '1',
 '0','0','1','1',NULL,NULL,'0'),
(6011, 601, '接口查询', 'sql2api:design:list',   'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(6012, 601, '接口新增', 'sql2api:design:add',    'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(6013, 601, '接口删除', 'sql2api:design:remove', 'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 文件管理
-- =========================================
(700, 0, '文件管理', 'file', 'M',
 '/file', 'Layout', 'vicons:antd:FolderOutlined', 7, '1',
 '0','0','1','1',NULL,'/file/list','0'),
(701, 700, '文件列表', 'file:list', 'C',
 '/file/list', 'file/list/index', 'vicons:antd:FileOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(7011, 701, '文件查询', 'file:list:list',   'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(7012, 701, '文件上传', 'file:list:upload', 'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(7013, 701, '文件下载', 'file:list:download','F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(7014, 701, '文件删除', 'file:list:remove', 'F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(702, 700, '存储配置', 'file:storage', 'C',
 '/file/storage', 'file/storage/index', 'vicons:antd:CloudServerOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(7021, 702, '存储查询', 'file:storage:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(7022, 702, '存储新增', 'file:storage:add',   'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(7023, 702, '存储编辑', 'file:storage:edit',  'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(7024, 702, '存储删除', 'file:storage:remove','F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 任务调度
-- =========================================
(800, 0, '任务调度', 'schedule', 'M',
 '/schedule', 'Layout', 'vicons:antd:ClockCircleOutlined', 8, '1',
 '0','0','1','1',NULL,'/schedule/job','0'),
(801, 800, '定时任务', 'schedule:job', 'C',
 '/schedule/job', 'schedule/job/index', 'vicons:antd:ScheduleOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(8011, 801, '任务查询', 'schedule:job:list',   'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(8012, 801, '任务新增', 'schedule:job:add',    'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(8013, 801, '任务编辑', 'schedule:job:edit',   'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(8014, 801, '任务删除', 'schedule:job:remove', 'F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(802, 800, '调度日志', 'schedule:log', 'C',
 '/schedule/log', 'schedule/log/index', 'vicons:antd:FileTextOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(8021, 802, '日志查询', 'schedule:log:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(8022, 802, '日志清空', 'schedule:log:clear', 'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 代码生成
-- =========================================
(900, 0, '代码生成', 'code', 'M',
 '/code', 'Layout', 'vicons:antd:ThunderboltOutlined', 9, '1',
 '0','0','1','1',NULL,'/code/generator','0'),
(901, 900, '生成配置', 'code:generator', 'C',
 '/code/generator', 'code/index', 'vicons:antd:BuildOutlined', 1, '1',
 '0','0','1','1',NULL,NULL,'0'),
(9011, 901, '表查询',   'code:generator:list',     'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(9012, 901, '代码预览', 'code:generator:preview',  'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(9013, 901, '代码下载', 'code:generator:download', 'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 测评中心
-- =========================================
(1000, 0, '测评中心', 'assessment', 'M',
 '/assessment', 'Layout', 'vicons:antd:FormOutlined', 10, '1',
 '0','0','1','1',NULL,'/assessment/schema','0'),
(1001, 1000, '表单模板', 'assessment:schema', 'C',
 '/assessment/schema', 'assessment/schema/index', 'vicons:antd:ProfileOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(10011, 1001, '模板查询', 'assessment:schema:list',   'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(10012, 1001, '模板新增', 'assessment:schema:add',    'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(10013, 1001, '模板编辑', 'assessment:schema:edit',   'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(10014, 1001, '模板删除', 'assessment:schema:remove', 'F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(1002, 1000, '运行实例', 'assessment:runtime', 'C',
 '/assessment/runtime', 'assessment/runtime/index', 'vicons:antd:PlayCircleOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(10021, 1002, '实例查询', 'assessment:runtime:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(10022, 1002, '作答查看', 'assessment:runtime:view',  'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(10023, 1002, '实例删除', 'assessment:runtime:remove','F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- ETL 管理
-- =========================================
(1100, 0, 'ETL管理', 'etl', 'M',
 '/etl', 'Layout', 'vicons:antd:InteractionOutlined', 11, '1',
 '0','0','1','1',NULL,'/etl/offline','0'),
(1101, 1100, '离线任务', 'etl:offline', 'C',
 '/etl/offline', 'etl/offline/index', 'vicons:antd:CloudDownloadOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(11011, 1101, '任务查询', 'etl:offline:list',   'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(11012, 1101, '任务新增', 'etl:offline:add',    'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(11013, 1101, '任务编辑', 'etl:offline:edit',   'F','','','',3,'1','0','0','1','1',NULL,NULL,'0'),
(11014, 1101, '任务删除', 'etl:offline:remove', 'F','','','',4,'1','0','0','1','1',NULL,NULL,'0'),
(1102, 1100, '执行明细', 'etl:log', 'C',
 '/etl/log', 'etl/log/index', 'vicons:antd:UnorderedListOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(11021, 1102, '日志查询', 'etl:log:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(11022, 1102, '日志清空', 'etl:log:clear', 'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 系统监控
-- =========================================
(1200, 0, '系统监控', 'monitor', 'M',
 '/monitor', 'Layout', 'vicons:antd:MonitorOutlined', 12, '1',
 '0','0','1','1',NULL,'/monitor/online','0'),
(1201, 1200, '在线用户', 'monitor:online', 'C',
 '/monitor/online', 'monitor/online/index', 'vicons:antd:UserSwitchOutlined', 1, '1',
 '0','1','1','1',NULL,NULL,'0'),
(12011, 1201, '在线查询', 'monitor:online:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(12012, 1201, '强制下线', 'monitor:online:logout','F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(1202, 1200, '登录日志', 'monitor:loginLog', 'C',
 '/monitor/loginLog', 'monitor/loginLog/index', 'vicons:antd:LoginOutlined', 2, '1',
 '0','1','1','1',NULL,NULL,'0'),
(12021, 1202, '日志查询', 'monitor:loginLog:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(12022, 1202, '日志清空', 'monitor:loginLog:clear', 'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(1203, 1200, '操作日志', 'monitor:operLog', 'C',
 '/monitor/operLog', 'monitor/operLog/index', 'vicons:antd:AuditOutlined', 3, '1',
 '0','1','1','1',NULL,NULL,'0'),
(12031, 1203, '日志查询', 'monitor:operLog:list',  'F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(12032, 1203, '日志清空', 'monitor:operLog:clear', 'F','','','',2,'1','0','0','1','1',NULL,NULL,'0'),
(1204, 1200, '服务监控', 'monitor:server', 'C',
 '/monitor/server', 'monitor/server/index', 'vicons:antd:CloudServerOutlined', 4, '1',
 '0','0','1','1',NULL,NULL,'0'),
(12041, 1204, '服务查看', 'monitor:server:view','F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),
(1205, 1200, '缓存监控', 'monitor:cache', 'C',
 '/monitor/cache', 'monitor/cache/index', 'vicons:antd:ThunderboltOutlined', 5, '1',
 '0','0','1','1',NULL,NULL,'0'),
(12051, 1205, '缓存查看', 'monitor:cache:view','F','','','',1,'1','0','0','1','1',NULL,NULL,'0'),

-- =========================================
-- 个人中心（隐藏，仍需权限控制）
-- =========================================
(1300, 0, '个人中心', 'user', 'M',
 '/user', 'Layout', 'vicons:antd:UserOutlined', 99, '1',
 '0','0','0','0',NULL,'/user/profile','0'),
(1301, 1300, '个人信息', 'user:profile', 'C',
 '/user/profile', 'user/profile/index', 'vicons:antd:IdcardOutlined', 1, '1',
 '0','1','0','1',NULL,NULL,'0'),
(1302, 1300, '消息中心', 'user:message', 'C',
 '/user/message', 'user/message/index', 'vicons:antd:MessageOutlined', 2, '1',
 '0','1','0','1',NULL,NULL,'0'),

-- =========================================
-- 关于
-- =========================================
(1400, 0, '关于', 'about', 'M',
 '/about', 'Layout', 'vicons:antd:InfoCircleOutlined', 13, '1',
 '0','0','1','1',NULL,'/about/apiDoc','0'),
(1401, 1400, '接口文档', 'about:apiDoc', 'C',
 '/about/apiDoc', 'about/apiDoc/index', 'vicons:antd:FileSearchOutlined', 1, '1',
 '0','0','1','1',NULL,NULL,'0'),
(1402, 1400, '更新日志', 'about:changelog', 'C',
 '/about/changelog', 'about/changelog/index', 'vicons:antd:HistoryOutlined', 2, '1',
 '0','0','1','1',NULL,NULL,'0'),
(1403, 1400, '开源地址', 'about:source', 'C',
 'https://github.com/linyuliu/woodlin', '', 'vicons:antd:GithubOutlined', 3, '1',
 '1','0','1','1',NULL,NULL,'0');

-- =============================================
-- Bind ALL menus to super-admin role (role_id = 1)
-- =============================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
VALUES
(1,100),(1,101),(1,102),
(1,200),
(1,201),(1,2011),(1,2012),(1,2013),(1,2014),(1,2015),(1,2016),(1,2017),(1,2018),
(1,202),(1,2021),(1,2022),(1,2023),(1,2024),(1,2025),(1,2026),
(1,203),(1,2031),(1,2032),(1,2033),(1,2034),
(1,204),(1,2041),(1,2042),(1,2043),(1,2044),
(1,205),(1,2051),(1,2052),(1,2053),(1,2054),
(1,206),(1,2061),(1,2062),(1,2063),(1,2064),
(1,207),(1,2071),(1,2072),(1,2073),(1,2074),
(1,208),(1,2081),(1,2082),
(1,300),
(1,301),(1,3011),(1,3012),(1,3013),(1,3014),
(1,302),(1,3021),(1,3022),(1,3023),(1,3024),
(1,400),
(1,401),(1,4011),(1,4012),(1,4013),(1,4014),
(1,402),(1,4021),(1,4022),(1,4023),(1,4024),
(1,403),(1,4031),(1,4032),(1,4033),(1,4034),
(1,404),(1,4041),
(1,500),
(1,501),(1,5011),(1,5012),(1,5013),(1,5014),
(1,600),
(1,601),(1,6011),(1,6012),(1,6013),
(1,700),
(1,701),(1,7011),(1,7012),(1,7013),(1,7014),
(1,702),(1,7021),(1,7022),(1,7023),(1,7024),
(1,800),
(1,801),(1,8011),(1,8012),(1,8013),(1,8014),
(1,802),(1,8021),(1,8022),
(1,900),
(1,901),(1,9011),(1,9012),(1,9013),
(1,1000),
(1,1001),(1,10011),(1,10012),(1,10013),(1,10014),
(1,1002),(1,10021),(1,10022),(1,10023),
(1,1100),
(1,1101),(1,11011),(1,11012),(1,11013),(1,11014),
(1,1102),(1,11021),(1,11022),
(1,1200),
(1,1201),(1,12011),(1,12012),
(1,1202),(1,12021),(1,12022),
(1,1203),(1,12031),(1,12032),
(1,1204),(1,12041),
(1,1205),(1,12051),
(1,1300),
(1,1301),(1,1302),
(1,1400),
(1,1401),(1,1402),(1,1403);
