-- =============================================
-- Woodlin 多租户中后台管理系统 - 完整初始数据
-- 作者: mumu
-- 描述: 包含所有初始化数据的完整脚本
-- 版本: 2.0.0 (RBAC1支持)
-- 时间: 2025-11-07
-- 说明: 本脚本包含基础数据 + RBAC1初始化 + OSS默认配置 + 系统配置
-- 前置要求: 请先执行 woodlin_complete_schema.sql 创建表结构
-- =============================================

USE `woodlin`;

-- =============================================
-- 第一部分：基础租户和部门数据
-- =============================================

-- 初始化租户数据
INSERT INTO `sys_tenant` VALUES 
('default', '默认租户', 'default', '系统管理员', '13800000000', 'admin@woodlin.com', '1', '2030-12-31 23:59:59', 1000, '系统默认租户', 'system', NOW(), 'system', NOW(), '0');

-- 初始化部门数据
INSERT INTO `sys_dept` VALUES 
(100, 0, '0', '木林科技', 'woodlin', 0, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', '总公司', 'system', NOW(), 'system', NOW(), '0'),
(101, 100, '0,100', '深圳总公司', 'sz_company', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(102, 100, '0,100', '长沙分公司', 'cs_company', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(103, 101, '0,100,101', '研发部门', 'dev_dept', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(104, 101, '0,100,101', '市场部门', 'market_dept', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(105, 101, '0,100,101', '测试部门', 'test_dept', 3, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(106, 101, '0,100,101', '财务部门', 'finance_dept', 4, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(107, 101, '0,100,101', '运维部门', 'ops_dept', 5, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(108, 102, '0,100,102', '市场部门', 'cs_market_dept', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0'),
(109, 102, '0,100,102', '财务部门', 'cs_finance_dept', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', NOW(), 'system', NOW(), '0');

-- =============================================
-- 第二部分：用户数据
-- =============================================

-- 初始化用户数据
-- 密码为 Passw0rd，已使用BCrypt加密
INSERT INTO `sys_user` VALUES 
(1, 'admin', '超级管理员', '木林', '$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu', 'admin@woodlin.com', '15888888888', '', 1, NOW(), '1', 'default', 103, NOW(), '127.0.0.1', 0, 0, NULL, NOW(), 0, NULL, '管理员', 'system', NOW(), 'system', NOW(), '0'),
(2, 'demo', '演示用户', '演示', '$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu', 'demo@woodlin.com', '15666666666', '', 1, NOW(), '1', 'default', 105, NOW(), '127.0.0.1', 0, 0, NULL, NOW(), 0, NULL, '演示用户', 'system', NOW(), 'system', NOW(), '0');

-- =============================================
-- 第三部分：角色和权限数据（包含RBAC1支持）
-- =============================================

-- 初始化角色数据（包含RBAC1字段）
INSERT INTO `sys_role` VALUES 
(1, NULL, 0, '/1/', '超级管理员', 'admin', 1, '1', '1', '1', 'default', '超级管理员', 'system', NOW(), 'system', NOW(), '0'),
(2, NULL, 0, '/2/', '普通角色', 'common', 2, '2', '1', '1', 'default', '普通角色', 'system', NOW(), 'system', NOW(), '0');

-- 初始化角色层次关系（RBAC1）- 每个角色指向自己
INSERT INTO `sys_role_hierarchy` (`ancestor_role_id`, `descendant_role_id`, `distance`, `tenant_id`) VALUES
(1, 1, 0, 'default'),
(2, 2, 0, 'default');

-- 初始化权限数据
INSERT INTO `sys_permission` VALUES 
(1, 0, '系统管理', 'system', 'M', 'system', NULL, 'system', 1, '1', '0', '0', '1', '系统管理目录', 'system', NOW(), 'system', NOW(), '0'),
(2, 1, '用户管理', 'system:user', 'C', 'user', 'system/UserView', 'user', 1, '1', '0', '0', '1', '用户管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(3, 1, '角色管理', 'system:role', 'C', 'role', 'system/RoleView', 'peoples', 2, '1', '0', '0', '1', '角色管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(4, 1, '菜单管理', 'system:menu', 'C', 'menu', 'system/permission/PermissionManagementView', 'tree-table', 3, '1', '0', '0', '1', '菜单管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(5, 1, '部门管理', 'system:dept', 'C', 'dept', 'system/DeptView', 'tree', 4, '1', '0', '0', '1', '部门管理菜单', 'system', NOW(), 'system', NOW(), '0'),

-- 用户管理权限
(100, 2, '用户查询', 'system:user:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(101, 2, '用户新增', 'system:user:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(102, 2, '用户修改', 'system:user:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(103, 2, '用户删除', 'system:user:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(104, 2, '用户导出', 'system:user:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(105, 2, '用户导入', 'system:user:import', 'F', '', '', '#', 6, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(106, 2, '重置密码', 'system:user:resetPwd', 'F', '', '', '#', 7, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- 角色管理权限
(200, 3, '角色查询', 'system:role:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(201, 3, '角色新增', 'system:role:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(202, 3, '角色修改', 'system:role:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(203, 3, '角色删除', 'system:role:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(204, 3, '角色导出', 'system:role:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- 菜单管理权限
(300, 4, '菜单查询', 'system:menu:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(301, 4, '菜单新增', 'system:menu:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(302, 4, '菜单修改', 'system:menu:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(303, 4, '菜单删除', 'system:menu:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- 部门管理权限
(400, 5, '部门查询', 'system:dept:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(401, 5, '部门新增', 'system:dept:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(402, 5, '部门修改', 'system:dept:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),
(403, 5, '部门删除', 'system:dept:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', NOW(), 'system', NOW(), '0'),

-- 系统管理-字典管理
(6, 1, '字典管理', 'system:dict', 'C', 'dict', 'system/DictView', 'book-outline', 5, '1', '0', '0', '1', '字典管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(7, 1, '配置管理', 'system:config', 'C', 'config', 'system/ConfigView', 'options-outline', 6, '1', '0', '0', '1', '配置管理菜单', 'system', NOW(), 'system', NOW(), '0'),
(8, 1, '系统设置', 'system:settings', 'C', 'settings', 'system/SystemSettingsView', 'cog-outline', 7, '1', '0', '0', '1', '系统设置菜单', 'system', NOW(), 'system', NOW(), '0'),

-- 仪表板
(1000, 0, '仪表板', 'dashboard', 'C', 'dashboard', 'DashboardView', 'dashboard-outline', 1, '1', '0', '0', '1', '仪表板页面', 'system', NOW(), 'system', NOW(), '0'),

-- 数据源管理
(2000, 0, '数据源管理', 'datasource', 'M', 'datasource', NULL, 'server-outline', 2, '1', '0', '0', '1', '数据源管理目录', 'system', NOW(), 'system', NOW(), '0'),
(2001, 2000, '数据源列表', 'datasource:list', 'C', 'list', 'datasource/DatasourceList', 'list-outline', 1, '1', '0', '0', '1', '数据源列表页面', 'system', NOW(), 'system', NOW(), '0'),
(2002, 2000, '数据源监控', 'datasource:monitor', 'C', 'monitor', 'datasource/DatasourceMonitor', 'stats-chart-outline', 2, '1', '0', '0', '1', '数据源监控页面', 'system', NOW(), 'system', NOW(), '0'),

-- 租户管理
(3000, 0, '租户管理', 'tenant', 'M', 'tenant', NULL, 'home-outline', 3, '1', '0', '0', '1', '租户管理目录', 'system', NOW(), 'system', NOW(), '0'),
(3001, 3000, '租户列表', 'tenant:list', 'C', 'list', 'tenant/TenantView', 'list-outline', 1, '1', '0', '0', '1', '租户列表页面', 'system', NOW(), 'system', NOW(), '0'),

-- 文件管理
(4000, 0, '文件管理', 'file', 'M', 'file', NULL, 'folder-outline', 4, '1', '0', '0', '1', '文件管理目录', 'system', NOW(), 'system', NOW(), '0'),
(4001, 4000, '文件列表', 'file:list', 'C', 'list', 'file/FileList', 'documents-outline', 1, '1', '0', '0', '1', '文件列表页面', 'system', NOW(), 'system', NOW(), '0'),
(4002, 4000, '存储配置', 'file:storage', 'C', 'storage', 'file/FileStorage', 'archive-outline', 2, '1', '0', '0', '1', '存储配置页面', 'system', NOW(), 'system', NOW(), '0'),

-- 任务管理
(5000, 0, '任务管理', 'task', 'M', 'task', NULL, 'timer-outline', 5, '1', '0', '0', '1', '任务管理目录', 'system', NOW(), 'system', NOW(), '0'),
(5001, 5000, '任务列表', 'task:list', 'C', 'list', 'task/TaskList', 'list-outline', 1, '1', '0', '0', '1', '任务列表页面', 'system', NOW(), 'system', NOW(), '0'),
(5002, 5000, '任务日志', 'task:log', 'C', 'log', 'task/TaskLog', 'receipt-outline', 2, '1', '0', '0', '1', '任务日志页面', 'system', NOW(), 'system', NOW(), '0'),

-- 开发工具
(6000, 0, '开发工具', 'dev', 'M', 'dev', NULL, 'code-slash-outline', 6, '1', '0', '0', '1', '开发工具目录', 'system', NOW(), 'system', NOW(), '0'),
(6001, 6000, 'SQL转API', 'dev:sql2api', 'C', 'sql2api', 'sql2api/SqlApiEditor', 'terminal-outline', 1, '1', '0', '0', '1', 'SQL转API页面', 'system', NOW(), 'system', NOW(), '0'),
(6002, 6000, '代码生成', 'dev:generator', 'C', 'generator', 'dev/CodeGenerator', 'construct-outline', 2, '1', '0', '0', '1', '代码生成页面', 'system', NOW(), 'system', NOW(), '0');

-- =============================================
-- 第四部分：用户角色和角色权限关联数据
-- =============================================

-- 初始化用户和角色关联数据
INSERT INTO `sys_user_role` VALUES 
(1, 1),
(2, 2);

-- 初始化角色和权限关联数据（直接权限）
INSERT INTO `sys_role_permission` VALUES 
-- 超级管理员拥有所有权限
-- 系统管理模块
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105), (1, 106),
(1, 200), (1, 201), (1, 202), (1, 203), (1, 204),
(1, 300), (1, 301), (1, 302), (1, 303),
(1, 400), (1, 401), (1, 402), (1, 403),
-- 仪表板
(1, 1000),
-- 数据源管理
(1, 2000), (1, 2001), (1, 2002),
-- 租户管理
(1, 3000), (1, 3001),
-- 文件管理
(1, 4000), (1, 4001), (1, 4002),
-- 任务管理
(1, 5000), (1, 5001), (1, 5002),
-- 开发工具
(1, 6000), (1, 6001), (1, 6002),

-- 普通角色只有查询权限
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
(2, 100), (2, 200), (2, 300), (2, 400),
-- 仪表板
(2, 1000),
-- 数据源（只看列表）
(2, 2000), (2, 2001),
-- 租户（只看列表）
(2, 3000), (2, 3001),
-- 文件（只看列表）
(2, 4000), (2, 4001),
-- 任务（只看列表）
(2, 5000), (2, 5001),
-- 开发工具（只看SQL2API）
(2, 6000), (2, 6001);

-- 初始化角色继承权限缓存（RBAC1）- 包含直接权限
INSERT INTO `sys_role_inherited_permission` (`role_id`, `permission_id`, `is_inherited`, `inherited_from`, `tenant_id`) 
SELECT rp.`role_id`, rp.`permission_id`, '0', NULL, r.`tenant_id`
FROM `sys_role_permission` rp
INNER JOIN `sys_role` r ON rp.`role_id` = r.`role_id`
WHERE r.`deleted` = '0';

-- =============================================
-- 第五部分：系统配置数据
-- =============================================

-- 基础系统配置
INSERT INTO `sys_config` VALUES 
(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'default', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 'system', NOW(), 'system', NOW(), '0'),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '12345678', 'Y', 'default', '初始化密码 12345678', 'system', NOW(), 'system', NOW(), '0'),
(3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'default', '深色主题theme-dark，浅色主题theme-light', 'system', NOW(), 'system', NOW(), '0'),
(4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'default', '是否开启验证码功能（true开启，false关闭）', 'system', NOW(), 'system', NOW(), '0'),
(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'default', '是否开启注册用户功能（true开启，false关闭）', 'system', NOW(), 'system', NOW(), '0'),
(6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'default', '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）', 'system', NOW(), 'system', NOW(), '0');

-- API加密配置
INSERT INTO `sys_config` VALUES 
(100, 'API加密-启用开关', 'api.encryption.enabled', 'false', 'Y', 'default', '是否启用API加密功能（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(101, 'API加密-加密算法', 'api.encryption.algorithm', 'AES', 'Y', 'default', '加密算法类型（AES、RSA、SM4）', 'system', NOW(), 'system', NOW(), '0'),
(102, 'API加密-AES密钥', 'api.encryption.aes-key', '', 'Y', 'default', 'AES加密密钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(103, 'API加密-AES向量', 'api.encryption.aes-iv', '', 'Y', 'default', 'AES初始化向量IV（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(104, 'API加密-AES模式', 'api.encryption.aes-mode', 'CBC', 'Y', 'default', 'AES加密模式（CBC、ECB、CFB、OFB、CTR）', 'system', NOW(), 'system', NOW(), '0'),
(105, 'API加密-AES填充', 'api.encryption.aes-padding', 'PKCS5Padding', 'Y', 'default', 'AES填充方式（PKCS5Padding、PKCS7Padding、NoPadding）', 'system', NOW(), 'system', NOW(), '0'),
(106, 'API加密-RSA公钥', 'api.encryption.rsa-public-key', '', 'Y', 'default', 'RSA公钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(107, 'API加密-RSA私钥', 'api.encryption.rsa-private-key', '', 'Y', 'default', 'RSA私钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(108, 'API加密-RSA密钥长度', 'api.encryption.rsa-key-size', '2048', 'Y', 'default', 'RSA密钥长度（1024、2048、4096）', 'system', NOW(), 'system', NOW(), '0'),
(109, 'API加密-SM4密钥', 'api.encryption.sm4-key', '', 'Y', 'default', 'SM4加密密钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(110, 'API加密-SM4向量', 'api.encryption.sm4-iv', '', 'Y', 'default', 'SM4初始化向量IV（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(111, 'API加密-SM4模式', 'api.encryption.sm4-mode', 'CBC', 'Y', 'default', 'SM4加密模式（CBC、ECB）', 'system', NOW(), 'system', NOW(), '0'),
(112, 'API加密-包含路径', 'api.encryption.include-patterns', '', 'Y', 'default', '需要加密的接口路径模式（多个用逗号分隔）', 'system', NOW(), 'system', NOW(), '0'),
(113, 'API加密-排除路径', 'api.encryption.exclude-patterns', '', 'Y', 'default', '排除加密的接口路径模式（多个用逗号分隔）', 'system', NOW(), 'system', NOW(), '0'),
(114, 'API加密-加密请求', 'api.encryption.encrypt-request', 'true', 'Y', 'default', '是否加密请求体（true加密，false不加密）', 'system', NOW(), 'system', NOW(), '0'),
(115, 'API加密-加密响应', 'api.encryption.encrypt-response', 'true', 'Y', 'default', '是否加密响应体（true加密，false不加密）', 'system', NOW(), 'system', NOW(), '0');

-- 密码策略配置
INSERT INTO `sys_config` VALUES 
(200, '密码策略-启用开关', 'password.policy.enabled', 'true', 'Y', 'default', '是否启用密码策略（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(201, '密码策略-首次登录修改', 'password.policy.require-change-on-first-login', 'false', 'Y', 'default', '是否要求首次登录修改密码（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(202, '密码策略-过期天数', 'password.policy.expire-days', '0', 'Y', 'default', '密码过期天数（0表示永不过期）', 'system', NOW(), 'system', NOW(), '0'),
(203, '密码策略-提醒天数', 'password.policy.warning-days', '7', 'Y', 'default', '密码过期前提醒天数', 'system', NOW(), 'system', NOW(), '0'),
(204, '密码策略-最大错误次数', 'password.policy.max-error-count', '5', 'Y', 'default', '最大密码错误次数（超过将锁定账号）', 'system', NOW(), 'system', NOW(), '0'),
(205, '密码策略-锁定时长', 'password.policy.lock-duration-minutes', '30', 'Y', 'default', '账号锁定时长（分钟）', 'system', NOW(), 'system', NOW(), '0'),
(206, '密码策略-强密码要求', 'password.policy.strong-password-required', 'false', 'Y', 'default', '是否启用强密码策略（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(207, '密码策略-最小长度', 'password.policy.min-length', '6', 'Y', 'default', '最小密码长度', 'system', NOW(), 'system', NOW(), '0'),
(208, '密码策略-最大长度', 'password.policy.max-length', '20', 'Y', 'default', '最大密码长度', 'system', NOW(), 'system', NOW(), '0'),
(209, '密码策略-要求数字', 'password.policy.require-digits', 'false', 'Y', 'default', '是否要求包含数字（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(210, '密码策略-要求小写字母', 'password.policy.require-lowercase', 'false', 'Y', 'default', '是否要求包含小写字母（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(211, '密码策略-要求大写字母', 'password.policy.require-uppercase', 'false', 'Y', 'default', '是否要求包含大写字母（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(212, '密码策略-要求特殊字符', 'password.policy.require-special-chars', 'false', 'Y', 'default', '是否要求包含特殊字符（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0');

-- 用户活动监控配置
INSERT INTO `sys_config` VALUES 
(300, '活动监控-启用开关', 'activity.monitoring.enabled', 'true', 'Y', 'default', '是否启用用户活动监控（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(301, '活动监控-超时时间', 'activity.monitoring.timeout-seconds', '1800', 'Y', 'default', '用户无活动超时时间（秒），-1表示不限制', 'system', NOW(), 'system', NOW(), '0'),
(302, '活动监控-检查间隔', 'activity.monitoring.check-interval-seconds', '60', 'Y', 'default', '监控检查间隔（秒）', 'system', NOW(), 'system', NOW(), '0'),
(303, '活动监控-API请求监控', 'activity.monitoring.monitor-api-requests', 'true', 'Y', 'default', '是否监控API请求活动（true监控，false不监控）', 'system', NOW(), 'system', NOW(), '0'),
(304, '活动监控-用户交互监控', 'activity.monitoring.monitor-user-interactions', 'true', 'Y', 'default', '是否监控前端用户交互（true监控，false不监控）', 'system', NOW(), 'system', NOW(), '0'),
(305, '活动监控-警告提前时间', 'activity.monitoring.warning-before-timeout-seconds', '300', 'Y', 'default', '活动监控警告提前时间（秒）', 'system', NOW(), 'system', NOW(), '0');

-- =============================================
-- 第六部分：OSS存储默认配置
-- =============================================

-- 插入默认存储配置
INSERT INTO `sys_storage_config` (
    `config_id`, `config_name`, `storage_type`, `bucket_name`, `base_path`, 
    `is_default`, `is_public`, `status`, `max_file_size`, `remark`, `create_by`, `create_time`
) VALUES (
    1, '本地存储', 'local', 'uploads', '/uploads/', 
    '1', '0', '1', 104857600, '默认本地文件存储配置', 'system', NOW()
);

-- 插入默认上传策略
INSERT INTO `sys_upload_policy` (
    `policy_id`, `policy_name`, `policy_code`, `storage_config_id`, 
    `detect_file_type`, `check_file_size`, `max_file_size`, 
    `allowed_extensions`, `check_md5`, `allow_duplicate`, 
    `path_pattern`, `file_name_pattern`, `signature_expires`, 
    `status`, `remark`, `create_by`, `create_time`
) VALUES (
    1, '通用上传策略', 'default', 1, 
    '0', '1', 104857600, 
    'jpg,jpeg,png,gif,bmp,webp,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar', 
    '1', '1', 
    '/{yyyy}/{MM}/{dd}/', '{timestamp}_{random}', 3600, 
    '1', '默认上传策略，适用于大多数文件上传场景', 'system', NOW()
);

INSERT INTO `sys_upload_policy` (
    `policy_id`, `policy_name`, `policy_code`, `storage_config_id`, 
    `detect_file_type`, `check_file_size`, `max_file_size`, 
    `allowed_extensions`, `allowed_mime_types`, `check_md5`, `allow_duplicate`, 
    `generate_thumbnail`, `thumbnail_width`, `thumbnail_height`,
    `path_pattern`, `file_name_pattern`, `signature_expires`, 
    `status`, `remark`, `create_by`, `create_time`
) VALUES (
    2, '图片上传策略', 'image', 1, 
    '1', '1', 10485760, 
    'jpg,jpeg,png,gif,bmp,webp', 
    'image/jpeg,image/png,image/gif,image/bmp,image/webp', 
    '1', '1', 
    '1', 200, 200,
    '/images/{yyyy}/{MM}/{dd}/', '{timestamp}_{random}', 3600, 
    '1', '图片上传策略，启用真实类型检测和缩略图生成', 'system', NOW()
);

-- =============================================
-- 数据脚本完成
-- =============================================
-- 说明：
-- 1. 本脚本包含所有初始化数据，支持RBAC1角色继承功能
-- 2. 默认管理员账号：admin / Passw0rd
-- 3. 默认演示账号：demo / Passw0rd
-- 4. 所有配置项都已初始化，可根据需要修改
-- 5. OSS存储默认使用本地存储，可后续配置云存储
-- 6. RBAC1角色继承功能已初始化，可在Java层实现继承逻辑
-- =============================================
