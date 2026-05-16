-- Woodlin 统一授权中心重做（不包含 ReBAC）
-- 执行前请显式连接 woodlin 库，并先校验：
-- SELECT DATABASE(), @@hostname, @@port;

CREATE TABLE IF NOT EXISTS `auth_role`
(
  `role_id`        bigint(20)   NOT NULL COMMENT '角色ID',
  `parent_role_id` bigint(20)   DEFAULT NULL COMMENT '父角色ID',
  `role_level`     int(11)      DEFAULT 0 COMMENT '角色层级',
  `role_path`      varchar(500) DEFAULT '' COMMENT '角色路径',
  `role_name`      varchar(30)  NOT NULL COMMENT '角色名称',
  `role_code`      varchar(100) NOT NULL COMMENT '角色编码',
  `sort_order`     int(11)      DEFAULT 0 COMMENT '显示顺序',
  `data_scope`     char(1)      DEFAULT '1' COMMENT '数据范围',
  `is_inheritable` char(1)      DEFAULT '1' COMMENT '是否可继承',
  `status`         char(1)      DEFAULT '1' COMMENT '角色状态',
  `tenant_id`      varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`         varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`      varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_auth_role_code` (`role_code`),
  KEY `idx_auth_role_parent` (`parent_role_id`),
  KEY `idx_auth_role_tenant` (`tenant_id`),
  KEY `idx_auth_role_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权角色表';

CREATE TABLE IF NOT EXISTS `auth_permission`
(
  `permission_id`   bigint(20)   NOT NULL COMMENT '权限ID',
  `parent_id`       bigint(20)   DEFAULT 0 COMMENT '父权限ID',
  `permission_name` varchar(50)  NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `permission_type` char(1)      DEFAULT 'M' COMMENT '权限类型',
  `resource_type`   varchar(50)  DEFAULT NULL COMMENT '资源类型',
  `resource_id`     varchar(255) DEFAULT NULL COMMENT '资源ID或路径',
  `path`            varchar(200) DEFAULT '' COMMENT '路由地址',
  `component`       varchar(255) DEFAULT NULL COMMENT '组件路径',
  `icon`            varchar(100) DEFAULT '#' COMMENT '权限图标',
  `sort_order`      int(11)      DEFAULT 0 COMMENT '显示顺序',
  `status`          char(1)      DEFAULT '1' COMMENT '权限状态',
  `is_frame`        char(1)      DEFAULT '0' COMMENT '是否为外链',
  `is_cache`        char(1)      DEFAULT '0' COMMENT '是否缓存',
  `visible`         char(1)      DEFAULT '1' COMMENT '显示状态',
  `show_in_tabs`    char(1)      DEFAULT '1' COMMENT '是否在标签页显示',
  `active_menu`     varchar(255) DEFAULT NULL COMMENT '高亮菜单路径',
  `redirect`        varchar(255) DEFAULT NULL COMMENT '重定向路径',
  `tenant_id`       varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`          varchar(500) DEFAULT '' COMMENT '备注',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`permission_id`),
  KEY `idx_auth_permission_parent` (`parent_id`),
  KEY `idx_auth_permission_code` (`permission_code`),
  KEY `idx_auth_permission_resource` (`resource_type`, `resource_id`),
  KEY `idx_auth_permission_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权权限表';

CREATE TABLE IF NOT EXISTS `auth_user_role`
(
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `idx_auth_user_role_role` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权用户角色关联表';

CREATE TABLE IF NOT EXISTS `auth_role_permission`
(
  `role_id`       bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_auth_role_permission_permission` (`permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权角色权限关联表';

CREATE TABLE IF NOT EXISTS `auth_role_hierarchy`
(
  `ancestor_role_id`   bigint(20) NOT NULL COMMENT '祖先角色ID',
  `descendant_role_id` bigint(20) NOT NULL COMMENT '后代角色ID',
  `distance`           int(11)    NOT NULL DEFAULT 0 COMMENT '层级距离',
  `tenant_id`          varchar(64)         DEFAULT NULL COMMENT '租户ID',
  `create_time`        datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`ancestor_role_id`, `descendant_role_id`),
  KEY `idx_auth_role_hierarchy_descendant` (`descendant_role_id`),
  KEY `idx_auth_role_hierarchy_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权角色层级闭包表';

CREATE TABLE IF NOT EXISTS `auth_role_dept`
(
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权角色部门关联表';

CREATE TABLE IF NOT EXISTS `auth_policy`
(
  `policy_id`   bigint(20)   NOT NULL COMMENT '策略ID',
  `policy_code` varchar(100) NOT NULL COMMENT '策略编码',
  `policy_name` varchar(100) NOT NULL COMMENT '策略名称',
  `policy_type` varchar(50)  NOT NULL COMMENT '策略类型',
  `priority`    int(11)      DEFAULT 0 COMMENT '优先级',
  `effect`      varchar(20)  NOT NULL DEFAULT 'DENY' COMMENT 'ALLOW/DENY',
  `policy_json` json         NOT NULL COMMENT '策略JSON',
  `version`     int(11)      DEFAULT 1 COMMENT '版本号',
  `enabled`     char(1)      DEFAULT '1' COMMENT '是否启用',
  `tenant_id`   varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`   varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `uk_auth_policy_code` (`policy_code`),
  KEY `idx_auth_policy_type_enabled` (`policy_type`, `enabled`),
  KEY `idx_auth_policy_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='授权策略表';

CREATE TABLE IF NOT EXISTS `auth_capability`
(
  `capability_id`      bigint(20)   NOT NULL COMMENT '能力ID',
  `capability_code`    varchar(100) NOT NULL COMMENT '能力编码',
  `capability_name`    varchar(100) NOT NULL COMMENT '能力名称',
  `resource_type`      varchar(50)  NOT NULL COMMENT '资源类型',
  `resource_pattern`   varchar(255) NOT NULL COMMENT '资源模式',
  `enabled`            char(1)      DEFAULT '1' COMMENT '是否启用',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`capability_id`),
  UNIQUE KEY `uk_auth_capability_code` (`capability_code`),
  KEY `idx_auth_capability_resource` (`resource_type`, `resource_pattern`),
  KEY `idx_auth_capability_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放能力表';

CREATE TABLE IF NOT EXISTS `auth_scope`
(
  `scope_id`      bigint(20)   NOT NULL COMMENT '范围ID',
  `capability_id` bigint(20)   NOT NULL COMMENT '能力ID',
  `scope_code`    varchar(100) NOT NULL COMMENT '范围编码',
  `scope_name`    varchar(100) NOT NULL COMMENT '范围名称',
  `actions`       varchar(500) DEFAULT '*' COMMENT '动作集合，逗号分隔',
  `enabled`       char(1)      DEFAULT '1' COMMENT '是否启用',
  `tenant_id`     varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`scope_id`),
  UNIQUE KEY `uk_auth_scope_code` (`scope_code`),
  KEY `idx_auth_scope_capability` (`capability_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放范围表';

CREATE TABLE IF NOT EXISTS `auth_subject_grant`
(
  `grant_id`      bigint(20)  NOT NULL COMMENT '授权ID',
  `subject_type`  varchar(50) NOT NULL COMMENT '主体类型',
  `subject_id`    varchar(64) NOT NULL COMMENT '主体ID',
  `capability_id` bigint(20)  NOT NULL COMMENT '能力ID',
  `scope_id`      bigint(20)  NOT NULL COMMENT '范围ID',
  `status`        char(1)     DEFAULT '1' COMMENT '授权状态',
  `tenant_id`     varchar(64) DEFAULT NULL COMMENT '租户ID',
  `create_by`     varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)     DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`grant_id`),
  UNIQUE KEY `uk_auth_subject_grant` (`subject_type`, `subject_id`, `capability_id`, `scope_id`),
  KEY `idx_auth_subject_grant_subject` (`subject_type`, `subject_id`),
  KEY `idx_auth_subject_grant_scope` (`scope_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='主体能力授权表';

CREATE TABLE IF NOT EXISTS `auth_quota_policy`
(
  `quota_id`       bigint(20)  NOT NULL COMMENT '限额ID',
  `subject_type`   varchar(50) NOT NULL COMMENT '主体类型',
  `subject_id`     varchar(64) NOT NULL COMMENT '主体ID',
  `capability_id`  bigint(20)  DEFAULT NULL COMMENT '能力ID',
  `scope_id`       bigint(20)  DEFAULT NULL COMMENT '范围ID',
  `window_seconds` int(11)     NOT NULL COMMENT '窗口秒数',
  `limit_count`    bigint(20)  NOT NULL COMMENT '限制次数',
  `enabled`        char(1)     DEFAULT '1' COMMENT '是否启用',
  `tenant_id`      varchar(64) DEFAULT NULL COMMENT '租户ID',
  `create_by`      varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)     DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`quota_id`),
  KEY `idx_auth_quota_subject` (`subject_type`, `subject_id`),
  KEY `idx_auth_quota_capability_scope` (`capability_id`, `scope_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='限额策略表';

CREATE TABLE IF NOT EXISTS `auth_quota_usage`
(
  `usage_id`       bigint(20)  NOT NULL COMMENT '用量ID',
  `quota_id`       bigint(20)  NOT NULL COMMENT '限额ID',
  `subject_type`   varchar(50) NOT NULL COMMENT '主体类型',
  `subject_id`     varchar(64) NOT NULL COMMENT '主体ID',
  `window_start`   datetime    NOT NULL COMMENT '窗口开始时间',
  `window_seconds` int(11)     NOT NULL COMMENT '窗口秒数',
  `used_count`     bigint(20)  DEFAULT 0 COMMENT '已使用次数',
  `tenant_id`      varchar(64) DEFAULT NULL COMMENT '租户ID',
  `create_time`    datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`usage_id`),
  UNIQUE KEY `uk_auth_quota_usage_window` (`quota_id`, `subject_type`, `subject_id`, `window_start`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='限额用量审计表';

INSERT INTO `auth_role`
SELECT `role_id`, `parent_role_id`, `role_level`, `role_path`, `role_name`, `role_code`, `sort_order`,
       `data_scope`, `is_inheritable`, `status`, `tenant_id`, `remark`, `create_by`, `create_time`,
       `update_by`, `update_time`, `deleted`
FROM `sys_role`
ON DUPLICATE KEY UPDATE
  `parent_role_id` = VALUES(`parent_role_id`),
  `role_level` = VALUES(`role_level`),
  `role_path` = VALUES(`role_path`),
  `role_name` = VALUES(`role_name`),
  `role_code` = VALUES(`role_code`),
  `sort_order` = VALUES(`sort_order`),
  `data_scope` = VALUES(`data_scope`),
  `is_inheritable` = VALUES(`is_inheritable`),
  `status` = VALUES(`status`),
  `tenant_id` = VALUES(`tenant_id`),
  `remark` = VALUES(`remark`),
  `update_by` = VALUES(`update_by`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

SET @show_in_tabs_expr = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_permission'
      AND COLUMN_NAME = 'show_in_tabs'
  ),
  '`show_in_tabs`',
  '''1'''
);
SET @active_menu_expr = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_permission'
      AND COLUMN_NAME = 'active_menu'
  ),
  '`active_menu`',
  'NULL'
);
SET @redirect_expr = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_permission'
      AND COLUMN_NAME = 'redirect'
  ),
  '`redirect`',
  'NULL'
);
SET @auth_permission_insert_sql = CONCAT(
  'INSERT INTO `auth_permission` ',
  '(`permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `resource_type`, ',
  '`resource_id`, `path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`, `visible`, ',
  '`show_in_tabs`, `active_menu`, `redirect`, `tenant_id`, `remark`, `create_by`, `create_time`, `update_by`, ',
  '`update_time`, `deleted`) ',
  'SELECT `permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, ',
  'CASE WHEN `permission_type` IN (''M'', ''C'') THEN ''menu'' ELSE ''permission'' END, ',
  'CASE WHEN `permission_type` IN (''M'', ''C'') THEN `path` ELSE `permission_code` END, ',
  '`path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`, `visible`, ',
  @show_in_tabs_expr, ', ', @active_menu_expr, ', ', @redirect_expr,
  ', NULL, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted` ',
  'FROM `sys_permission` ',
  'ON DUPLICATE KEY UPDATE ',
  '`parent_id` = VALUES(`parent_id`), ',
  '`permission_name` = VALUES(`permission_name`), ',
  '`permission_code` = VALUES(`permission_code`), ',
  '`permission_type` = VALUES(`permission_type`), ',
  '`resource_type` = VALUES(`resource_type`), ',
  '`resource_id` = VALUES(`resource_id`), ',
  '`path` = VALUES(`path`), ',
  '`component` = VALUES(`component`), ',
  '`icon` = VALUES(`icon`), ',
  '`sort_order` = VALUES(`sort_order`), ',
  '`status` = VALUES(`status`), ',
  '`is_frame` = VALUES(`is_frame`), ',
  '`is_cache` = VALUES(`is_cache`), ',
  '`visible` = VALUES(`visible`), ',
  '`show_in_tabs` = VALUES(`show_in_tabs`), ',
  '`active_menu` = VALUES(`active_menu`), ',
  '`redirect` = VALUES(`redirect`), ',
  '`remark` = VALUES(`remark`), ',
  '`update_by` = VALUES(`update_by`), ',
  '`update_time` = VALUES(`update_time`), ',
  '`deleted` = VALUES(`deleted`)'
);
PREPARE auth_permission_insert_stmt FROM @auth_permission_insert_sql;
EXECUTE auth_permission_insert_stmt;
DEALLOCATE PREPARE auth_permission_insert_stmt;

DELETE ap
FROM `auth_permission` ap
LEFT JOIN `sys_permission` sp ON sp.permission_id = ap.permission_id
WHERE sp.permission_id IS NULL
  AND ap.permission_id NOT IN (2800000000000000000, 2800000000000000001);

INSERT IGNORE INTO `auth_user_role`
SELECT `user_id`, `role_id`
FROM `sys_user_role`;

DELETE aur
FROM `auth_user_role` aur
LEFT JOIN `sys_user_role` sur ON sur.user_id = aur.user_id AND sur.role_id = aur.role_id
WHERE sur.user_id IS NULL;

INSERT IGNORE INTO `auth_role_permission`
SELECT `role_id`, `permission_id`
FROM `sys_role_permission`;

DELETE rp
FROM `auth_role_permission` rp
LEFT JOIN `sys_role_permission` srp ON srp.role_id = rp.role_id AND srp.permission_id = rp.permission_id
WHERE srp.role_id IS NULL
  AND rp.permission_id NOT IN (2800000000000000000, 2800000000000000001);

DELETE rp
FROM `auth_role_permission` rp
LEFT JOIN `auth_role` r ON r.role_id = rp.role_id
LEFT JOIN `auth_permission` p ON p.permission_id = rp.permission_id
WHERE r.role_id IS NULL
   OR p.permission_id IS NULL;

INSERT IGNORE INTO `auth_role_hierarchy`
SELECT `ancestor_role_id`, `descendant_role_id`, `distance`, `tenant_id`, `create_time`
FROM `sys_role_hierarchy`;

INSERT IGNORE INTO `auth_role_hierarchy`
(`ancestor_role_id`, `descendant_role_id`, `distance`, `tenant_id`, `create_time`)
SELECT `role_id`, `role_id`, 0, `tenant_id`, NOW()
FROM `auth_role`;

INSERT IGNORE INTO `auth_role_dept`
SELECT `role_id`, `dept_id`
FROM `sys_role_dept`;

INSERT IGNORE INTO `auth_permission`
(`permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `resource_type`,
 `resource_id`, `path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`, `visible`,
 `show_in_tabs`, `active_menu`, `redirect`, `tenant_id`, `remark`, `create_by`, `create_time`, `update_by`,
 `update_time`, `deleted`)
VALUES
(2800000000000000000, 0, '授权中心', 'authorization:center:view', 'C', 'menu', '/authorization',
 '/authorization', 'authorization/index', 'vicons:antd:SafetyCertificateOutlined', 900, '1', '0', '0', '1',
 '1', NULL, NULL, NULL, '统一授权中心入口', 'system', NOW(), 'system', NOW(), '0'),
(2800000000000000001, 2800000000000000000, '决策测试', 'authorization:decision:test', 'F', 'permission',
 'authorization:decision:test', '', NULL, '#', 1, '1', '0', '0', '1', '1', NULL, NULL, NULL,
 '授权决策测试器', 'system', NOW(), 'system', NOW(), '0');

INSERT IGNORE INTO `auth_role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `auth_role` r
INNER JOIN `auth_permission` p ON p.permission_id IN (2800000000000000000, 2800000000000000001)
WHERE r.role_code = 'admin';

INSERT IGNORE INTO `auth_policy`
(`policy_id`, `policy_code`, `policy_name`, `policy_type`, `priority`, `effect`, `policy_json`, `version`,
 `enabled`, `tenant_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000100, 'policy.super-admin.allow', '超级管理员兜底允许', 'RBAC', 1000, 'ALLOW',
 JSON_OBJECT('subject', JSON_OBJECT('type', 'user'), 'actions', JSON_ARRAY('*:*:*')), 1, '0', NULL,
 '示例 JSON 策略，实际超级管理员仍由 auth_role 判断', 'system', NOW(), 'system', NOW(), '0');

INSERT IGNORE INTO `auth_capability`
(`capability_id`, `capability_code`, `capability_name`, `resource_type`, `resource_pattern`, `enabled`,
 `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000200, 'openapi.all', '开放接口默认能力', 'api', '/openapi/*', '1',
 NULL, 'system', NOW(), 'system', NOW(), '0');

INSERT IGNORE INTO `auth_scope`
(`scope_id`, `capability_id`, `scope_code`, `scope_name`, `actions`, `enabled`, `tenant_id`, `create_by`,
 `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000201, 2800000000000000200, 'openapi.all:*', '开放接口全部动作', '*', '1', NULL,
 'system', NOW(), 'system', NOW(), '0');

SET @auth_subject_grant_seed_sql = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_open_app'
  ),
  'INSERT IGNORE INTO `auth_subject_grant`
   (`grant_id`, `subject_type`, `subject_id`, `capability_id`, `scope_id`, `status`, `tenant_id`, `create_by`,
    `create_time`, `update_by`, `update_time`, `deleted`)
   SELECT 2800000000001000000 + ROW_NUMBER() OVER (ORDER BY app_id),
          ''app'', CAST(app_id AS CHAR), 2800000000000000200, 2800000000000000201, ''1'', tenant_id,
          ''system'', NOW(), ''system'', NOW(), ''0''
   FROM `sys_open_app`
   WHERE deleted = ''0''',
  'SELECT 1'
);
PREPARE auth_subject_grant_seed_stmt FROM @auth_subject_grant_seed_sql;
EXECUTE auth_subject_grant_seed_stmt;
DEALLOCATE PREPARE auth_subject_grant_seed_stmt;

SET @auth_quota_policy_seed_sql = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_open_app'
  ),
  'INSERT IGNORE INTO `auth_quota_policy`
   (`quota_id`, `subject_type`, `subject_id`, `capability_id`, `scope_id`, `window_seconds`, `limit_count`,
    `enabled`, `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
   SELECT 2800000000002000000 + ROW_NUMBER() OVER (ORDER BY app_id),
          ''app'', CAST(app_id AS CHAR), 2800000000000000200, 2800000000000000201, 60, 600,
          ''1'', tenant_id, ''system'', NOW(), ''system'', NOW(), ''0''
   FROM `sys_open_app`
   WHERE deleted = ''0''',
  'SELECT 1'
);
PREPARE auth_quota_policy_seed_stmt FROM @auth_quota_policy_seed_sql;
EXECUTE auth_quota_policy_seed_stmt;
DEALLOCATE PREPARE auth_quota_policy_seed_stmt;
