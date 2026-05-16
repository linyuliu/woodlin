-- Woodlin OpenAPI AppId 授权改造
-- 执行前请显式连接 woodlin 库，并先校验：
-- SELECT DATABASE(), @@hostname, @@port;

CREATE TABLE IF NOT EXISTS `sys_open_client`
(
  `client_id`     bigint(20)   NOT NULL COMMENT '客户ID',
  `client_code`   varchar(100) NOT NULL COMMENT '客户编码',
  `client_name`   varchar(100) NOT NULL COMMENT '客户名称',
  `tenant_id`     varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `owner_user_id` bigint(20)   DEFAULT NULL COMMENT '负责人用户ID',
  `owner_dept_id` bigint(20)   DEFAULT NULL COMMENT '负责部门ID',
  `owner_name`    varchar(100) DEFAULT NULL COMMENT '负责人',
  `status`        char(1)      DEFAULT '1' COMMENT '状态',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`client_id`),
  UNIQUE KEY `uk_sys_open_client_code` (`client_code`),
  KEY `idx_sys_open_client_tenant` (`tenant_id`),
  KEY `idx_sys_open_client_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放平台客户表';

CREATE TABLE IF NOT EXISTS `sys_open_app`
(
  `app_id`        bigint(20)   NOT NULL COMMENT '应用ID',
  `client_id`     bigint(20)   DEFAULT NULL COMMENT '客户ID',
  `app_code`      varchar(100) NOT NULL COMMENT '应用编码',
  `app_name`      varchar(100) NOT NULL COMMENT '应用名称',
  `status`        char(1)      DEFAULT '1' COMMENT '应用状态',
  `tenant_id`     varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `region_code`   varchar(100) DEFAULT NULL COMMENT '地区编码',
  `region_name`   varchar(100) DEFAULT NULL COMMENT '地区名称',
  `owner_user_id` bigint(20)   DEFAULT NULL COMMENT '负责人用户ID',
  `owner_dept_id` bigint(20)   DEFAULT NULL COMMENT '负责部门ID',
  `owner_name`    varchar(100) DEFAULT NULL COMMENT '负责人',
  `ip_whitelist`  varchar(500) DEFAULT NULL COMMENT 'IP白名单',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `uk_sys_open_app_code` (`app_code`),
  KEY `idx_sys_open_app_client` (`client_id`),
  KEY `idx_sys_open_app_tenant` (`tenant_id`),
  KEY `idx_sys_open_app_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放应用表';

CREATE TABLE IF NOT EXISTS `sys_open_app_credential`
(
  `credential_id`              bigint(20)   NOT NULL COMMENT '凭证ID',
  `app_id`                     bigint(20)   NOT NULL COMMENT '应用ID',
  `credential_name`            varchar(100) NOT NULL COMMENT '凭证名称',
  `credential_type`            varchar(20)  DEFAULT 'AKSK' COMMENT '凭证类型',
  `access_key`                 varchar(100) DEFAULT NULL COMMENT '访问密钥',
  `app_key_hash`               varchar(128) DEFAULT NULL COMMENT 'AppKey哈希',
  `secret_key_encrypted`       text         DEFAULT NULL COMMENT '加密存储的密钥',
  `secret_key_fingerprint`     varchar(64)  DEFAULT NULL COMMENT '密钥指纹',
  `signature_public_key`       text         DEFAULT NULL COMMENT '签名公钥',
  `encryption_public_key`      text         DEFAULT NULL COMMENT '客户端加密公钥',
  `server_public_key`          text         DEFAULT NULL COMMENT '服务端加密公钥',
  `server_private_key_encrypted` text       DEFAULT NULL COMMENT '服务端私钥密文',
  `signature_algorithm`        varchar(50)  DEFAULT NULL COMMENT '签名算法',
  `encryption_algorithm`       varchar(50)  DEFAULT NULL COMMENT '加密算法',
  `security_mode`              varchar(50)  DEFAULT 'AKSK' COMMENT '安全模式',
  `active_from`                datetime     DEFAULT NULL COMMENT '生效时间',
  `active_to`                  datetime     DEFAULT NULL COMMENT '失效时间',
  `last_rotated_time`          datetime     DEFAULT NULL COMMENT '最近轮换时间',
  `last_used_time`             datetime     DEFAULT NULL COMMENT '最近使用时间',
  `status`                     char(1)      DEFAULT '1' COMMENT '凭证状态',
  `remark`                     varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`                  varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`                datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`                  varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`                datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                    char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`credential_id`),
  UNIQUE KEY `uk_sys_open_app_credential_ak` (`access_key`),
  UNIQUE KEY `uk_sys_open_app_credential_app_key` (`app_key_hash`),
  KEY `idx_sys_open_app_credential_app` (`app_id`),
  KEY `idx_sys_open_app_credential_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放应用凭证表';

CREATE TABLE IF NOT EXISTS `sys_open_api_policy`
(
  `policy_id`                bigint(20)   NOT NULL COMMENT '策略ID',
  `policy_name`              varchar(100) NOT NULL COMMENT '策略名称',
  `path_pattern`             varchar(255) NOT NULL COMMENT '路径模式',
  `http_method`              varchar(20)  NOT NULL COMMENT 'HTTP方法',
  `security_mode`            varchar(50)  DEFAULT NULL COMMENT '安全模式',
  `signature_algorithm`      varchar(50)  DEFAULT NULL COMMENT '签名算法',
  `encryption_algorithm`     varchar(50)  DEFAULT NULL COMMENT '加密算法',
  `timestamp_window_seconds` int(11)      DEFAULT NULL COMMENT '时间窗秒数',
  `nonce_enabled`            char(1)      DEFAULT NULL COMMENT '是否启用nonce',
  `nonce_ttl_seconds`        int(11)      DEFAULT NULL COMMENT 'nonce TTL秒数',
  `tenant_required`          char(1)      DEFAULT '0' COMMENT '是否要求租户',
  `enabled`                  char(1)      DEFAULT '1' COMMENT '是否启用',
  `remark`                   varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`                varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`              datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`                varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`              datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                  char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `uk_sys_open_api_policy_path_method` (`path_pattern`, `http_method`),
  KEY `idx_sys_open_api_policy_enabled` (`enabled`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放API安全策略表';

CREATE TABLE IF NOT EXISTS `auth_open_api_resource`
(
  `resource_id`    bigint(20)   NOT NULL COMMENT '开放接口资源ID',
  `resource_code`  varchar(100) NOT NULL COMMENT '资源编码',
  `resource_name`  varchar(100) NOT NULL COMMENT '资源名称',
  `http_method`    varchar(20)  NOT NULL COMMENT 'HTTP方法',
  `path_pattern`   varchar(255) NOT NULL COMMENT '路径模式',
  `capability_id`  bigint(20)   NOT NULL COMMENT '能力ID',
  `scope_id`       bigint(20)   NOT NULL COMMENT '默认Scope ID',
  `auth_mode`      varchar(50)  DEFAULT 'AKSK' COMMENT '默认认证模式',
  `status`         char(1)      DEFAULT '1' COMMENT '状态',
  `tenant_id`      varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`         varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`      varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)      DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`resource_id`),
  UNIQUE KEY `uk_auth_open_api_resource_code` (`resource_code`),
  UNIQUE KEY `uk_auth_open_api_resource_route` (`http_method`, `path_pattern`),
  KEY `idx_auth_open_api_resource_scope` (`scope_id`),
  KEY `idx_auth_open_api_resource_capability` (`capability_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放接口资源目录表';

CREATE TABLE IF NOT EXISTS `auth_scope_resource`
(
  `scope_id`    bigint(20) NOT NULL COMMENT 'Scope ID',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`scope_id`, `resource_id`),
  KEY `idx_auth_scope_resource_resource` (`resource_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Scope开放接口资源关联表';

SET @schema_name = DATABASE();

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app' AND COLUMN_NAME = 'client_id'),
  'ALTER TABLE `sys_open_app` ADD COLUMN `client_id` bigint(20) DEFAULT NULL COMMENT ''客户ID'' AFTER `app_id`',
  'SELECT ''skip sys_open_app.client_id'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app' AND COLUMN_NAME = 'region_code'),
  'ALTER TABLE `sys_open_app` ADD COLUMN `region_code` varchar(100) DEFAULT NULL COMMENT ''地区编码'' AFTER `tenant_id`',
  'SELECT ''skip sys_open_app.region_code'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app' AND COLUMN_NAME = 'region_name'),
  'ALTER TABLE `sys_open_app` ADD COLUMN `region_name` varchar(100) DEFAULT NULL COMMENT ''地区名称'' AFTER `region_code`',
  'SELECT ''skip sys_open_app.region_name'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app' AND COLUMN_NAME = 'owner_user_id'),
  'ALTER TABLE `sys_open_app` ADD COLUMN `owner_user_id` bigint(20) DEFAULT NULL COMMENT ''负责人用户ID'' AFTER `region_name`',
  'SELECT ''skip sys_open_app.owner_user_id'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app' AND COLUMN_NAME = 'owner_dept_id'),
  'ALTER TABLE `sys_open_app` ADD COLUMN `owner_dept_id` bigint(20) DEFAULT NULL COMMENT ''负责部门ID'' AFTER `owner_user_id`',
  'SELECT ''skip sys_open_app.owner_dept_id'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app_credential')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app_credential' AND COLUMN_NAME = 'credential_type'),
  'ALTER TABLE `sys_open_app_credential` ADD COLUMN `credential_type` varchar(20) DEFAULT ''AKSK'' COMMENT ''凭证类型'' AFTER `credential_name`',
  'SELECT ''skip sys_open_app_credential.credential_type'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app_credential')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app_credential' AND COLUMN_NAME = 'app_key_hash'),
  'ALTER TABLE `sys_open_app_credential` ADD COLUMN `app_key_hash` varchar(128) DEFAULT NULL COMMENT ''AppKey哈希'' AFTER `access_key`',
  'SELECT ''skip sys_open_app_credential.app_key_hash'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app_credential')
  AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_open_app_credential' AND COLUMN_NAME = 'last_used_time'),
  'ALTER TABLE `sys_open_app_credential` ADD COLUMN `last_used_time` datetime DEFAULT NULL COMMENT ''最近使用时间'' AFTER `last_rotated_time`',
  'SELECT ''skip sys_open_app_credential.last_used_time'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `auth_capability`
SET `enabled` = '0'
WHERE `capability_code` = 'openapi.all';

INSERT INTO `auth_capability`
(`capability_id`, `capability_code`, `capability_name`, `resource_type`, `resource_pattern`, `enabled`,
 `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000300, 'openapi.basic', '开放接口基础能力', 'api', '/open/*', '1',
 NULL, 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE
  `capability_name` = VALUES(`capability_name`),
  `resource_type` = VALUES(`resource_type`),
  `resource_pattern` = VALUES(`resource_pattern`),
  `enabled` = '1',
  `deleted` = '0';

INSERT INTO `auth_scope`
(`scope_id`, `capability_id`, `scope_code`, `scope_name`, `actions`, `enabled`, `tenant_id`,
 `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000301, 2800000000000000300, 'openapi.basic:*', '开放接口基础访问',
 'GET:/open/ping,GET:/open/meta/resources,GET:/openapi/ping,GET:/openapi/meta/resources', '1', NULL,
 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE
  `capability_id` = VALUES(`capability_id`),
  `scope_name` = VALUES(`scope_name`),
  `actions` = VALUES(`actions`),
  `enabled` = '1',
  `deleted` = '0';

INSERT INTO `auth_open_api_resource`
(`resource_id`, `resource_code`, `resource_name`, `http_method`, `path_pattern`, `capability_id`, `scope_id`,
 `auth_mode`, `status`, `tenant_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000400, 'openapi.basic.ping', '开放接口健康检查', 'GET', '/open/ping',
 2800000000000000300, 2800000000000000301, 'NONE', '1', NULL, '公开健康检查', 'system', NOW(), 'system', NOW(), '0'),
(2800000000000000401, 'openapi.basic.meta.resources', '已授权接口目录', 'GET', '/open/meta/resources',
 2800000000000000300, 2800000000000000301, 'AKSK', '1', NULL, '当前App已授权接口目录', 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE
  `resource_name` = VALUES(`resource_name`),
  `http_method` = VALUES(`http_method`),
  `path_pattern` = VALUES(`path_pattern`),
  `capability_id` = VALUES(`capability_id`),
  `scope_id` = VALUES(`scope_id`),
  `auth_mode` = VALUES(`auth_mode`),
  `status` = '1',
  `deleted` = '0';

INSERT IGNORE INTO `auth_scope_resource` (`scope_id`, `resource_id`)
VALUES
(2800000000000000301, 2800000000000000400),
(2800000000000000301, 2800000000000000401);

INSERT INTO `sys_open_api_policy`
(`policy_id`, `policy_name`, `path_pattern`, `http_method`, `security_mode`, `enabled`, `tenant_required`,
 `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES
(2800000000000000500, '开放接口健康检查放行', '/open/ping', 'GET', 'NONE', '1', '0',
 '系统默认公开健康检查', 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE
  `policy_name` = VALUES(`policy_name`),
  `security_mode` = VALUES(`security_mode`),
  `enabled` = '1',
  `tenant_required` = '0',
  `deleted` = '0';

INSERT IGNORE INTO `auth_subject_grant`
(`grant_id`, `subject_type`, `subject_id`, `capability_id`, `scope_id`, `status`, `tenant_id`,
 `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 2800000000003000000 + ROW_NUMBER() OVER (ORDER BY app_id),
       'app', CAST(app_id AS CHAR), 2800000000000000300, 2800000000000000301, '1', tenant_id,
       'system', NOW(), 'system', NOW(), '0'
FROM `sys_open_app`
WHERE `deleted` = '0';

INSERT INTO `sys_permission`
(`permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`,
 `icon`, `sort_order`, `status`, `is_frame`, `is_cache`, `visible`, `show_in_tabs`, `active_menu`, `redirect`,
 `deleted`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(405, 400, 'API资源', 'openapi:resource', 'C', '/openapi/resource', 'openapi/resource/index',
 'vicons:antd:ApartmentOutlined', 5, '1', '0', '0', '1', '1', NULL, NULL, '0', 'system', NOW(), 'system', NOW(), '开放接口资源目录'),
(4051, 405, '资源查询', 'openapi:resource:list', 'F', '', '', '#', 1, '1', '0', '0', '1', '1', NULL, NULL, '0', 'system', NOW(), 'system', NOW(), ''),
(4052, 405, '资源新增', 'openapi:resource:add', 'F', '', '', '#', 2, '1', '0', '0', '1', '1', NULL, NULL, '0', 'system', NOW(), 'system', NOW(), ''),
(4053, 405, '资源编辑', 'openapi:resource:edit', 'F', '', '', '#', 3, '1', '0', '0', '1', '1', NULL, NULL, '0', 'system', NOW(), 'system', NOW(), ''),
(4054, 405, '资源删除', 'openapi:resource:remove', 'F', '', '', '#', 4, '1', '0', '0', '1', '1', NULL, NULL, '0', 'system', NOW(), 'system', NOW(), '')
ON DUPLICATE KEY UPDATE
  `permission_name` = VALUES(`permission_name`),
  `permission_code` = VALUES(`permission_code`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`),
  `status` = '1',
  `deleted` = '0',
  `update_by` = 'system',
  `update_time` = NOW();

INSERT INTO `auth_permission`
(`permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `resource_type`,
 `resource_id`, `path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`, `visible`,
 `show_in_tabs`, `active_menu`, `redirect`, `tenant_id`, `remark`, `create_by`, `create_time`, `update_by`,
 `update_time`, `deleted`)
SELECT `permission_id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`,
       CASE WHEN `permission_type` IN ('M', 'C') THEN 'menu' ELSE 'permission' END,
       CASE WHEN `permission_type` IN ('M', 'C') THEN `path` ELSE `permission_code` END,
       `path`, `component`, `icon`, `sort_order`, `status`, `is_frame`, `is_cache`, `visible`,
       `show_in_tabs`, `active_menu`, `redirect`, NULL, `remark`, `create_by`, `create_time`, `update_by`,
       `update_time`, `deleted`
FROM `sys_permission`
WHERE `permission_id` IN (405, 4051, 4052, 4053, 4054)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `permission_name` = VALUES(`permission_name`),
  `permission_code` = VALUES(`permission_code`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`),
  `status` = '1',
  `deleted` = '0',
  `update_by` = VALUES(`update_by`),
  `update_time` = VALUES(`update_time`);

INSERT IGNORE INTO `auth_role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `auth_role` r
INNER JOIN `auth_permission` p ON p.permission_id IN (405, 4051, 4052, 4053, 4054)
WHERE r.role_code = 'admin';
