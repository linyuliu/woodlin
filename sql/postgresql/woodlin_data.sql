-- =============================================
-- Woodlin 多租户中后台管理系统 - 完整初始数据
-- 作者: mumu
-- 描述: 包含所有初始化数据的完整脚本
-- 版本: 2.0.0 (RBAC1支持)
-- 时间: 2025-11-07
-- 说明: 本脚本包含基础数据 + RBAC1初始化 + 字典/行政区划 + OSS默认配置 + 系统配置
-- 前置要求: 请先执行 woodlin_schema.sql 创建表结构
-- =============================================


-- =============================================
-- 第一部分：基础租户和部门数据
-- =============================================

-- 初始化租户数据
INSERT INTO sys_tenant VALUES
('default', '默认租户', 'default', '系统管理员', '13800000000', 'admin@woodlin.com', '1', '2030-12-31 23:59:59', 1000, '系统默认租户', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- 初始化部门数据
INSERT INTO sys_dept VALUES
(100, 0, '0', '木林科技', 'woodlin', 0, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', '总公司', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(101, 100, '0,100', '深圳总公司', 'sz_company', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(102, 100, '0,100', '长沙分公司', 'cs_company', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(103, 101, '0,100,101', '研发部门', 'dev_dept', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(104, 101, '0,100,101', '市场部门', 'market_dept', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(105, 101, '0,100,101', '测试部门', 'test_dept', 3, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(106, 101, '0,100,101', '财务部门', 'finance_dept', 4, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(107, 101, '0,100,101', '运维部门', 'ops_dept', 5, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(108, 102, '0,100,102', '市场部门', 'cs_market_dept', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(109, 102, '0,100,102', '财务部门', 'cs_finance_dept', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 第二部分：用户数据
-- =============================================

-- 初始化用户数据
-- 密码为 Passw0rd，已使用BCrypt加密
INSERT INTO sys_user VALUES (1, 'admin', '超级管理员', '木林',
                             '$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu', 'admin@woodlin.com',
                             '15888888888', '', 1, CURRENT_TIMESTAMP, '1', 'default', 103, CURRENT_TIMESTAMP,
                             '127.0.0.1', 0, 0, NULL, CURRENT_TIMESTAMP, 0, NULL, '管理员', 'system', CURRENT_TIMESTAMP,
                             'system', CURRENT_TIMESTAMP, '0'),
                            (2, 'demo', '演示用户', '演示',
                             '$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu', 'demo@woodlin.com',
                             '15666666666', '', 1, CURRENT_TIMESTAMP, '1', 'default', 105, CURRENT_TIMESTAMP,
                             '127.0.0.1', 0, 0, NULL, CURRENT_TIMESTAMP, 0, NULL, '演示用户', 'system',
                             CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 第三部分：角色和权限数据（包含RBAC1支持）
-- =============================================

-- 初始化角色数据（包含RBAC1字段）
INSERT INTO sys_role VALUES (1, NULL, 0, '/1/', '超级管理员', 'admin', 1, '1', '1', '1', 'default', '超级管理员',
                             'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
                            (2, NULL, 0, '/2/', '普通角色', 'common', 2, '2', '1', '1', 'default', '普通角色', 'system',
                             CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- 初始化角色层次关系（RBAC1）- 每个角色指向自己
INSERT INTO sys_role_hierarchy (ancestor_role_id, descendant_role_id, distance, tenant_id)
VALUES (1, 1, 0, 'default'),
       (2, 2, 0, 'default');

-- 初始化权限数据
INSERT INTO sys_permission VALUES
(1, 0, '系统管理', 'system', 'M', 'system', NULL, 'system', 1, '1', '0', '0', '1', '系统管理目录', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(2, 1, '用户管理', 'system:user', 'C', 'user', 'system/UserView', 'user', 1, '1', '0', '0', '1', '用户管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(3, 1, '角色管理', 'system:role', 'C', 'role', 'system/RoleView', 'peoples', 2, '1', '0', '0', '1', '角色管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(4, 1, '菜单管理', 'system:menu', 'C', 'menu', 'system/permission/PermissionManagementView', 'tree-table', 3, '1', '0', '0', '1', '菜单管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(5, 1, '部门管理', 'system:dept', 'C', 'dept', 'system/DeptView', 'tree', 4, '1', '0', '0', '1', '部门管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 用户管理权限
(100, 2, '用户查询', 'system:user:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(101, 2, '用户新增', 'system:user:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(102, 2, '用户修改', 'system:user:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(103, 2, '用户删除', 'system:user:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(104, 2, '用户导出', 'system:user:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(105, 2, '用户导入', 'system:user:import', 'F', '', '', '#', 6, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(106, 2, '重置密码', 'system:user:resetPwd', 'F', '', '', '#', 7, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 角色管理权限
(200, 3, '角色查询', 'system:role:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(201, 3, '角色新增', 'system:role:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(202, 3, '角色修改', 'system:role:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(203, 3, '角色删除', 'system:role:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(204, 3, '角色导出', 'system:role:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 菜单管理权限
(300, 4, '菜单查询', 'system:menu:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(301, 4, '菜单新增', 'system:menu:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(302, 4, '菜单修改', 'system:menu:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(303, 4, '菜单删除', 'system:menu:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 部门管理权限
(400, 5, '部门查询', 'system:dept:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(401, 5, '部门新增', 'system:dept:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(402, 5, '部门修改', 'system:dept:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(403, 5, '部门删除', 'system:dept:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP,
 'system', CURRENT_TIMESTAMP, '0'),

-- 系统管理-字典管理
(6, 1, '字典管理', 'system:dict', 'C', 'dict', 'system/DictView', 'book-outline', 5, '1', '0', '0', '1', '字典管理菜单',
 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(7, 1, '配置管理', 'system:config', 'C', 'config', 'system/ConfigView', 'options-outline', 6, '1', '0', '0', '1',
 '配置管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(8, 1, '系统设置', 'system:settings', 'C', 'settings', 'system/SystemSettingsView', 'cog-outline', 7, '1', '0', '0',
 '1', '系统设置菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 仪表板
(1000, 0, '仪表板', 'dashboard', 'C', 'dashboard', 'DashboardView', 'dashboard-outline', 1, '1', '0', '0', '1',
 '仪表板页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 数据源管理
(2000, 0, '数据源管理', 'datasource', 'M', 'datasource', NULL, 'server-outline', 2, '1', '0', '0', '1',
 '数据源管理目录', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(2001, 2000, '数据源列表', 'datasource:list', 'C', 'list', 'datasource/DatasourceList', 'list-outline', 1, '1', '0',
 '0', '1', '数据源列表页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(2002, 2000, '数据源监控', 'datasource:monitor', 'C', 'monitor', 'datasource/DatasourceMonitor', 'stats-chart-outline',
 2, '1', '0', '0', '1', '数据源监控页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 租户管理
(3000, 0, '租户管理', 'tenant', 'M', 'tenant', NULL, 'home-outline', 3, '1', '0', '0', '1', '租户管理目录', 'system',
 CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(3001, 3000, '租户列表', 'tenant:list', 'C', 'list', 'tenant/TenantView', 'list-outline', 1, '1', '0', '0', '1',
 '租户列表页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 文件管理
(4000, 0, '文件管理', 'file', 'M', 'file', NULL, 'folder-outline', 4, '1', '0', '0', '1', '文件管理目录', 'system',
 CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(4001, 4000, '文件列表', 'file:list', 'C', 'list', 'file/FileList', 'documents-outline', 1, '1', '0', '0', '1',
 '文件列表页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(4002, 4000, '存储配置', 'file:storage', 'C', 'storage', 'file/FileStorage', 'archive-outline', 2, '1', '0', '0', '1',
 '存储配置页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 任务管理
(5000, 0, '任务管理', 'task', 'M', 'task', NULL, 'timer-outline', 5, '1', '0', '0', '1', '任务管理目录', 'system',
 CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(5001, 5000, '任务列表', 'task:list', 'C', 'list', 'task/TaskList', 'list-outline', 1, '1', '0', '0', '1',
 '任务列表页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(5002, 5000, '任务日志', 'task:log', 'C', 'log', 'task/TaskLog', 'receipt-outline', 2, '1', '0', '0', '1',
 '任务日志页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 开发工具
(6000, 0, '开发工具', 'dev', 'M', 'dev', NULL, 'code-slash-outline', 6, '1', '0', '0', '1', '开发工具目录', 'system',
 CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(6001, 6000, 'SQL转API', 'dev:sql2api', 'C', 'sql2api', 'sql2api/SqlApiEditor', 'terminal-outline', 1, '1', '0', '0',
 '1', 'SQL转API页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(6002, 6000, '代码生成', 'dev:generator', 'C', 'generator', 'dev/CodeGenerator', 'construct-outline', 2, '1', '0', '0',
 '1', '代码生成页面', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 第四部分：用户角色和角色权限关联数据
-- =============================================

-- 初始化用户和角色关联数据
INSERT INTO sys_user_role VALUES (1, 1),
                                 (2, 2);

-- 初始化角色和权限关联数据（直接权限）
INSERT INTO sys_role_permission VALUES
-- 超级管理员拥有所有权限
-- 系统管理模块
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105), (1, 106),
(1, 200), (1, 201), (1, 202), (1, 203), (1, 204),
(1, 300), (1, 301), (1, 302), (1, 303),
(1, 400), (1, 401), (1, 402), (1, 403),
-- 仪表板
(1, 1000),
-- 数据源管理
(1, 2000),
(1, 2001),
(1, 2002),
-- 租户管理
(1, 3000),
(1, 3001),
-- 文件管理
(1, 4000),
(1, 4001),
(1, 4002),
-- 任务管理
(1, 5000),
(1, 5001),
(1, 5002),
-- 开发工具
(1, 6000),
(1, 6001),
(1, 6002),

-- 普通角色只有查询权限
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 100),
(2, 200),
(2, 300),
(2, 400),
-- 仪表板
(2, 1000),
-- 数据源（只看列表）
(2, 2000),
(2, 2001),
-- 租户（只看列表）
(2, 3000),
(2, 3001),
-- 文件（只看列表）
(2, 4000),
(2, 4001),
-- 任务（只看列表）
(2, 5000),
(2, 5001),
-- 开发工具（只看SQL2API）
(2, 6000),
(2, 6001);

-- 初始化角色继承权限缓存（RBAC1）- 包含直接权限
INSERT INTO sys_role_inherited_permission (role_id, permission_id, is_inherited, inherited_from, tenant_id)
SELECT rp.role_id, rp.permission_id, '0', NULL, r.tenant_id
FROM sys_role_permission rp
       INNER JOIN sys_role r ON rp.role_id = r.role_id
WHERE r.deleted = '0';

-- =============================================
-- 第五部分：系统配置数据
-- =============================================

-- 基础系统配置
INSERT INTO sys_config VALUES
(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'default', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '12345678', 'Y', 'default', '初始化密码 12345678', 'system',
 CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'default', '深色主题theme-dark，浅色主题theme-light', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'default', '是否开启验证码功能（true开启，false关闭）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'default', '是否开启注册用户功能（true开启，false关闭）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'default', '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- API加密配置
INSERT INTO sys_config
VALUES (100, 'API加密-启用开关', 'api.encryption.enabled', 'false', 'Y', 'default',
        '是否启用API加密功能（true启用，false禁用）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (101, 'API加密-加密算法', 'api.encryption.algorithm', 'AES', 'Y', 'default', '加密算法类型（AES、RSA、SM4）',
        'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (102, 'API加密-AES密钥', 'api.encryption.aes-key', '', 'Y', 'default', 'AES加密密钥（Base64编码）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (103, 'API加密-AES向量', 'api.encryption.aes-iv', '', 'Y', 'default', 'AES初始化向量IV（Base64编码）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (104, 'API加密-AES模式', 'api.encryption.aes-mode', 'CBC', 'Y', 'default', 'AES加密模式（CBC、ECB、CFB、OFB、CTR）',
        'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (105, 'API加密-AES填充', 'api.encryption.aes-padding', 'PKCS5Padding', 'Y', 'default',
        'AES填充方式（PKCS5Padding、PKCS7Padding、NoPadding）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
        '0'),
       (106, 'API加密-RSA公钥', 'api.encryption.rsa-public-key', '', 'Y', 'default', 'RSA公钥（Base64编码）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (107, 'API加密-RSA私钥', 'api.encryption.rsa-private-key', '', 'Y', 'default', 'RSA私钥（Base64编码）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (108, 'API加密-RSA密钥长度', 'api.encryption.rsa-key-size', '2048', 'Y', 'default',
        'RSA密钥长度（1024、2048、4096）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (109, 'API加密-SM4密钥', 'api.encryption.sm4-key', '', 'Y', 'default', 'SM4加密密钥（Base64编码）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (110, 'API加密-SM4向量', 'api.encryption.sm4-iv', '', 'Y', 'default', 'SM4初始化向量IV（Base64编码）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (111, 'API加密-SM4模式', 'api.encryption.sm4-mode', 'CBC', 'Y', 'default', 'SM4加密模式（CBC、ECB）', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (112, 'API加密-包含路径', 'api.encryption.include-patterns', '', 'Y', 'default',
        '需要加密的接口路径模式（多个用逗号分隔）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (113, 'API加密-排除路径', 'api.encryption.exclude-patterns', '', 'Y', 'default',
        '排除加密的接口路径模式（多个用逗号分隔）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (114, 'API加密-加密请求', 'api.encryption.encrypt-request', 'true', 'Y', 'default',
        '是否加密请求体（true加密，false不加密）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (115, 'API加密-加密响应', 'api.encryption.encrypt-response', 'true', 'Y', 'default',
        '是否加密响应体（true加密，false不加密）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- 密码策略配置
INSERT INTO sys_config
VALUES (200, '密码策略-启用开关', 'password.policy.enabled', 'true', 'Y', 'default',
        '是否启用密码策略（true启用，false禁用）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (201, '密码策略-首次登录修改', 'password.policy.require-change-on-first-login', 'false', 'Y', 'default',
        '是否要求首次登录修改密码（true要求，false不要求）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
        '0'),
       (202, '密码策略-过期天数', 'password.policy.expire-days', '0', 'Y', 'default', '密码过期天数（0表示永不过期）',
        'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (203, '密码策略-提醒天数', 'password.policy.warning-days', '7', 'Y', 'default', '密码过期前提醒天数', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (204, '密码策略-最大错误次数', 'password.policy.max-error-count', '5', 'Y', 'default',
        '最大密码错误次数（超过将锁定账号）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (205, '密码策略-锁定时长', 'password.policy.lock-duration-minutes', '30', 'Y', 'default', '账号锁定时长（分钟）',
        'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (206, '密码策略-强密码要求', 'password.policy.strong-password-required', 'false', 'Y', 'default',
        '是否启用强密码策略（true启用，false禁用）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (207, '密码策略-最小长度', 'password.policy.min-length', '6', 'Y', 'default', '最小密码长度', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (208, '密码策略-最大长度', 'password.policy.max-length', '20', 'Y', 'default', '最大密码长度', 'system',
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (209, '密码策略-要求数字', 'password.policy.require-digits', 'false', 'Y', 'default',
        '是否要求包含数字（true要求，false不要求）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (210, '密码策略-要求小写字母', 'password.policy.require-lowercase', 'false', 'Y', 'default',
        '是否要求包含小写字母（true要求，false不要求）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (211, '密码策略-要求大写字母', 'password.policy.require-uppercase', 'false', 'Y', 'default',
        '是否要求包含大写字母（true要求，false不要求）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (212, '密码策略-要求特殊字符', 'password.policy.require-special-chars', 'false', 'Y', 'default',
        '是否要求包含特殊字符（true要求，false不要求）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- 用户活动监控配置
INSERT INTO sys_config
VALUES (300, '活动监控-启用开关', 'activity.monitoring.enabled', 'true', 'Y', 'default',
        '是否启用用户活动监控（true启用，false禁用）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (301, '活动监控-超时时间', 'activity.monitoring.timeout-seconds', '1800', 'Y', 'default',
        '用户无活动超时时间（秒），-1表示不限制', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (302, '活动监控-检查间隔', 'activity.monitoring.check-interval-seconds', '60', 'Y', 'default',
        '监控检查间隔（秒）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (303, '活动监控-API请求监控', 'activity.monitoring.monitor-api-requests', 'true', 'Y', 'default',
        '是否监控API请求活动（true监控，false不监控）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (304, '活动监控-用户交互监控', 'activity.monitoring.monitor-user-interactions', 'true', 'Y', 'default',
        '是否监控前端用户交互（true监控，false不监控）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
       (305, '活动监控-警告提前时间', 'activity.monitoring.warning-before-timeout-seconds', '300', 'Y', 'default',
        '活动监控警告提前时间（秒）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 第六部分：OSS存储默认配置
-- =============================================

-- 插入默认存储配置
INSERT INTO sys_storage_config (config_id, config_name, storage_type, bucket_name, base_path,
                                is_default, is_public, status, max_file_size, remark, create_by, create_time)
VALUES (1, '本地存储', 'local', 'uploads', '/uploads/',
        '1', '0', '1', 104857600, '默认本地文件存储配置', 'system', CURRENT_TIMESTAMP);

-- 插入默认上传策略
INSERT INTO sys_upload_policy (policy_id, policy_name, policy_code, storage_config_id,
                               detect_file_type, check_file_size, max_file_size,
                               allowed_extensions, check_md5, allow_duplicate,
                               path_pattern, file_name_pattern, signature_expires,
                               status, remark, create_by, create_time)
VALUES (1, '通用上传策略', 'default', 1,
        '0', '1', 104857600,
        'jpg,jpeg,png,gif,bmp,webp,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar',
        '1', '1',
        '/{yyyy}/{MM}/{dd}/', '{timestamp}_{random}', 3600,
        '1', '默认上传策略，适用于大多数文件上传场景', 'system', CURRENT_TIMESTAMP);

INSERT INTO sys_upload_policy (policy_id, policy_name, policy_code, storage_config_id,
                               detect_file_type, check_file_size, max_file_size,
                               allowed_extensions, allowed_mime_types, check_md5, allow_duplicate,
                               generate_thumbnail, thumbnail_width, thumbnail_height,
                               path_pattern, file_name_pattern, signature_expires,
                               status, remark, create_by, create_time)
VALUES (2, '图片上传策略', 'image', 1,
        '1', '1', 10485760,
        'jpg,jpeg,png,gif,bmp,webp',
        'image/jpeg,image/png,image/gif,image/bmp,image/webp',
        '1', '1',
        '1', 200, 200,
        '/images/{yyyy}/{MM}/{dd}/', '{timestamp}_{random}', 3600,
        '1', '图片上传策略，启用真实类型检测和缩略图生成', 'system', CURRENT_TIMESTAMP);

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

-- =============================================
-- 字典类型与字典数据初始化
-- =============================================

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, dict_category, status, remark)
VALUES (1, '性别', 'gender', 'system', '1', 'GB/T 2261.1-2003 标准'),
       (2, '民族', 'ethnicity', 'system', '1', 'GB/T 3304-1991 标准，56个民族'),
       (3, '学历', 'education', 'system', '1', 'GB/T 4658-2006 标准'),
       (4, '婚姻状况', 'marital', 'system', '1', 'GB/T 2261.2-2003 标准'),
       (5, '政治面貌', 'political', 'system', '1', 'GB/T 4762-1984 标准'),
       (6, '证件类型', 'idtype', 'system', '1', 'GB/T 2261.4 标准'),
       (7, '用户状态', 'user_status', 'system', '1', '用户账号状态');

-- 性别
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('gender', '未知的性别', '0', 'GB/T 2261.1-2003标准', 1, '1'),
       ('gender', '男性', '1', 'GB/T 2261.1-2003标准', 2, '1'),
       ('gender', '女性', '2', 'GB/T 2261.1-2003标准', 3, '1'),
       ('gender', '未说明的性别', '9', 'GB/T 2261.1-2003标准', 4, '1');

-- 民族
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('ethnicity', '汉族', '01', 'GB/T 3304-1991', 1, '1'),
       ('ethnicity', '蒙古族', '02', 'GB/T 3304-1991', 2, '1'),
       ('ethnicity', '回族', '03', 'GB/T 3304-1991', 3, '1'),
       ('ethnicity', '藏族', '04', 'GB/T 3304-1991', 4, '1'),
       ('ethnicity', '维吾尔族', '05', 'GB/T 3304-1991', 5, '1'),
       ('ethnicity', '苗族', '06', 'GB/T 3304-1991', 6, '1'),
       ('ethnicity', '彝族', '07', 'GB/T 3304-1991', 7, '1'),
       ('ethnicity', '壮族', '08', 'GB/T 3304-1991', 8, '1'),
       ('ethnicity', '布依族', '09', 'GB/T 3304-1991', 9, '1'),
       ('ethnicity', '朝鲜族', '10', 'GB/T 3304-1991', 10, '1'),
       ('ethnicity', '满族', '11', 'GB/T 3304-1991', 11, '1'),
       ('ethnicity', '侗族', '12', 'GB/T 3304-1991', 12, '1'),
       ('ethnicity', '瑶族', '13', 'GB/T 3304-1991', 13, '1'),
       ('ethnicity', '白族', '14', 'GB/T 3304-1991', 14, '1'),
       ('ethnicity', '土家族', '15', 'GB/T 3304-1991', 15, '1'),
       ('ethnicity', '哈尼族', '16', 'GB/T 3304-1991', 16, '1'),
       ('ethnicity', '哈萨克族', '17', 'GB/T 3304-1991', 17, '1'),
       ('ethnicity', '傣族', '18', 'GB/T 3304-1991', 18, '1'),
       ('ethnicity', '黎族', '19', 'GB/T 3304-1991', 19, '1'),
       ('ethnicity', '傈僳族', '20', 'GB/T 3304-1991', 20, '1'),
       ('ethnicity', '佤族', '21', 'GB/T 3304-1991', 21, '1'),
       ('ethnicity', '畲族', '22', 'GB/T 3304-1991', 22, '1'),
       ('ethnicity', '高山族', '23', 'GB/T 3304-1991', 23, '1'),
       ('ethnicity', '拉祜族', '24', 'GB/T 3304-1991', 24, '1'),
       ('ethnicity', '水族', '25', 'GB/T 3304-1991', 25, '1'),
       ('ethnicity', '东乡族', '26', 'GB/T 3304-1991', 26, '1'),
       ('ethnicity', '纳西族', '27', 'GB/T 3304-1991', 27, '1'),
       ('ethnicity', '景颇族', '28', 'GB/T 3304-1991', 28, '1'),
       ('ethnicity', '柯尔克孜族', '29', 'GB/T 3304-1991', 29, '1'),
       ('ethnicity', '土族', '30', 'GB/T 3304-1991', 30, '1'),
       ('ethnicity', '达斡尔族', '31', 'GB/T 3304-1991', 31, '1'),
       ('ethnicity', '仫佬族', '32', 'GB/T 3304-1991', 32, '1'),
       ('ethnicity', '羌族', '33', 'GB/T 3304-1991', 33, '1'),
       ('ethnicity', '布朗族', '34', 'GB/T 3304-1991', 34, '1'),
       ('ethnicity', '撒拉族', '35', 'GB/T 3304-1991', 35, '1'),
       ('ethnicity', '毛南族', '36', 'GB/T 3304-1991', 36, '1'),
       ('ethnicity', '仡佬族', '37', 'GB/T 3304-1991', 37, '1'),
       ('ethnicity', '锡伯族', '38', 'GB/T 3304-1991', 38, '1'),
       ('ethnicity', '阿昌族', '39', 'GB/T 3304-1991', 39, '1'),
       ('ethnicity', '普米族', '40', 'GB/T 3304-1991', 40, '1'),
       ('ethnicity', '塔吉克族', '41', 'GB/T 3304-1991', 41, '1'),
       ('ethnicity', '怒族', '42', 'GB/T 3304-1991', 42, '1'),
       ('ethnicity', '乌孜别克族', '43', 'GB/T 3304-1991', 43, '1'),
       ('ethnicity', '俄罗斯族', '44', 'GB/T 3304-1991', 44, '1'),
       ('ethnicity', '鄂温克族', '45', 'GB/T 3304-1991', 45, '1'),
       ('ethnicity', '德昂族', '46', 'GB/T 3304-1991', 46, '1'),
       ('ethnicity', '保安族', '47', 'GB/T 3304-1991', 47, '1'),
       ('ethnicity', '裕固族', '48', 'GB/T 3304-1991', 48, '1'),
       ('ethnicity', '京族', '49', 'GB/T 3304-1991', 49, '1'),
       ('ethnicity', '塔塔尔族', '50', 'GB/T 3304-1991', 50, '1'),
       ('ethnicity', '独龙族', '51', 'GB/T 3304-1991', 51, '1'),
       ('ethnicity', '鄂伦春族', '52', 'GB/T 3304-1991', 52, '1'),
       ('ethnicity', '赫哲族', '53', 'GB/T 3304-1991', 53, '1'),
       ('ethnicity', '门巴族', '54', 'GB/T 3304-1991', 54, '1'),
       ('ethnicity', '珞巴族', '55', 'GB/T 3304-1991', 55, '1'),
       ('ethnicity', '基诺族', '56', 'GB/T 3304-1991', 56, '1');

-- 学历
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('education', '研究生', '1', 'GB/T 4658-2006', 1, '1'),
       ('education', '大学本科', '2', 'GB/T 4658-2006', 2, '1'),
       ('education', '大学专科', '3', 'GB/T 4658-2006', 3, '1'),
       ('education', '中等专科', '4', 'GB/T 4658-2006', 4, '1'),
       ('education', '技工学校', '5', 'GB/T 4658-2006', 5, '1'),
       ('education', '高中', '6', 'GB/T 4658-2006', 6, '1'),
       ('education', '初中', '7', 'GB/T 4658-2006', 7, '1'),
       ('education', '小学', '8', 'GB/T 4658-2006', 8, '1'),
       ('education', '文盲或半文盲', '9', 'GB/T 4658-2006', 9, '1'),
       ('education', '其他', '10', 'GB/T 4658-2006', 10, '1');

-- 婚姻状况
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('marital', '未婚', '10', 'GB/T 2261.2-2003', 1, '1'),
       ('marital', '已婚', '20', 'GB/T 2261.2-2003', 2, '1'),
       ('marital', '丧偶', '30', 'GB/T 2261.2-2003', 3, '1'),
       ('marital', '离婚', '40', 'GB/T 2261.2-2003', 4, '1');

-- 政治面貌
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('political', '中共党员', '01', 'GB/T 4762-1984', 1, '1'),
       ('political', '中共预备党员', '02', 'GB/T 4762-1984', 2, '1'),
       ('political', '共青团员', '03', 'GB/T 4762-1984', 3, '1'),
       ('political', '民革会员', '04', 'GB/T 4762-1984', 4, '1'),
       ('political', '民盟盟员', '05', 'GB/T 4762-1984', 5, '1'),
       ('political', '民建会员', '06', 'GB/T 4762-1984', 6, '1'),
       ('political', '民进会员', '07', 'GB/T 4762-1984', 7, '1'),
       ('political', '农工党党员', '08', 'GB/T 4762-1984', 8, '1'),
       ('political', '致公党党员', '09', 'GB/T 4762-1984', 9, '1'),
       ('political', '九三学社社员', '10', 'GB/T 4762-1984', 10, '1'),
       ('political', '台盟盟员', '11', 'GB/T 4762-1984', 11, '1'),
       ('political', '无党派人士', '12', 'GB/T 4762-1984', 12, '1'),
       ('political', '群众', '13', 'GB/T 4762-1984', 13, '1');

-- 证件类型
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('idtype', '居民身份证', '01', 'GB/T 2261.4', 1, '1'),
       ('idtype', '护照', '02', 'GB/T 2261.4', 2, '1'),
       ('idtype', '军官证', '03', 'GB/T 2261.4', 3, '1'),
       ('idtype', '士兵证', '04', 'GB/T 2261.4', 4, '1'),
       ('idtype', '港澳居民来往内地通行证', '05', 'GB/T 2261.4', 5, '1'),
       ('idtype', '台湾居民来往大陆通行证', '06', 'GB/T 2261.4', 6, '1'),
       ('idtype', '外国人永久居留身份证', '07', 'GB/T 2261.4', 7, '1'),
       ('idtype', '其他', '99', 'GB/T 2261.4', 8, '1');

-- 用户状态
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status)
VALUES ('user_status', '正常', '1', '用户账号正常', 1, '1'),
       ('user_status', '停用', '0', '用户账号停用', 2, '1');

-- =============================================
-- 行政区划数据（省级）
-- =============================================

INSERT INTO sys_region (region_code, region_name, parent_code, region_level, region_type, short_name, pinyin,
                        pinyin_abbr, sort_order, is_municipality, status)
VALUES ('110000', '北京市', NULL, 1, 'province', '京', 'Beijing', 'BJ', 1, '1', '1'),
       ('120000', '天津市', NULL, 1, 'province', '津', 'Tianjin', 'TJ', 2, '1', '1'),
       ('130000', '河北省', NULL, 1, 'province', '冀', 'Hebei', 'HE', 3, '0', '1'),
       ('140000', '山西省', NULL, 1, 'province', '晋', 'Shanxi', 'SX', 4, '0', '1'),
       ('150000', '内蒙古自治区', NULL, 1, 'province', '蒙', 'Inner Mongolia', 'NM', 5, '0', '1'),
       ('210000', '辽宁省', NULL, 1, 'province', '辽', 'Liaoning', 'LN', 6, '0', '1'),
       ('220000', '吉林省', NULL, 1, 'province', '吉', 'Jilin', 'JL', 7, '0', '1'),
       ('230000', '黑龙江省', NULL, 1, 'province', '黑', 'Heilongjiang', 'HL', 8, '0', '1'),
       ('310000', '上海市', NULL, 1, 'province', '沪', 'Shanghai', 'SH', 9, '1', '1'),
       ('320000', '江苏省', NULL, 1, 'province', '苏', 'Jiangsu', 'JS', 10, '0', '1'),
       ('330000', '浙江省', NULL, 1, 'province', '浙', 'Zhejiang', 'ZJ', 11, '0', '1'),
       ('340000', '安徽省', NULL, 1, 'province', '皖', 'Anhui', 'AH', 12, '0', '1'),
       ('350000', '福建省', NULL, 1, 'province', '闽', 'Fujian', 'FJ', 13, '0', '1'),
       ('360000', '江西省', NULL, 1, 'province', '赣', 'Jiangxi', 'JX', 14, '0', '1'),
       ('370000', '山东省', NULL, 1, 'province', '鲁', 'Shandong', 'SD', 15, '0', '1'),
       ('410000', '河南省', NULL, 1, 'province', '豫', 'Henan', 'HA', 16, '0', '1'),
       ('420000', '湖北省', NULL, 1, 'province', '鄂', 'Hubei', 'HB', 17, '0', '1'),
       ('430000', '湖南省', NULL, 1, 'province', '湘', 'Hunan', 'HN', 18, '0', '1'),
       ('440000', '广东省', NULL, 1, 'province', '粤', 'Guangdong', 'GD', 19, '0', '1'),
       ('450000', '广西壮族自治区', NULL, 1, 'province', '桂', 'Guangxi', 'GX', 20, '0', '1'),
       ('460000', '海南省', NULL, 1, 'province', '琼', 'Hainan', 'HI', 21, '0', '1'),
       ('500000', '重庆市', NULL, 1, 'province', '渝', 'Chongqing', 'CQ', 22, '1', '1'),
       ('510000', '四川省', NULL, 1, 'province', '川', 'Sichuan', 'SC', 23, '0', '1'),
       ('520000', '贵州省', NULL, 1, 'province', '黔', 'Guizhou', 'GZ', 24, '0', '1'),
       ('530000', '云南省', NULL, 1, 'province', '云', 'Yunnan', 'YN', 25, '0', '1'),
       ('540000', '西藏自治区', NULL, 1, 'province', '藏', 'Tibet', 'XZ', 26, '0', '1'),
       ('610000', '陕西省', NULL, 1, 'province', '陕', 'Shaanxi', 'SN', 27, '0', '1'),
       ('620000', '甘肃省', NULL, 1, 'province', '甘', 'Gansu', 'GS', 28, '0', '1'),
       ('630000', '青海省', NULL, 1, 'province', '青', 'Qinghai', 'QH', 29, '0', '1'),
       ('640000', '宁夏回族自治区', NULL, 1, 'province', '宁', 'Ningxia', 'NX', 30, '0', '1'),
       ('650000', '新疆维吾尔自治区', NULL, 1, 'province', '新', 'Xinjiang', 'XJ', 31, '0', '1'),
       ('710000', '台湾省', NULL, 1, 'province', '台', 'Taiwan', 'TW', 32, '0', '1'),
       ('810000', '香港特别行政区', NULL, 1, 'province', '港', 'Hong Kong', 'HK', 33, '1', '1'),
       ('820000', '澳门特别行政区', NULL, 1, 'province', '澳', 'Macao', 'MO', 34, '1', '1');

-- 同步序列，确保后续自增不冲突
SELECT setval(pg_get_serial_sequence('sys_dict_type', 'dict_id'),
              COALESCE((SELECT MAX(dict_id) FROM sys_dict_type), 1));
SELECT setval(pg_get_serial_sequence('sys_dict_data', 'data_id'),
              COALESCE((SELECT MAX(data_id) FROM sys_dict_data), 1));
SELECT setval(pg_get_serial_sequence('sys_region', 'region_id'), COALESCE((SELECT MAX(region_id) FROM sys_region), 1));
