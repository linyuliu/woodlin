-- =============================================
-- Woodlin 多租户中后台管理系统初始数据 (PostgreSQL)
-- 作者: mumu
-- 描述: PostgreSQL 系统初始化数据脚本
-- 版本: 1.0.0
-- 时间: 2025-01-01
-- 数据库: PostgreSQL 12+
-- 说明: 从 MySQL 转换而来，包含系统启动必需的基础数据
-- =============================================

-- 连接到 woodlin 数据库
-- \c woodlin;

-- =============================================
-- 初始化租户数据
-- 说明: 创建默认租户，用于单租户或多租户系统的初始化
-- =============================================
INSERT INTO sys_tenant VALUES 
('default', '默认租户', 'default', '系统管理员', '13800000000', 'admin@woodlin.com', '1', '2030-12-31 23:59:59', 1000, '系统默认租户', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 初始化部门数据
-- 说明: 创建组织架构树形结构，包含总公司和各部门
-- ancestors 字段存储祖先路径，便于快速查询层级关系
-- =============================================
INSERT INTO sys_dept VALUES 
-- 根部门：木林科技
(100, 0, '0', '木林科技', 'woodlin', 0, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', '总公司', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 一级部门：总公司和分公司
(101, 100, '0,100', '深圳总公司', 'sz_company', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(102, 100, '0,100', '长沙分公司', 'cs_company', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 二级部门：深圳总公司下的各职能部门
(103, 101, '0,100,101', '研发部门', 'dev_dept', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(104, 101, '0,100,101', '市场部门', 'market_dept', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(105, 101, '0,100,101', '测试部门', 'test_dept', 3, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(106, 101, '0,100,101', '财务部门', 'finance_dept', 4, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(107, 101, '0,100,101', '运维部门', 'ops_dept', 5, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 二级部门：长沙分公司下的职能部门
(108, 102, '0,100,102', '市场部门', 'cs_market_dept', 1, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(109, 102, '0,100,102', '财务部门', 'cs_finance_dept', 2, '木林', '15888888888', 'woodlin@qq.com', '1', 'default', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 初始化用户数据
-- 说明: 创建系统管理员和演示用户
-- 密码: Passw0rd (BCrypt加密后的值)
-- =============================================
INSERT INTO sys_user VALUES 
-- 超级管理员账号
(1, 'admin', '超级管理员', '木林', '$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu', 'admin@woodlin.com', '15888888888', '', 1, CURRENT_TIMESTAMP, '1', 'default', 103, CURRENT_TIMESTAMP, '127.0.0.1', 0, 0, NULL, '管理员', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 演示用户账号
(2, 'demo', '演示用户', '演示', '$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu', 'demo@woodlin.com', '15666666666', '', 1, CURRENT_TIMESTAMP, '1', 'default', 105, CURRENT_TIMESTAMP, '127.0.0.1', 0, 0, NULL, '演示用户', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 初始化角色数据
-- 说明: 创建系统默认角色
-- data_scope: 数据权限范围（1-全部数据权限）
-- =============================================
INSERT INTO sys_role VALUES 
-- 超级管理员角色：拥有所有权限
(1, '超级管理员', 'admin', 1, '1', '1', 'default', '超级管理员', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 普通角色：基础权限
(2, '普通角色', 'common', 2, '2', '1', 'default', '普通角色', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 初始化权限数据
-- 说明: 创建系统菜单和权限点
-- permission_type: M-目录，C-菜单，F-按钮
-- =============================================
INSERT INTO sys_permission VALUES 
-- 一级菜单：系统管理
(1, 0, '系统管理', 'system', 'M', 'system', NULL, 'system', 1, '1', '0', '0', '1', '系统管理目录', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 二级菜单：系统管理下的各个功能模块
(2, 1, '用户管理', 'system:user', 'C', 'user', 'system/user/index', 'user', 1, '1', '0', '0', '1', '用户管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(3, 1, '角色管理', 'system:role', 'C', 'role', 'system/role/index', 'peoples', 2, '1', '0', '0', '1', '角色管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(4, 1, '菜单管理', 'system:menu', 'C', 'menu', 'system/menu/index', 'tree-table', 3, '1', '0', '0', '1', '菜单管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(5, 1, '部门管理', 'system:dept', 'C', 'dept', 'system/dept/index', 'tree', 4, '1', '0', '0', '1', '部门管理菜单', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 用户管理的按钮权限
(100, 2, '用户查询', 'system:user:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(101, 2, '用户新增', 'system:user:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(102, 2, '用户修改', 'system:user:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(103, 2, '用户删除', 'system:user:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(104, 2, '用户导出', 'system:user:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(105, 2, '用户导入', 'system:user:import', 'F', '', '', '#', 6, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(106, 2, '重置密码', 'system:user:resetPwd', 'F', '', '', '#', 7, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 角色管理的按钮权限
(200, 3, '角色查询', 'system:role:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(201, 3, '角色新增', 'system:role:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(202, 3, '角色修改', 'system:role:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(203, 3, '角色删除', 'system:role:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(204, 3, '角色导出', 'system:role:export', 'F', '', '', '#', 5, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 菜单管理的按钮权限
(300, 4, '菜单查询', 'system:menu:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(301, 4, '菜单新增', 'system:menu:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(302, 4, '菜单修改', 'system:menu:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(303, 4, '菜单删除', 'system:menu:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),

-- 部门管理的按钮权限
(400, 5, '部门查询', 'system:dept:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(401, 5, '部门新增', 'system:dept:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(402, 5, '部门修改', 'system:dept:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(403, 5, '部门删除', 'system:dept:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 初始化用户和角色关联数据
-- 说明: 建立用户与角色的多对多关系
-- =============================================
INSERT INTO sys_user_role VALUES 
(1, 1),  -- admin 用户 -> 超级管理员角色
(2, 2);  -- demo 用户 -> 普通角色

-- =============================================
-- 初始化角色和权限关联数据
-- 说明: 建立角色与权限的多对多关系
-- =============================================
INSERT INTO sys_role_permission VALUES 
-- 超级管理员拥有所有权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105), (1, 106),
(1, 200), (1, 201), (1, 202), (1, 203), (1, 204),
(1, 300), (1, 301), (1, 302), (1, 303),
(1, 400), (1, 401), (1, 402), (1, 403),

-- 普通角色只有查询权限
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(2, 100), (2, 200), (2, 300), (2, 400);

-- =============================================
-- 初始化系统配置
-- 说明: 设置系统默认配置参数
-- config_type: Y-系统内置，N-用户自定义
-- =============================================
INSERT INTO sys_config VALUES 
(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'default', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'default', '初始化密码 123456', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'default', '深色主题theme-dark，浅色主题theme-light', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'default', '是否开启验证码功能（true开启，false关闭）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'default', '是否开启注册用户功能（true开启，false关闭）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0'),
(6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'default', '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, '0');

-- =============================================
-- 脚本执行完成
-- =============================================
-- 说明:
-- 1. 本脚本包含系统启动所需的最小数据集
-- 2. 默认管理员账号: admin / Passw0rd
-- 3. 演示账号: demo / Passw0rd
-- 4. 所有时间戳使用 CURRENT_TIMESTAMP
-- 5. 建议在生产环境修改默认密码
-- =============================================
