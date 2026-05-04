-- =============================================
-- Woodlin 多租户中后台管理系统 - 完整数据库架构
-- 作者: mumu
-- 描述: 包含所有表结构的完整数据库脚本（不含数据）
-- 版本: 2.0.0 (RBAC1支持)
-- 时间: 2025-11-07
-- 说明: 本脚本包含基础表 + RBAC1 + 字典/行政区划 + OSS管理 + SQL2API + ETL等功能模块
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `woodlin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `woodlin`;

-- =============================================
-- 第一部分：基础系统表
-- =============================================

-- =============================================
-- 租户管理表
-- =============================================
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`
(
  `tenant_id`     varchar(64)  NOT NULL COMMENT '租户ID',
  `tenant_name`   varchar(100) NOT NULL COMMENT '租户名称',
  `tenant_code`   varchar(50)  NOT NULL COMMENT '租户编码',
  `contact_name`  varchar(50)  DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20)  DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `status`        char(1)      DEFAULT '1' COMMENT '租户状态（1-启用，0-禁用）',
  `expire_time`   datetime     DEFAULT NULL COMMENT '过期时间',
  `user_limit`    int(11)      DEFAULT 100 COMMENT '用户数量限制',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`tenant_id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  KEY `idx_tenant_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='租户信息表';

-- =============================================
-- 部门管理表
-- =============================================
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
  `dept_id`     bigint(20)  NOT NULL COMMENT '部门ID',
  `parent_id`   bigint(20)   DEFAULT 0 COMMENT '父部门ID',
  `ancestors`   varchar(500) DEFAULT '' COMMENT '祖级列表',
  `dept_name`   varchar(30) NOT NULL COMMENT '部门名称',
  `dept_code`   varchar(50)  DEFAULT NULL COMMENT '部门编码',
  `sort_order`  int(11)      DEFAULT 0 COMMENT '显示顺序',
  `leader`      varchar(20)  DEFAULT NULL COMMENT '负责人',
  `phone`       varchar(11)  DEFAULT NULL COMMENT '联系电话',
  `email`       varchar(50)  DEFAULT NULL COMMENT '邮箱',
  `status`      char(1)      DEFAULT '1' COMMENT '部门状态（1-启用，0-禁用）',
  `tenant_id`   varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`   varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`dept_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='部门表';

-- =============================================
-- 用户信息表（包含密码策略字段）
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
  `user_id`         bigint(20)  NOT NULL COMMENT '用户ID',
  `username`        varchar(30) NOT NULL COMMENT '用户账号',
  `nickname`        varchar(30) NOT NULL COMMENT '用户昵称',
  `real_name`       varchar(30)  DEFAULT NULL COMMENT '真实姓名',
  `password`        varchar(100) DEFAULT '' COMMENT '密码',
  `email`           varchar(50)  DEFAULT '' COMMENT '用户邮箱',
  `mobile`          varchar(11)  DEFAULT '' COMMENT '手机号码',
  `avatar`          varchar(200) DEFAULT '' COMMENT '头像路径',
  `gender`          tinyint(1)   DEFAULT 0 COMMENT '用户性别（GB/T 2261.1标准：0-未知的性别，1-男性，2-女性，9-未说明的性别）',
  `birthday`        datetime     DEFAULT NULL COMMENT '生日',
  `status`          char(1)      DEFAULT '1' COMMENT '帐号状态（1-启用，0-禁用）',
  `tenant_id`       varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `dept_id`         bigint(20)   DEFAULT NULL COMMENT '部门ID',
  `last_login_time` datetime     DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip`   varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_count`     int(11)      DEFAULT 0 COMMENT '登录次数',
  `pwd_error_count` int(11)      DEFAULT 0 COMMENT '密码错误次数',
  `lock_time`       datetime     DEFAULT NULL COMMENT '账号锁定时间',
  `pwd_change_time` datetime     DEFAULT NULL COMMENT '密码最后修改时间',
  `is_first_login`  tinyint(1)   DEFAULT 1 COMMENT '是否首次登录（0-否，1-是）',
  `pwd_expire_days` int(11)      DEFAULT NULL COMMENT '密码过期天数（0表示永不过期，优先于系统配置）',
  `remark`          varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_pwd_change_time` (`pwd_change_time`),
  KEY `idx_is_first_login` (`is_first_login`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户信息表';

-- =============================================
-- 角色信息表（包含RBAC1层次结构字段）
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
  `role_id`        bigint(20)   NOT NULL COMMENT '角色ID',
  `parent_role_id` bigint(20)   DEFAULT NULL COMMENT '父角色ID（用于角色继承）',
  `role_level`     int(11)      DEFAULT 0 COMMENT '角色层级（0为顶级角色）',
  `role_path`      varchar(500) DEFAULT '' COMMENT '角色路径（用于快速查找祖先角色）',
  `role_name`      varchar(30)  NOT NULL COMMENT '角色名称',
  `role_code`      varchar(100) NOT NULL COMMENT '角色权限字符串',
  `sort_order`     int(11)      DEFAULT 0 COMMENT '显示顺序',
  `data_scope`     char(1)      DEFAULT '1' COMMENT '数据范围（1-全部数据权限，2-自定数据权限，3-本部门数据权限，4-本部门及以下数据权限，5-仅本人数据权限）',
  `is_inheritable` char(1)      DEFAULT '1' COMMENT '是否可继承（1-是，0-否）',
  `status`         char(1)      DEFAULT '1' COMMENT '角色状态（1-启用，0-禁用）',
  `tenant_id`      varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`         varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`      varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_parent_role_id` (`parent_role_id`),
  KEY `idx_role_level` (`role_level`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色信息表';

-- =============================================
-- 权限信息表
-- =============================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
  `permission_id`   bigint(20)  NOT NULL COMMENT '权限ID',
  `parent_id`       bigint(20)   DEFAULT 0 COMMENT '父权限ID',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `permission_type` char(1)      DEFAULT 'M' COMMENT '权限类型（M-目录，C-菜单，F-按钮）',
  `path`            varchar(200) DEFAULT '' COMMENT '路由地址',
  `component`       varchar(255) DEFAULT NULL COMMENT '组件路径',
  `icon`            varchar(100) DEFAULT '#' COMMENT '权限图标',
  `sort_order`      int(11)      DEFAULT 0 COMMENT '显示顺序',
  `status`          char(1)      DEFAULT '1' COMMENT '权限状态（1-启用，0-禁用）',
  `is_frame`        char(1)      DEFAULT '0' COMMENT '是否为外链（0-否，1-是）',
  `is_cache`        char(1)      DEFAULT '0' COMMENT '是否缓存（0-不缓存，1-缓存）',
  `visible`         char(1)      DEFAULT '1' COMMENT '显示状态（0-隐藏，1-显示）',
  `remark`          varchar(500) DEFAULT '' COMMENT '备注',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`permission_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='权限表';

-- =============================================
-- 用户和角色关联表
-- =============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户和角色关联表';

-- =============================================
-- 角色和权限关联表
-- =============================================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`
(
  `role_id`       bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色和权限关联表';

-- =============================================
-- 角色和部门关联表（数据权限）
-- =============================================
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`
(
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色和部门关联表';

-- =============================================
-- 第二部分：RBAC1 角色层次结构表
-- =============================================

-- =============================================
-- 角色继承关系表（闭包表）
-- =============================================
DROP TABLE IF EXISTS `sys_role_hierarchy`;
CREATE TABLE `sys_role_hierarchy`
(
  `ancestor_role_id`   bigint(20) NOT NULL COMMENT '祖先角色ID',
  `descendant_role_id` bigint(20) NOT NULL COMMENT '后代角色ID',
  `distance`           int(11)    NOT NULL DEFAULT 0 COMMENT '层级距离（0表示自己）',
  `tenant_id`          varchar(64)         DEFAULT NULL COMMENT '租户ID',
  `create_time`        datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`ancestor_role_id`, `descendant_role_id`),
  KEY `idx_descendant` (`descendant_role_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_distance` (`distance`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色继承层次关系表（闭包表）';

-- =============================================
-- 角色继承权限缓存表
-- =============================================
DROP TABLE IF EXISTS `sys_role_inherited_permission`;
CREATE TABLE `sys_role_inherited_permission`
(
  `role_id`        bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id`  bigint(20) NOT NULL COMMENT '权限ID（包括继承的）',
  `is_inherited`   char(1)     DEFAULT '0' COMMENT '是否继承而来（0-直接拥有，1-继承获得）',
  `inherited_from` bigint(20)  DEFAULT NULL COMMENT '继承自哪个角色',
  `tenant_id`      varchar(64) DEFAULT NULL COMMENT '租户ID',
  `update_time`    datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`),
  KEY `idx_inherited_from` (`inherited_from`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色继承权限缓存表';

-- =============================================
-- 第三部分：系统配置和日志表
-- =============================================

-- =============================================
-- 系统配置表
-- =============================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`
(
  `config_id`    bigint(20) NOT NULL COMMENT '参数主键',
  `config_name`  varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key`   varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type`  char(1)      DEFAULT 'N' COMMENT '系统内置（Y-是，N-否）',
  `tenant_id`    varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`       varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`    varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='参数配置表';

-- =============================================
-- 开放API应用表
-- =============================================
DROP TABLE IF EXISTS `sys_open_app`;
CREATE TABLE `sys_open_app`
(
  `app_id`       bigint(20)   NOT NULL COMMENT '应用ID',
  `app_code`     varchar(100) NOT NULL COMMENT '应用编码',
  `app_name`     varchar(100) NOT NULL COMMENT '应用名称',
  `status`       char(1)       DEFAULT '1' COMMENT '应用状态（1-启用，0-停用）',
  `tenant_id`    varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `owner_name`   varchar(64)   DEFAULT NULL COMMENT '负责人',
  `ip_whitelist` varchar(1000) DEFAULT NULL COMMENT 'IP白名单，逗号分隔',
  `remark`       varchar(500)  DEFAULT NULL COMMENT '备注',
  `create_by`    varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`  datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`  datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `uk_open_app_code` (`app_code`),
  KEY `idx_open_app_status` (`status`),
  KEY `idx_open_app_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放API应用表';

-- =============================================
-- 开放API凭证表
-- =============================================
DROP TABLE IF EXISTS `sys_open_app_credential`;
CREATE TABLE `sys_open_app_credential`
(
  `credential_id`                bigint(20)   NOT NULL COMMENT '凭证ID',
  `app_id`                       bigint(20)   NOT NULL COMMENT '应用ID',
  `credential_name`              varchar(100) NOT NULL COMMENT '凭证名称',
  `access_key`                   varchar(100) NOT NULL COMMENT '访问密钥',
  `secret_key_encrypted`         varchar(2000) DEFAULT NULL COMMENT '加密存储的SK',
  `secret_key_fingerprint`       varchar(64)   DEFAULT NULL COMMENT 'SK指纹',
  `signature_public_key`         text COMMENT '签名公钥',
  `encryption_public_key`        text COMMENT '客户端加密公钥',
  `server_public_key`            text COMMENT '服务端加密公钥',
  `server_private_key_encrypted` text COMMENT '加密存储的服务端私钥',
  `signature_algorithm`          varchar(50)   DEFAULT NULL COMMENT '签名算法',
  `encryption_algorithm`         varchar(50)   DEFAULT NULL COMMENT '加密算法',
  `security_mode`                varchar(50)   DEFAULT NULL COMMENT '安全模式',
  `active_from`                  datetime      DEFAULT NULL COMMENT '生效时间',
  `active_to`                    datetime      DEFAULT NULL COMMENT '失效时间',
  `last_rotated_time`            datetime      DEFAULT NULL COMMENT '最近轮换时间',
  `status`                       char(1)       DEFAULT '1' COMMENT '凭证状态（1-启用，0-吊销）',
  `remark`                       varchar(500)  DEFAULT NULL COMMENT '备注',
  `create_by`                    varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`                  datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`                    varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`                  datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                      char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`credential_id`),
  UNIQUE KEY `uk_open_credential_access_key` (`access_key`),
  KEY `idx_open_credential_app_id` (`app_id`),
  KEY `idx_open_credential_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放API凭证表';

-- =============================================
-- 开放API策略表
-- =============================================
DROP TABLE IF EXISTS `sys_open_api_policy`;
CREATE TABLE `sys_open_api_policy`
(
  `policy_id`                bigint(20)   NOT NULL COMMENT '策略ID',
  `policy_name`              varchar(100) NOT NULL COMMENT '策略名称',
  `path_pattern`             varchar(255) NOT NULL COMMENT '路径模式',
  `http_method`              varchar(20)  NOT NULL COMMENT 'HTTP方法',
  `security_mode`            varchar(50)  DEFAULT NULL COMMENT '安全模式',
  `signature_algorithm`      varchar(50)  DEFAULT NULL COMMENT '签名算法',
  `encryption_algorithm`     varchar(50)  DEFAULT NULL COMMENT '加密算法',
  `timestamp_window_seconds` int(11)      DEFAULT 300 COMMENT '时间窗秒数',
  `nonce_enabled`            char(1)      DEFAULT '1' COMMENT '是否启用nonce（1-是，0-否）',
  `nonce_ttl_seconds`        int(11)      DEFAULT 300 COMMENT 'nonce TTL秒数',
  `tenant_required`          char(1)      DEFAULT '0' COMMENT '是否要求租户（1-是，0-否）',
  `enabled`                  char(1)      DEFAULT '1' COMMENT '是否启用（1-是，0-否）',
  `remark`                   varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`                varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`              datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`                varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`              datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                  char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`policy_id`),
  KEY `idx_open_policy_method` (`http_method`),
  KEY `idx_open_policy_enabled` (`enabled`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='开放API策略表';

-- =============================================
-- 操作日志表
-- =============================================
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`
(
  `oper_id`        bigint(20) NOT NULL COMMENT '日志主键',
  `title`          varchar(50)   DEFAULT '' COMMENT '模块标题',
  `business_type`  int(2)        DEFAULT 0 COMMENT '业务类型（0-其它，1-新增，2-修改，3-删除）',
  `method`         varchar(200)  DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10)   DEFAULT '' COMMENT '请求方式',
  `oper_url`       varchar(255)  DEFAULT '' COMMENT '请求URL',
  `oper_ip`        varchar(128)  DEFAULT '' COMMENT '主机地址',
  `oper_location`  varchar(255)  DEFAULT '' COMMENT '操作地点',
  `oper_param`     varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result`    varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status`         int(1)        DEFAULT 0 COMMENT '操作状态（0-正常，1-异常）',
  `error_msg`      varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time`      datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time`      bigint(20)    DEFAULT 0 COMMENT '消耗时间（毫秒）',
  `tenant_id`      varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`      varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`oper_id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_status` (`status`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志记录';

-- =============================================
-- 第四部分：文件管理和OSS存储表
-- =============================================

-- =============================================
-- 存储平台配置表
-- =============================================
DROP TABLE IF EXISTS `sys_storage_config`;
CREATE TABLE `sys_storage_config`
(
  `config_id`          bigint(20)   NOT NULL COMMENT '配置ID',
  `config_name`        varchar(100) NOT NULL COMMENT '配置名称',
  `storage_type`       varchar(20)  NOT NULL COMMENT '存储类型（local-本地，minio-MinIO，s3-AWS S3，oss-阿里云OSS，cos-腾讯云COS，obs-华为云OBS）',
  `access_key`         varchar(200) DEFAULT NULL COMMENT '访问密钥',
  `secret_key`         varchar(500) DEFAULT NULL COMMENT '密钥（加密存储）',
  `endpoint`           varchar(500) DEFAULT NULL COMMENT '终端节点地址',
  `bucket_name`        varchar(100) DEFAULT NULL COMMENT '存储桶名称',
  `region`             varchar(50)  DEFAULT NULL COMMENT '区域',
  `base_path`          varchar(200) DEFAULT '/' COMMENT '基础路径',
  `domain`             varchar(500) DEFAULT NULL COMMENT '自定义域名',
  `is_default`         char(1)      DEFAULT '0' COMMENT '是否为默认配置（0-否，1-是）',
  `is_public`          char(1)      DEFAULT '0' COMMENT '是否公开访问（0-私有，1-公开）',
  `status`             char(1)      DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `max_file_size`      bigint(20)   DEFAULT 104857600 COMMENT '最大文件大小（字节），默认100MB',
  `allowed_extensions` varchar(500) DEFAULT NULL COMMENT '允许的文件扩展名，逗号分隔',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`             varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`config_id`),
  KEY `idx_storage_type` (`storage_type`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='存储平台配置表';

-- =============================================
-- 上传策略表
-- =============================================
DROP TABLE IF EXISTS `sys_upload_policy`;
CREATE TABLE `sys_upload_policy`
(
  `policy_id`          bigint(20)   NOT NULL COMMENT '策略ID',
  `policy_name`        varchar(100) NOT NULL COMMENT '策略名称',
  `policy_code`        varchar(50)  NOT NULL COMMENT '策略编码',
  `storage_config_id`  bigint(20)   NOT NULL COMMENT '存储配置ID',
  `detect_file_type`   char(1)      DEFAULT '0' COMMENT '是否检测文件真实类型（0-否，1-是，使用Apache Tika）',
  `check_file_size`    char(1)      DEFAULT '1' COMMENT '是否检查文件大小（0-否，1-是）',
  `max_file_size`      bigint(20)   DEFAULT 104857600 COMMENT '最大文件大小（字节），默认100MB',
  `allowed_extensions` varchar(500) DEFAULT NULL COMMENT '允许的文件扩展名，逗号分隔，如：jpg,png,pdf',
  `allowed_mime_types` varchar(500) DEFAULT NULL COMMENT '允许的MIME类型，逗号分隔',
  `check_md5`          char(1)      DEFAULT '1' COMMENT '是否校验MD5（0-否，1-是）',
  `allow_duplicate`    char(1)      DEFAULT '1' COMMENT '是否允许重复上传（0-否，1-是）',
  `generate_thumbnail` char(1)      DEFAULT '0' COMMENT '是否生成缩略图（0-否，1-是）',
  `thumbnail_width`    int(11)      DEFAULT 200 COMMENT '缩略图宽度',
  `thumbnail_height`   int(11)      DEFAULT 200 COMMENT '缩略图高度',
  `path_pattern`       varchar(200) DEFAULT '/{yyyy}/{MM}/{dd}/' COMMENT '文件路径模式，支持占位符：{yyyy}年，{MM}月，{dd}日，{userId}用户ID，{tenantId}租户ID',
  `file_name_pattern`  varchar(100) DEFAULT '{timestamp}_{random}' COMMENT '文件名模式，支持占位符：{original}原始文件名，{timestamp}时间戳，{random}随机字符串，{uuid}UUID',
  `signature_expires`  int(11)      DEFAULT 3600 COMMENT '签名有效期（秒），默认1小时',
  `callback_url`       varchar(500) DEFAULT NULL COMMENT '上传回调地址',
  `status`             char(1)      DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`             varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `uk_policy_code` (`policy_code`),
  KEY `idx_storage_config_id` (`storage_config_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='上传策略表';

-- =============================================
-- 文件信息表（增强版，整合所有文件相关功能）
-- =============================================
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`
(
  `file_id`            bigint(20)   NOT NULL COMMENT '文件ID',
  `file_name`          varchar(100) NOT NULL COMMENT '文件名称',
  `original_name`      varchar(100) DEFAULT NULL COMMENT '原始文件名',
  `file_path`          varchar(255) NOT NULL COMMENT '文件路径',
  `file_url`           varchar(500) DEFAULT NULL COMMENT '文件URL',
  `file_size`          bigint(20)   DEFAULT 0 COMMENT '文件大小（字节）',
  `file_extension`     varchar(20)  DEFAULT NULL COMMENT '文件扩展名',
  `file_type`          varchar(50)  DEFAULT NULL COMMENT '文件类型',
  `mime_type`          varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `detected_mime_type` varchar(100) DEFAULT NULL COMMENT '检测到的真实MIME类型（通过Apache Tika）',
  `file_md5`           varchar(32)  DEFAULT NULL COMMENT '文件MD5',
  `file_sha256`        varchar(64)  DEFAULT NULL COMMENT '文件SHA256',
  `storage_type`       varchar(20)  DEFAULT 'local' COMMENT '存储位置（local-本地，minio-MinIO，s3-AWS S3，oss-阿里云OSS，cos-腾讯云COS，obs-华为云OBS）',
  `storage_config_id`  bigint(20)   DEFAULT NULL COMMENT '存储配置ID',
  `upload_policy_id`   bigint(20)   DEFAULT NULL COMMENT '上传策略ID',
  `bucket_name`        varchar(100) DEFAULT NULL COMMENT '存储桶名称',
  `object_key`         varchar(500) DEFAULT NULL COMMENT '对象键',
  `is_image`           char(1)      DEFAULT '0' COMMENT '是否为图片（0-否，1-是）',
  `image_width`        int(11)      DEFAULT NULL COMMENT '图片宽度',
  `image_height`       int(11)      DEFAULT NULL COMMENT '图片高度',
  `thumbnail_path`     varchar(255) DEFAULT NULL COMMENT '缩略图路径',
  `thumbnail_url`      varchar(500) DEFAULT NULL COMMENT '缩略图URL',
  `is_public`          char(1)      DEFAULT '0' COMMENT '是否公开（0-否，1-是）',
  `access_count`       int(11)      DEFAULT 0 COMMENT '访问次数',
  `last_access_time`   datetime     DEFAULT NULL COMMENT '最后访问时间',
  `expired_time`       datetime     DEFAULT NULL COMMENT '过期时间',
  `upload_ip`          varchar(128) DEFAULT NULL COMMENT '上传IP',
  `user_agent`         varchar(500) DEFAULT NULL COMMENT '用户代理',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `business_type`      varchar(50)  DEFAULT NULL COMMENT '业务类型',
  `business_id`        varchar(100) DEFAULT NULL COMMENT '业务ID',
  `remark`             varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`file_id`),
  KEY `idx_file_md5` (`file_md5`),
  KEY `idx_file_sha256` (`file_sha256`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_storage_config_id` (`storage_config_id`),
  KEY `idx_upload_policy_id` (`upload_policy_id`),
  KEY `idx_business` (`business_type`, `business_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文件信息表';

-- =============================================
-- 上传令牌表（用于签名验证）
-- =============================================
DROP TABLE IF EXISTS `sys_upload_token`;
CREATE TABLE `sys_upload_token`
(
  `token_id`           bigint(20)   NOT NULL COMMENT '令牌ID',
  `token`              varchar(200) NOT NULL COMMENT '上传令牌',
  `policy_id`          bigint(20)   NOT NULL COMMENT '上传策略ID',
  `user_id`            bigint(20)   DEFAULT NULL COMMENT '用户ID',
  `signature`          varchar(500) NOT NULL COMMENT '签名',
  `max_file_size`      bigint(20)   DEFAULT NULL COMMENT '最大文件大小',
  `allowed_extensions` varchar(500) DEFAULT NULL COMMENT '允许的文件扩展名',
  `expire_time`        datetime     NOT NULL COMMENT '过期时间',
  `is_used`            char(1)      DEFAULT '0' COMMENT '是否已使用（0-否，1-是）',
  `used_time`          datetime     DEFAULT NULL COMMENT '使用时间',
  `file_id`            bigint(20)   DEFAULT NULL COMMENT '关联文件ID',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_policy_id` (`policy_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='上传令牌表';

-- =============================================
-- 第五部分：定时任务和代码生成表
-- =============================================

-- =============================================
-- 定时任务表
-- =============================================
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`
(
  `job_id`            bigint(20)   NOT NULL COMMENT '任务ID',
  `job_name`          varchar(64)  NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group`         varchar(64)  NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target`     varchar(500) NOT NULL COMMENT '调用目标字符串',
  `cron_expression`   varchar(255)          DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy`    varchar(20)           DEFAULT '3' COMMENT '计划执行错误策略（1-立即执行，2-执行一次，3-放弃执行）',
  `concurrent`        char(1)               DEFAULT '1' COMMENT '是否并发执行（0-禁止，1-允许）',
  `status`            char(1)               DEFAULT '0' COMMENT '状态（0-暂停，1-正常）',
  `next_execute_time` datetime              DEFAULT NULL COMMENT '下次执行时间',
  `last_execute_time` datetime              DEFAULT NULL COMMENT '上次执行时间',
  `tenant_id`         varchar(64)           DEFAULT NULL COMMENT '租户ID',
  `remark`            varchar(500)          DEFAULT '' COMMENT '备注信息',
  `create_by`         varchar(64)           DEFAULT NULL COMMENT '创建者',
  `create_time`       datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         varchar(64)           DEFAULT NULL COMMENT '更新者',
  `update_time`       datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`           char(1)               DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='定时任务调度表';

-- =============================================
-- 代码生成业务表
-- =============================================
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`
(
  `table_id`        bigint(20) NOT NULL COMMENT '编号',
  `table_name`      varchar(200) DEFAULT '' COMMENT '表名称',
  `table_comment`   varchar(500) DEFAULT '' COMMENT '表描述',
  `class_name`      varchar(100) DEFAULT '' COMMENT '实体类名称',
  `package_name`    varchar(100) DEFAULT NULL COMMENT '生成包路径',
  `module_name`     varchar(30)  DEFAULT NULL COMMENT '生成模块名',
  `business_name`   varchar(30)  DEFAULT NULL COMMENT '生成业务名',
  `function_name`   varchar(50)  DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50)  DEFAULT NULL COMMENT '生成功能作者',
  `gen_type`        char(1)      DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path`        varchar(200) DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `pk_column`       varchar(100) DEFAULT NULL COMMENT '主键信息',
  `remark`          varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`table_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='代码生成业务表';

-- =============================================
-- 第六部分：SQL2API 动态API表
-- =============================================

-- =============================================
-- SQL API配置表
-- =============================================
DROP TABLE IF EXISTS `sql2api_config`;
CREATE TABLE `sql2api_config`
(
  `api_id`            BIGINT       NOT NULL COMMENT 'API ID',
  `api_name`          VARCHAR(100) NOT NULL COMMENT 'API名称',
  `api_path`          VARCHAR(200) NOT NULL COMMENT 'API路径',
  `http_method`       VARCHAR(10)  NOT NULL DEFAULT 'GET' COMMENT '请求方法(GET,POST,PUT,DELETE)',
  `datasource_name`   VARCHAR(50)  NOT NULL DEFAULT 'master' COMMENT '数据源名称',
  `sql_type`          VARCHAR(20)  NOT NULL COMMENT 'SQL类型(SELECT,INSERT,UPDATE,DELETE)',
  `sql_content`       TEXT         NOT NULL COMMENT 'SQL语句(支持MyBatis动态SQL)',
  `params_config`     TEXT COMMENT '参数配置(JSON格式)',
  `result_type`       VARCHAR(20)  NOT NULL DEFAULT 'list' COMMENT '返回结果类型(single,list,page)',
  `cache_enabled`     TINYINT(1)            DEFAULT 0 COMMENT '是否启用缓存',
  `cache_expire`      INT                   DEFAULT 300 COMMENT '缓存过期时间(秒)',
  `encrypt_enabled`   TINYINT(1)            DEFAULT 0 COMMENT '是否启用加密',
  `encrypt_algorithm` VARCHAR(20) COMMENT '加密算法(AES,RSA,SM4)',
  `auth_required`     TINYINT(1)            DEFAULT 1 COMMENT '是否需要认证',
  `auth_type`         VARCHAR(20)           DEFAULT 'TOKEN' COMMENT '认证类型(TOKEN,API_KEY,NONE)',
  `flow_limit`        INT                   DEFAULT 0 COMMENT '流控配置(QPS限制,0表示不限制)',
  `api_desc`          VARCHAR(500) COMMENT 'API描述',
  `enabled`           TINYINT(1)            DEFAULT 1 COMMENT '是否启用',
  `status`            INT                   DEFAULT 0 COMMENT '状态(0=正常,1=禁用)',
  `create_by`         VARCHAR(64) COMMENT '创建者',
  `create_time`       DATETIME COMMENT '创建时间',
  `update_by`         VARCHAR(64) COMMENT '更新者',
  `update_time`       DATETIME COMMENT '更新时间',
  PRIMARY KEY (`api_id`),
  UNIQUE KEY `uk_api_path` (`api_path`),
  KEY `idx_datasource` (`datasource_name`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='SQL API配置表';

-- =============================================
-- API编排配置表
-- =============================================
DROP TABLE IF EXISTS `sql2api_orchestration`;
CREATE TABLE `sql2api_orchestration`
(
  `orchestration_id`     BIGINT       NOT NULL COMMENT '编排ID',
  `orchestration_name`   VARCHAR(100) NOT NULL COMMENT '编排名称',
  `orchestration_path`   VARCHAR(200) NOT NULL COMMENT '编排路径',
  `orchestration_config` TEXT COMMENT '编排配置(JSON格式)',
  `execution_order`      TEXT COMMENT '执行顺序配置(JSON数组)',
  `field_mapping`        TEXT COMMENT '字段映射配置(JSON格式)',
  `validation_rules`     TEXT COMMENT '校验规则配置(JSON格式)',
  `error_strategy`       VARCHAR(20) DEFAULT 'STOP' COMMENT '错误处理策略(STOP,CONTINUE,RETRY)',
  `timeout`              INT         DEFAULT 30000 COMMENT '超时时间(毫秒)',
  `enabled`              TINYINT(1)  DEFAULT 1 COMMENT '是否启用',
  `orchestration_desc`   VARCHAR(500) COMMENT '编排描述',
  `create_by`            VARCHAR(64) COMMENT '创建者',
  `create_time`          DATETIME COMMENT '创建时间',
  `update_by`            VARCHAR(64) COMMENT '更新者',
  `update_time`          DATETIME COMMENT '更新时间',
  PRIMARY KEY (`orchestration_id`),
  UNIQUE KEY `uk_orchestration_path` (`orchestration_path`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='API编排配置表';

-- =============================================
-- 基础设施-数据源配置表
-- =============================================
DROP TABLE IF EXISTS `infra_datasource`;
CREATE TABLE `infra_datasource`
(
  `id`              BIGINT       NOT NULL COMMENT '主键ID',
  `datasource_code` VARCHAR(64)  NOT NULL COMMENT '数据源唯一编码（工程级）',
  `datasource_name` VARCHAR(128) NOT NULL COMMENT '数据源名称（展示用）',
  `datasource_type` VARCHAR(32)  NOT NULL COMMENT '数据源类型：MYSQL / PG / ORACLE / DM / CLICKHOUSE',
  `driver_class`    VARCHAR(256) NOT NULL COMMENT 'JDBC Driver',
  `jdbc_url`        VARCHAR(512) NOT NULL COMMENT 'JDBC URL',
  `username`        VARCHAR(128) NOT NULL COMMENT '账号',
  `password`        VARCHAR(256) NOT NULL COMMENT '密码（加密）',
  `test_sql`        VARCHAR(512)          DEFAULT NULL COMMENT '连通性校验SQL',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `owner`           VARCHAR(64)           DEFAULT NULL COMMENT '负责人',
  `biz_tags`        VARCHAR(256)          DEFAULT NULL COMMENT '业务标签（逗号分隔）',
  `remark`          VARCHAR(512)          DEFAULT NULL COMMENT '备注',
  `ext_config`      JSON                  DEFAULT NULL COMMENT '扩展配置（连接池/方言/特殊参数）',
  `create_by`       VARCHAR(64)           DEFAULT NULL COMMENT '创建者',
  `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       VARCHAR(64)           DEFAULT NULL COMMENT '更新者',
  `update_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         CHAR(1)               DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  `version`         INT                   DEFAULT 1 COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_datasource_code` (`datasource_code`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='基础设施-数据源配置表';

-- =============================================
-- API执行日志表
-- =============================================
DROP TABLE IF EXISTS `sql2api_execute_log`;
CREATE TABLE `sql2api_execute_log`
(
  `log_id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `api_id`           BIGINT COMMENT 'API ID',
  `orchestration_id` BIGINT COMMENT '编排ID',
  `request_path`     VARCHAR(200) COMMENT '请求路径',
  `request_method`   VARCHAR(10) COMMENT '请求方法',
  `request_params`   TEXT COMMENT '请求参数',
  `response_result`  TEXT COMMENT '响应结果',
  `execute_status`   VARCHAR(20) COMMENT '执行状态(SUCCESS,FAILED,ERROR)',
  `execute_time`     BIGINT COMMENT '执行耗时(毫秒)',
  `error_message`    TEXT COMMENT '错误信息',
  `client_ip`        VARCHAR(50) COMMENT '客户端IP',
  `user_agent`       VARCHAR(500) COMMENT 'User-Agent',
  `create_time`      DATETIME COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_orchestration_id` (`orchestration_id`),
  KEY `idx_execute_status` (`execute_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='API执行日志表';

-- =============================================
-- 脚本完成
-- =============================================
-- 说明：
-- 1. 本脚本包含所有系统表结构，已整合RBAC1角色继承功能
-- 2. 去除了存储过程，角色继承逻辑应在Java应用层实现
-- 3. 表按功能模块分组，便于理解和维护
-- 4. 所有索引已优化，支持多租户查询
-- 5. 请在导入本脚本后，再导入数据脚本(woodlin_data.sql)
-- =============================================

-- =============================================
-- 动态字典与行政区划
-- =============================================

-- 字典类型表
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`
(
  `dict_id`       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name`     varchar(100) NOT NULL COMMENT '字典名称',
  `dict_type`     varchar(100) NOT NULL COMMENT '字典类型',
  `dict_category` varchar(50)  DEFAULT 'system' COMMENT '字典分类（system-系统字典，business-业务字典，custom-自定义字典）',
  `status`        char(1)      DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id`     varchar(64)  DEFAULT NULL COMMENT '租户ID（NULL表示通用字典）',
  `create_by`     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`, `tenant_id`, `deleted`),
  KEY `idx_dict_category` (`dict_category`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='字典类型表';

-- 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`
(
  `data_id`     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '字典数据主键',
  `dict_type`   varchar(100) NOT NULL COMMENT '字典类型',
  `dict_label`  varchar(100) NOT NULL COMMENT '字典标签',
  `dict_value`  varchar(100) NOT NULL COMMENT '字典键值',
  `dict_desc`   varchar(500) DEFAULT NULL COMMENT '字典描述',
  `dict_sort`   int(11)      DEFAULT 0 COMMENT '字典排序',
  `css_class`   varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class`  varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default`  char(1)      DEFAULT '0' COMMENT '是否默认（1-是，0-否）',
  `status`      char(1)      DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
  `extra_data`  text         DEFAULT NULL COMMENT '扩展数据（JSON格式）',
  `tenant_id`   varchar(64)  DEFAULT NULL COMMENT '租户ID（NULL表示通用字典）',
  `create_by`   varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`data_id`),
  KEY `idx_dict_type` (`dict_type`),
  KEY `idx_dict_sort` (`dict_sort`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='字典数据表';

-- 行政区划表（树形结构）
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region`
(
  `region_id`       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '区划主键',
  `region_code`     varchar(20)  NOT NULL COMMENT '区划代码（GB/T 2260标准6位代码）',
  `region_name`     varchar(100) NOT NULL COMMENT '区划名称',
  `parent_code`     varchar(20)    DEFAULT NULL COMMENT '父区划代码',
  `region_level`    int(11)        DEFAULT 1 COMMENT '区划层级（1-省级，2-市级，3-区县级，4-街道级）',
  `region_type`     varchar(20)    DEFAULT NULL COMMENT '区划类型（province-省，city-市，district-区县，street-街道）',
  `short_name`      varchar(50)    DEFAULT NULL COMMENT '简称',
  `pinyin`          varchar(100)   DEFAULT NULL COMMENT '拼音',
  `pinyin_abbr`     varchar(20)    DEFAULT NULL COMMENT '拼音缩写',
  `longitude`       decimal(10, 6) DEFAULT NULL COMMENT '经度',
  `latitude`        decimal(10, 6) DEFAULT NULL COMMENT '纬度',
  `sort_order`      int(11)        DEFAULT 0 COMMENT '排序',
  `is_municipality` char(1)        DEFAULT '0' COMMENT '是否直辖市/特别行政区（1-是，0-否）',
  `status`          char(1)        DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
  `remark`          varchar(500)   DEFAULT NULL COMMENT '备注',
  `create_by`       varchar(64)    DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)    DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)        DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`region_id`),
  UNIQUE KEY `uk_region_code` (`region_code`, `deleted`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_region_level` (`region_level`),
  KEY `idx_region_type` (`region_type`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='行政区划表';

-- =============================================
-- ETL 模块
-- =============================================

-- ETL任务表
DROP TABLE IF EXISTS `sys_etl_job`;
CREATE TABLE `sys_etl_job`
(
  `job_id`              bigint(20)   NOT NULL COMMENT '任务ID',
  `job_name`            varchar(100) NOT NULL COMMENT '任务名称',
  `job_group`           varchar(50)  DEFAULT 'DEFAULT' COMMENT '任务组名',
  `job_description`     varchar(500) DEFAULT NULL COMMENT '任务描述',
  `source_datasource`   varchar(100) NOT NULL COMMENT '源数据源名称',
  `source_table`        varchar(100) DEFAULT NULL COMMENT '源表名',
  `source_schema`       varchar(100) DEFAULT NULL COMMENT '源Schema名',
  `source_query`        text         DEFAULT NULL COMMENT '源查询SQL',
  `target_datasource`   varchar(100) NOT NULL COMMENT '目标数据源名称',
  `target_table`        varchar(100) NOT NULL COMMENT '目标表名',
  `target_schema`       varchar(100) DEFAULT NULL COMMENT '目标Schema名',
  `sync_mode`           varchar(20)  DEFAULT 'FULL' COMMENT '同步模式（FULL-全量，INCREMENTAL-增量）',
  `incremental_column`  varchar(100) DEFAULT NULL COMMENT '增量字段',
  `transform_rules`     text         DEFAULT NULL COMMENT '数据转换规则（JSON格式）',
  `filter_condition`    varchar(500) DEFAULT NULL COMMENT '过滤条件',
  `batch_size`          int(11)      DEFAULT 1000 COMMENT '批处理大小',
  `cron_expression`     varchar(100) NOT NULL COMMENT 'cron执行表达式',
  `status`              char(1)      DEFAULT '0' COMMENT '任务状态（1-启用，0-禁用）',
  `concurrent`          char(1)      DEFAULT '0' COMMENT '是否并发执行（1-允许，0-禁止）',
  `retry_count`         int(11)      DEFAULT 3 COMMENT '失败重试次数',
  `retry_interval`      int(11)      DEFAULT 60 COMMENT '重试间隔（秒）',
  `next_execute_time`   datetime     DEFAULT NULL COMMENT '下次执行时间',
  `last_execute_time`   datetime     DEFAULT NULL COMMENT '上次执行时间',
  `last_execute_status` varchar(20)  DEFAULT NULL COMMENT '上次执行状态',
  `tenant_id`           varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`              varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`           varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`job_id`),
  KEY `idx_job_name` (`job_name`),
  KEY `idx_job_group` (`job_group`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_next_execute_time` (`next_execute_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL任务表';

-- ETL执行历史表
DROP TABLE IF EXISTS `sys_etl_execution_log`;
CREATE TABLE `sys_etl_execution_log`
(
  `log_id`           bigint(20)   NOT NULL COMMENT '执行记录ID',
  `job_id`           bigint(20)   NOT NULL COMMENT '任务ID',
  `job_name`         varchar(100) NOT NULL COMMENT '任务名称',
  `execution_status` varchar(20)  NOT NULL COMMENT '执行状态（RUNNING-运行中，SUCCESS-成功，FAILED-失败，PARTIAL_SUCCESS-部分成功）',
  `start_time`       datetime     NOT NULL COMMENT '开始时间',
  `end_time`         datetime    DEFAULT NULL COMMENT '结束时间',
  `duration`         bigint(20)  DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `extracted_rows`   bigint(20)  DEFAULT 0 COMMENT '提取记录数',
  `transformed_rows` bigint(20)  DEFAULT 0 COMMENT '转换记录数',
  `loaded_rows`      bigint(20)  DEFAULT 0 COMMENT '加载记录数',
  `failed_rows`      bigint(20)  DEFAULT 0 COMMENT '失败记录数',
  `error_message`    text        DEFAULT NULL COMMENT '错误信息',
  `execution_detail` text        DEFAULT NULL COMMENT '执行详情（JSON格式）',
  `tenant_id`        varchar(64) DEFAULT NULL COMMENT '租户ID',
  `create_by`        varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time`      datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time`      datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          char(1)     DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`log_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_execution_status` (`execution_status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL执行历史表';

-- ETL字段映射规则表
DROP TABLE IF EXISTS `sys_etl_column_mapping_rule`;
CREATE TABLE `sys_etl_column_mapping_rule`
(
  `mapping_id`          bigint(20)   NOT NULL COMMENT '映射规则ID',
  `job_id`              bigint(20)   NOT NULL COMMENT '任务ID',
  `source_schema_name`  varchar(100) DEFAULT NULL COMMENT '源schema名称',
  `source_table_name`   varchar(100) DEFAULT NULL COMMENT '源表名称',
  `source_column_name`  varchar(100) NOT NULL COMMENT '源字段名称',
  `source_column_type`  varchar(100) DEFAULT NULL COMMENT '源字段类型',
  `target_schema_name`  varchar(100) DEFAULT NULL COMMENT '目标schema名称',
  `target_table_name`   varchar(100) DEFAULT NULL COMMENT '目标表名称',
  `target_column_name`  varchar(100) NOT NULL COMMENT '目标字段名称',
  `target_column_type`  varchar(100) DEFAULT NULL COMMENT '目标字段类型',
  `mapping_action`      varchar(20)  DEFAULT 'INSERT' COMMENT '映射动作',
  `transform_params`    text         DEFAULT NULL COMMENT '转换参数(JSON)',
  `constant_value`      varchar(500) DEFAULT NULL COMMENT '常量值',
  `default_value`       varchar(500) DEFAULT NULL COMMENT '默认值',
  `empty_value_policy`  varchar(50)  DEFAULT NULL COMMENT '空值处理策略',
  `ordinal_position`    int(11)      DEFAULT 0 COMMENT '字段顺序',
  `enabled`             char(1)      DEFAULT '1' COMMENT '是否启用（1-启用，0-禁用）',
  `tenant_id`           varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`              varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`           varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`mapping_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL字段映射规则表';

-- ETL同步检查点表
DROP TABLE IF EXISTS `sys_etl_sync_checkpoint`;
CREATE TABLE `sys_etl_sync_checkpoint`
(
  `checkpoint_id`         bigint(20)   NOT NULL COMMENT '检查点ID',
  `job_id`                bigint(20)   NOT NULL COMMENT '任务ID',
  `sync_mode`             varchar(20)  DEFAULT NULL COMMENT '同步模式',
  `incremental_column`    varchar(100) DEFAULT NULL COMMENT '增量字段',
  `last_incremental_value` varchar(255) DEFAULT NULL COMMENT '上次增量值',
  `last_sync_time`        datetime     DEFAULT NULL COMMENT '上次同步时间',
  `source_row_count`      bigint(20)   DEFAULT 0 COMMENT '源侧行数',
  `target_row_count`      bigint(20)   DEFAULT 0 COMMENT '目标侧行数',
  `applied_bucket_count`  int(11)      DEFAULT 0 COMMENT '命中桶数量',
  `skipped_bucket_count`  int(11)      DEFAULT 0 COMMENT '跳过桶数量',
  `validation_status`     varchar(20)  DEFAULT NULL COMMENT '校验状态',
  `last_execution_log_id` bigint(20)   DEFAULT NULL COMMENT '最近执行日志ID',
  `tenant_id`             varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`                varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`             varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`           datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`             varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`           datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`               char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`checkpoint_id`),
  UNIQUE KEY `uk_etl_sync_checkpoint_job` (`job_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL同步检查点表';

-- ETL表结构快照表
DROP TABLE IF EXISTS `sys_etl_table_structure_snapshot`;
CREATE TABLE `sys_etl_table_structure_snapshot`
(
  `snapshot_id`         bigint(20)   NOT NULL COMMENT '快照ID',
  `job_id`              bigint(20)   NOT NULL COMMENT '任务ID',
  `datasource_name`     varchar(100) NOT NULL COMMENT '数据源名称',
  `schema_name`         varchar(100) DEFAULT NULL COMMENT 'schema名称',
  `table_name`          varchar(100) NOT NULL COMMENT '表名称',
  `column_count`        int(11)      DEFAULT 0 COMMENT '字段数量',
  `primary_key_columns` varchar(500) DEFAULT NULL COMMENT '主键字段列表',
  `structure_digest`    varchar(128) NOT NULL COMMENT '结构摘要',
  `snapshot_time`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
  `tenant_id`           varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`              varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`           varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`snapshot_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL表结构快照表';

-- ETL桶位校验快照表
DROP TABLE IF EXISTS `sys_etl_data_bucket_checksum`;
CREATE TABLE `sys_etl_data_bucket_checksum`
(
  `bucket_checksum_id`  bigint(20)   NOT NULL COMMENT '桶位校验ID',
  `job_id`              bigint(20)   NOT NULL COMMENT '任务ID',
  `execution_log_id`    bigint(20)   NOT NULL COMMENT '执行日志ID',
  `bucket_number`       int(11)      NOT NULL COMMENT '桶号',
  `bucket_boundary_start` varchar(255) DEFAULT NULL COMMENT '桶边界起始值',
  `bucket_boundary_end` varchar(255) DEFAULT NULL COMMENT '桶边界结束值',
  `source_row_count`    bigint(20)   DEFAULT 0 COMMENT '源侧桶行数',
  `target_row_count`    bigint(20)   DEFAULT 0 COMMENT '目标侧桶行数',
  `source_checksum`     varchar(128) DEFAULT NULL COMMENT '源侧校验值',
  `target_checksum`     varchar(128) DEFAULT NULL COMMENT '目标侧校验值',
  `retry_count`         int(11)      DEFAULT 0 COMMENT '重试次数',
  `retry_success`       char(1)      DEFAULT '0' COMMENT '重试是否修复成功（1-是，0-否）',
  `last_retry_time`     datetime     DEFAULT NULL COMMENT '最后一次重试时间',
  `needs_sync`          char(1)      DEFAULT '0' COMMENT '是否需要同步（1-是，0-否）',
  `skip_reason`         varchar(255) DEFAULT NULL COMMENT '跳过原因',
  `compared_at`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '比对时间',
  `tenant_id`           varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`           varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`bucket_checksum_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL桶位校验快照表';

-- ETL数据一致性校验日志表
DROP TABLE IF EXISTS `sys_etl_data_validation_log`;
CREATE TABLE `sys_etl_data_validation_log`
(
  `validation_log_id`   bigint(20)   NOT NULL COMMENT '校验日志ID',
  `job_id`              bigint(20)   NOT NULL COMMENT '任务ID',
  `execution_log_id`    bigint(20)   NOT NULL COMMENT '执行日志ID',
  `validation_type`     varchar(20)  DEFAULT NULL COMMENT '校验类型',
  `source_row_count`    bigint(20)   DEFAULT 0 COMMENT '源侧行数',
  `target_row_count`    bigint(20)   DEFAULT 0 COMMENT '目标侧行数',
  `source_checksum`     varchar(128) DEFAULT NULL COMMENT '源侧摘要',
  `target_checksum`     varchar(128) DEFAULT NULL COMMENT '目标侧摘要',
  `bucket_count`        int(11)      DEFAULT 0 COMMENT '桶数量',
  `mismatch_bucket_count` int(11)    DEFAULT 0 COMMENT '差异桶数量',
  `validation_status`   varchar(20)  DEFAULT NULL COMMENT '校验状态',
  `validation_message`  text         DEFAULT NULL COMMENT '校验信息',
  `validated_at`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '校验时间',
  `tenant_id`           varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`           varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`validation_log_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ETL数据一致性校验日志表';

-- ETL任务表索引
CREATE INDEX `idx_etl_job_composite` ON `sys_etl_job` (`status`, `next_execute_time`);

-- ETL执行历史表索引
CREATE INDEX `idx_etl_log_composite` ON `sys_etl_execution_log` (`job_id`, `start_time` DESC);

-- ETL字段映射规则索引
CREATE INDEX `idx_etl_mapping_rule_job` ON `sys_etl_column_mapping_rule` (`job_id`, `enabled`, `ordinal_position`);

-- ETL表结构快照索引
CREATE INDEX `idx_etl_structure_snapshot_lookup`
  ON `sys_etl_table_structure_snapshot` (`job_id`, `datasource_name`, `schema_name`, `table_name`, `snapshot_time`);

-- ETL桶位校验快照索引
CREATE INDEX `idx_etl_bucket_checksum_execution`
  ON `sys_etl_data_bucket_checksum` (`execution_log_id`, `bucket_number`);

-- ETL数据一致性校验日志索引
CREATE INDEX `idx_etl_validation_log_execution`
  ON `sys_etl_data_validation_log` (`execution_log_id`, `validation_status`);

-- =============================================
-- 第N部分：测评模块（Assessment）
-- =============================================

-- =============================================
-- 测评主体表（量表/试卷/问卷）
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_form`;
CREATE TABLE `sys_assessment_form`
(
  `form_id`            bigint(20)   NOT NULL COMMENT '测评ID',
  `form_code`          varchar(100) NOT NULL COMMENT '唯一编码（业务标识）',
  `form_name`          varchar(200) NOT NULL COMMENT '测评名称',
  `assessment_type`    varchar(30)  DEFAULT 'scale' COMMENT '测评类型: scale/exam/survey',
  `category_code`      varchar(100) DEFAULT NULL COMMENT '分类编码（可关联字典）',
  `description`        text         DEFAULT NULL COMMENT '简介/说明',
  `cover_url`          varchar(500) DEFAULT NULL COMMENT '封面图URL',
  `tags`               varchar(500) DEFAULT NULL COMMENT '标签（JSON数组）',
  `current_version_id` bigint(20)   DEFAULT NULL COMMENT '当前活跃版本ID',
  `status`             tinyint(1)   DEFAULT 1 COMMENT '启用状态: 1=启用 0=禁用',
  `sort_order`         int(11)      DEFAULT 0 COMMENT '排序值',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`             varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`form_id`),
  UNIQUE KEY `uk_assessment_form_code` (`form_code`, `tenant_id`, `deleted`),
  KEY `idx_assessment_form_tenant` (`tenant_id`),
  KEY `idx_assessment_form_status` (`status`),
  KEY `idx_assessment_form_type` (`assessment_type`),
  KEY `idx_assessment_form_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测评主体表（量表/试卷/问卷）';

-- =============================================
-- 测评版本表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_form_version`;
CREATE TABLE `sys_assessment_form_version`
(
  `version_id`     bigint(20)   NOT NULL COMMENT '版本ID',
  `form_id`        bigint(20)   NOT NULL COMMENT '所属测评ID',
  `version_no`     varchar(30)  NOT NULL COMMENT '版本号（如 1.0.0）',
  `version_tag`    varchar(100) DEFAULT NULL COMMENT '版本标签（如 初稿/正式版/修订版）',
  `schema_id`      bigint(20)   DEFAULT NULL COMMENT '关联的 Schema ID',
  `schema_hash`    varchar(128) DEFAULT NULL COMMENT 'Schema 内容哈希（SHA-256）',
  `dsl_hash`       varchar(128) DEFAULT NULL COMMENT 'DSL 源码哈希',
  `status`         varchar(30)  DEFAULT 'draft' COMMENT '版本状态: draft/compiled/published/deprecated/archived',
  `published_at`   datetime     DEFAULT NULL COMMENT '发布时间',
  `published_by`   varchar(64)  DEFAULT NULL COMMENT '发布人',
  `change_summary` text         DEFAULT NULL COMMENT '变更说明',
  `tenant_id`      varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`      varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`version_id`),
  KEY `idx_assessment_version_form` (`form_id`),
  KEY `idx_assessment_version_status` (`status`),
  KEY `idx_assessment_version_tenant` (`tenant_id`),
  KEY `idx_assessment_version_published_at` (`published_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测评版本表';

-- =============================================
-- 测评 Schema 表（结构定义）
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_schema`;
CREATE TABLE `sys_assessment_schema`
(
  `schema_id`        bigint(20)  NOT NULL COMMENT 'Schema ID',
  `form_id`          bigint(20)  NOT NULL COMMENT '所属测评ID',
  `version_id`       bigint(20)  DEFAULT NULL COMMENT '所属版本ID',
  `status`           varchar(30) DEFAULT 'draft' COMMENT 'Schema 状态: draft/compiled/published/deprecated/archived',
  `canonical_schema` longtext    DEFAULT NULL COMMENT '规范 JSON Schema（后台配置主存）',
  `dsl_source`       longtext    DEFAULT NULL COMMENT 'DSL 源码（Kotlin DSL）',
  `compiled_schema`  longtext    DEFAULT NULL COMMENT '编译后运行时 Schema',
  `compile_error`    text        DEFAULT NULL COMMENT '最近一次编译错误信息',
  `schema_hash`      varchar(128) DEFAULT NULL COMMENT 'canonical_schema 内容哈希',
  `tenant_id`        varchar(64) DEFAULT NULL COMMENT '租户ID',
  `create_by`        varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time`      datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time`      datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          char(1)     DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`schema_id`),
  KEY `idx_assessment_schema_form` (`form_id`),
  KEY `idx_assessment_schema_version` (`version_id`),
  KEY `idx_assessment_schema_status` (`status`),
  KEY `idx_assessment_schema_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测评 Schema 表（结构定义）';

-- =============================================
-- 测评章节/页面表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_section`;
CREATE TABLE `sys_assessment_section`
(
  `section_id`      bigint(20)   NOT NULL COMMENT '章节ID',
  `version_id`      bigint(20)   NOT NULL COMMENT '所属版本ID',
  `form_id`         bigint(20)   NOT NULL COMMENT '所属测评ID（冗余）',
  `section_code`    varchar(100) NOT NULL COMMENT '章节唯一编码（版本内唯一）',
  `section_title`   varchar(200) DEFAULT NULL COMMENT '章节标题',
  `section_desc`    text         DEFAULT NULL COMMENT '章节说明/指导语',
  `display_mode`    varchar(30)  DEFAULT 'paged' COMMENT '展示模式: paged/continuous',
  `random_strategy` varchar(50)  DEFAULT 'none' COMMENT '随机化策略: none/random_items/random_options/random_both',
  `sort_order`      int(11)      DEFAULT 0 COMMENT '章节排序值',
  `is_required`     tinyint(1)   DEFAULT 1 COMMENT '是否必须完成（0-否，1-是）',
  `anchor_code`     varchar(100) DEFAULT NULL COMMENT '锚点编码（断点续答定位）',
  `tenant_id`       varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`section_id`),
  KEY `idx_assessment_section_version` (`version_id`),
  KEY `idx_assessment_section_form` (`form_id`),
  KEY `idx_assessment_section_tenant` (`tenant_id`),
  KEY `idx_assessment_section_sort` (`version_id`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测评章节/页面表';

-- =============================================
-- 题目/条目表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_item`;
CREATE TABLE `sys_assessment_item`
(
  `item_id`             bigint(20)    NOT NULL COMMENT '题目ID',
  `version_id`          bigint(20)    NOT NULL COMMENT '所属版本ID',
  `section_id`          bigint(20)    DEFAULT NULL COMMENT '所属章节ID',
  `form_id`             bigint(20)    NOT NULL COMMENT '所属测评ID（冗余）',
  `item_code`           varchar(100)  NOT NULL COMMENT '逻辑题号（版本内唯一，如 Q01）',
  `item_type`           varchar(50)   DEFAULT 'single_choice' COMMENT '题型: single_choice/multiple_choice/matrix_single/matrix_multiple/rating/short_text/…',
  `stem`                mediumtext    DEFAULT NULL COMMENT '题干（支持富文本/Markdown）',
  `stem_media_url`      varchar(500)  DEFAULT NULL COMMENT '题干附属媒体URL',
  `help_text`           varchar(500)  DEFAULT NULL COMMENT '帮助提示文本',
  `sort_order`          int(11)       DEFAULT 0 COMMENT '章节内排序值',
  `is_required`         tinyint(1)    DEFAULT 1 COMMENT '是否必答（0-否，1-是）',
  `is_scored`           tinyint(1)    DEFAULT 1 COMMENT '是否计分题（0-否，1-是）',
  `is_anchor`           tinyint(1)    DEFAULT 0 COMMENT '是否锚题（0-否，1-是）',
  `is_reverse`          tinyint(1)    DEFAULT 0 COMMENT '是否反向题（0-否，1-是）',
  `is_demographic`      tinyint(1)    DEFAULT 0 COMMENT '是否人口学信息题（0-否，1-是）',
  `max_score`           decimal(10,4) DEFAULT NULL COMMENT '单题最高分',
  `min_score`           decimal(10,4) DEFAULT NULL COMMENT '单题最低分',
  `time_limit_seconds`  int(11)       DEFAULT 0 COMMENT '单题作答时限（秒，0不限）',
  `demographic_field`   varchar(100)  DEFAULT NULL COMMENT '人口学字段名（如 gender/age/region_code）',
  `tenant_id`           varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`           varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`item_id`),
  KEY `idx_assessment_item_version` (`version_id`),
  KEY `idx_assessment_item_section` (`section_id`),
  KEY `idx_assessment_item_form` (`form_id`),
  KEY `idx_assessment_item_code` (`version_id`, `item_code`),
  KEY `idx_assessment_item_tenant` (`tenant_id`),
  KEY `idx_assessment_item_sort` (`section_id`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目/条目表';

-- =============================================
-- 选项表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_option`;
CREATE TABLE `sys_assessment_option`
(
  `option_id`            bigint(20)    NOT NULL COMMENT '选项ID',
  `item_id`              bigint(20)    NOT NULL COMMENT '所属题目ID',
  `option_code`          varchar(30)   NOT NULL COMMENT '选项逻辑编码（题目内唯一，如 A/B/C）',
  `display_text`         text          DEFAULT NULL COMMENT '展示文本（支持富文本/Markdown）',
  `media_url`            varchar(500)  DEFAULT NULL COMMENT '选项附属媒体URL',
  `raw_value`            varchar(255)  DEFAULT NULL COMMENT '原始字符串值',
  `score_value`          decimal(10,4) DEFAULT NULL COMMENT '正向得分值',
  `score_reverse_value`  decimal(10,4) DEFAULT NULL COMMENT '反向得分值（TABLE 模式反向计分）',
  `is_exclusive`         tinyint(1)    DEFAULT 0 COMMENT '是否互斥（0-否，1-是）',
  `is_correct`           tinyint(1)    DEFAULT 0 COMMENT '是否正确答案（0-否，1-是）',
  `sort_order`           int(11)       DEFAULT 0 COMMENT '选项排序值',
  `tenant_id`            varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`            varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`          datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`            varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`          datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`              char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`option_id`),
  KEY `idx_assessment_option_item` (`item_id`),
  KEY `idx_assessment_option_sort` (`item_id`, `sort_order`),
  KEY `idx_assessment_option_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目选项表';

-- =============================================
-- 维度/因子/分量表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_dimension`;
CREATE TABLE `sys_assessment_dimension`
(
  `dimension_id`        bigint(20)   NOT NULL COMMENT '维度ID',
  `form_id`             bigint(20)   NOT NULL COMMENT '所属测评ID',
  `version_id`          bigint(20)   NOT NULL COMMENT '所属版本ID',
  `parent_dimension_id` bigint(20)   DEFAULT NULL COMMENT '父维度ID（为 null 则为顶层维度）',
  `dimension_code`      varchar(100) NOT NULL COMMENT '维度编码（版本内唯一）',
  `dimension_name`      varchar(200) NOT NULL COMMENT '维度名称',
  `dimension_desc`      text         DEFAULT NULL COMMENT '维度说明',
  `score_mode`          varchar(50)  DEFAULT 'sum' COMMENT '计分模式: sum/mean/max/min/weighted_sum/custom_dsl',
  `score_dsl`           mediumtext   DEFAULT NULL COMMENT '自定义计分 DSL',
  `norm_set_id`         bigint(20)   DEFAULT NULL COMMENT '默认关联的常模集ID',
  `sort_order`          int(11)      DEFAULT 0 COMMENT '排序值',
  `tenant_id`           varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`           varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`dimension_id`),
  KEY `idx_assessment_dimension_form` (`form_id`),
  KEY `idx_assessment_dimension_version` (`version_id`),
  KEY `idx_assessment_dimension_parent` (`parent_dimension_id`),
  KEY `idx_assessment_dimension_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='维度/因子/分量表';

-- =============================================
-- 题目-维度映射表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_dimension_item`;
CREATE TABLE `sys_assessment_dimension_item`
(
  `id`           bigint(20)    NOT NULL COMMENT '映射ID',
  `dimension_id` bigint(20)    NOT NULL COMMENT '维度ID',
  `item_id`      bigint(20)    NOT NULL COMMENT '题目ID',
  `version_id`   bigint(20)    NOT NULL COMMENT '版本ID（冗余，便于版本内查询）',
  `weight`       decimal(10,4) DEFAULT 1.0000 COMMENT '计分权重（默认 1.0）',
  `score_mode`   varchar(50)   DEFAULT NULL COMMENT '该条目在本维度的计分模式（覆盖维度默认值）',
  `reverse_mode` varchar(20)   DEFAULT 'none' COMMENT '反向计分模式: none/formula/table',
  `tenant_id`    varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`    varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`  datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`  datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assessment_dim_item` (`dimension_id`, `item_id`, `deleted`),
  KEY `idx_assessment_dim_item_version` (`version_id`),
  KEY `idx_assessment_dim_item_item` (`item_id`),
  KEY `idx_assessment_dim_item_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目-维度映射表';

-- =============================================
-- 规则定义表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_rule`;
CREATE TABLE `sys_assessment_rule`
(
  `rule_id`       bigint(20)   NOT NULL COMMENT '规则ID',
  `form_id`       bigint(20)   NOT NULL COMMENT '所属测评ID',
  `version_id`    bigint(20)   NOT NULL COMMENT '所属版本ID',
  `rule_code`     varchar(100) NOT NULL COMMENT '规则编码（版本内唯一）',
  `rule_name`     varchar(200) DEFAULT NULL COMMENT '规则名称',
  `rule_type`     varchar(50)  NOT NULL COMMENT '规则类型: display/branch/validation/score/norm_match/report/eligibility/terminate',
  `target_type`   varchar(30)  DEFAULT NULL COMMENT '作用目标类型: item/section/dimension/form',
  `target_code`   varchar(200) DEFAULT NULL COMMENT '作用目标编码',
  `dsl_source`    mediumtext   DEFAULT NULL COMMENT '规则 DSL 源码',
  `compiled_rule` mediumtext   DEFAULT NULL COMMENT '编译后的规则表示（JSON）',
  `priority`      int(11)      DEFAULT 100 COMMENT '执行优先级（数字越小优先级越高）',
  `is_active`     tinyint(1)   DEFAULT 1 COMMENT '是否启用（0-否，1-是）',
  `compile_error` text         DEFAULT NULL COMMENT '最近一次编译错误',
  `tenant_id`     varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`rule_id`),
  KEY `idx_assessment_rule_form` (`form_id`),
  KEY `idx_assessment_rule_version` (`version_id`),
  KEY `idx_assessment_rule_type` (`rule_type`),
  KEY `idx_assessment_rule_active` (`is_active`),
  KEY `idx_assessment_rule_tenant` (`tenant_id`),
  KEY `idx_assessment_rule_priority` (`version_id`, `rule_type`, `priority`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='规则定义表';

-- =============================================
-- 锚题定义表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_anchor_item`;
CREATE TABLE `sys_assessment_anchor_item`
(
  `anchor_id`   bigint(20)   NOT NULL COMMENT '锚题记录ID',
  `form_id`     bigint(20)   NOT NULL COMMENT '所属测评ID',
  `anchor_code` varchar(100) NOT NULL COMMENT '锚题逻辑编码（跨版本共享）',
  `item_id`     bigint(20)   NOT NULL COMMENT '当前版本对应的题目ID',
  `version_id`  bigint(20)   NOT NULL COMMENT '版本ID',
  `anchor_type` varchar(30)  DEFAULT 'common' COMMENT '锚题类型: common/equating/calibration',
  `note`        varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id`   varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`   varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`anchor_id`),
  KEY `idx_assessment_anchor_form` (`form_id`),
  KEY `idx_assessment_anchor_version` (`version_id`),
  KEY `idx_assessment_anchor_code` (`anchor_code`),
  KEY `idx_assessment_anchor_item` (`item_id`),
  KEY `idx_assessment_anchor_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='锚题定义表';

-- =============================================
-- 发布实例表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_publish`;
CREATE TABLE `sys_assessment_publish`
(
  `publish_id`             bigint(20)   NOT NULL COMMENT '发布ID',
  `form_id`                bigint(20)   NOT NULL COMMENT '所属测评ID',
  `version_id`             bigint(20)   NOT NULL COMMENT '绑定的版本ID',
  `publish_code`           varchar(100) DEFAULT NULL COMMENT '发布编码（可用于生成访问链接）',
  `publish_name`           varchar(200) NOT NULL COMMENT '发布名称/批次名称',
  `status`                 varchar(30)  DEFAULT 'draft' COMMENT '发布状态: draft/under_review/published/paused/closed/archived',
  `start_time`             datetime     DEFAULT NULL COMMENT '开放开始时间',
  `end_time`               datetime     DEFAULT NULL COMMENT '开放截止时间',
  `time_limit_minutes`     int(11)      DEFAULT 0 COMMENT '作答总时限（分钟，0不限）',
  `max_attempts`           int(11)      DEFAULT 1 COMMENT '最大允许作答次数（0不限）',
  `allow_anonymous`        tinyint(1)   DEFAULT 0 COMMENT '是否允许匿名作答（0-否，1-是）',
  `allow_resume`           tinyint(1)   DEFAULT 1 COMMENT '是否允许断点续答（0-否，1-是）',
  `random_strategy`        varchar(50)  DEFAULT 'none' COMMENT '全局随机化策略: none/random_items/random_options/random_both',
  `access_policy`          text         DEFAULT NULL COMMENT '访问控制策略（JSON）',
  `device_restriction`     text         DEFAULT NULL COMMENT '设备限制（JSON）',
  `show_result_immediately` tinyint(1)  DEFAULT 0 COMMENT '完成后是否立即展示结果（0-否，1-是）',
  `result_visibility`      varchar(30)  DEFAULT 'self' COMMENT '结果可见范围: self/admin/all',
  `tenant_id`              varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `remark`                 varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`              varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`            datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`              varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`            datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`publish_id`),
  UNIQUE KEY `uk_assessment_publish_code` (`publish_code`, `deleted`),
  KEY `idx_assessment_publish_form` (`form_id`),
  KEY `idx_assessment_publish_version` (`version_id`),
  KEY `idx_assessment_publish_status` (`status`),
  KEY `idx_assessment_publish_tenant` (`tenant_id`),
  KEY `idx_assessment_publish_time` (`start_time`, `end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='发布实例表';

-- =============================================
-- 作答会话表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_session`;
CREATE TABLE `sys_assessment_session`
(
  `session_id`      bigint(20)   NOT NULL COMMENT '会话ID',
  `publish_id`      bigint(20)   NOT NULL COMMENT '发布实例ID',
  `form_id`         bigint(20)   NOT NULL COMMENT '测评ID（冗余）',
  `version_id`      bigint(20)   NOT NULL COMMENT '版本ID（冗余）',
  `user_id`         bigint(20)   DEFAULT NULL COMMENT '系统用户ID（匿名时为 null）',
  `anonymous_token` varchar(200) DEFAULT NULL COMMENT '匿名标识 Token',
  `status`          varchar(30)  DEFAULT 'not_started' COMMENT '会话状态: not_started/in_progress/paused/completed/expired/abandoned/invalidated',
  `display_seed`    bigint(20)   DEFAULT NULL COMMENT '乱序随机种子',
  `started_at`      datetime     DEFAULT NULL COMMENT '开始作答时间',
  `completed_at`    datetime     DEFAULT NULL COMMENT '完成时间',
  `elapsed_seconds` int(11)      DEFAULT 0 COMMENT '累计用时（秒）',
  `client_ip`       varchar(128) DEFAULT NULL COMMENT '作答客户端IP',
  `user_agent`      varchar(500) DEFAULT NULL COMMENT '浏览器 User-Agent',
  `device_type`     varchar(20)  DEFAULT NULL COMMENT '设备类型: pc/mobile/tablet',
  `attempt_number`  int(11)      DEFAULT 1 COMMENT '本次是第几次作答（从1开始）',
  `tenant_id`       varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`session_id`),
  KEY `idx_assessment_session_publish` (`publish_id`),
  KEY `idx_assessment_session_form` (`form_id`),
  KEY `idx_assessment_session_user` (`user_id`),
  KEY `idx_assessment_session_status` (`status`),
  KEY `idx_assessment_session_tenant` (`tenant_id`),
  KEY `idx_assessment_session_started_at` (`started_at`),
  KEY `idx_assessment_session_completed_at` (`completed_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='作答会话表';

-- =============================================
-- 作答断点快照表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_session_snapshot`;
CREATE TABLE `sys_assessment_session_snapshot`
(
  `snapshot_id`           bigint(20)  NOT NULL COMMENT '快照ID',
  `session_id`            bigint(20)  NOT NULL COMMENT '所属会话ID',
  `current_section_code`  varchar(100) DEFAULT NULL COMMENT '当前所在章节编码',
  `current_item_code`     varchar(100) DEFAULT NULL COMMENT '当前作答到的题目编码',
  `item_order_snapshot`   mediumtext   DEFAULT NULL COMMENT '题目展示顺序快照（JSON 数组）',
  `option_order_snapshot` mediumtext   DEFAULT NULL COMMENT '选项展示顺序快照（JSON Map）',
  `answered_cache`        mediumtext   DEFAULT NULL COMMENT '已答缓存（JSON Map）',
  `elapsed_seconds`       int(11)      DEFAULT 0 COMMENT '截至快照时的累计用时（秒）',
  `snapshot_at`           datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
  `tenant_id`             varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`             varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`           datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`             varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`           datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`               char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_assessment_snapshot_session` (`session_id`),
  KEY `idx_assessment_snapshot_at` (`snapshot_at`),
  KEY `idx_assessment_snapshot_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='作答断点快照表';

-- =============================================
-- 逐题作答记录表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_response`;
CREATE TABLE `sys_assessment_response`
(
  `response_id`                   bigint(20)   NOT NULL COMMENT '作答记录ID',
  `session_id`                    bigint(20)   NOT NULL COMMENT '所属会话ID',
  `form_id`                       bigint(20)   NOT NULL COMMENT '测评ID（冗余）',
  `item_id`                       bigint(20)   NOT NULL COMMENT '题目ID',
  `item_code`                     varchar(100) NOT NULL COMMENT '题目逻辑编码',
  `display_order`                 int(11)      DEFAULT NULL COMMENT '该题在本次作答中的展示序号',
  `raw_answer`                    mediumtext   DEFAULT NULL COMMENT '原始作答 payload（JSON）',
  `selected_option_codes`         text         DEFAULT NULL COMMENT '选中的选项逻辑编码（JSON 数组）',
  `selected_option_display_orders` text        DEFAULT NULL COMMENT '选中选项的展示序号（JSON 数组）',
  `text_answer`                   mediumtext   DEFAULT NULL COMMENT '文本答案（简答/填空题）',
  `answered_at`                   datetime     DEFAULT NULL COMMENT '本题作答时间',
  `time_spent_seconds`            int(11)      DEFAULT NULL COMMENT '本题用时（秒）',
  `is_skipped`                    tinyint(1)   DEFAULT 0 COMMENT '是否跳过/未作答（0-否，1-是）',
  `tenant_id`                     varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`                     varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`                   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`                     varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`                   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                       char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`response_id`),
  KEY `idx_assessment_response_session` (`session_id`),
  KEY `idx_assessment_response_form` (`form_id`),
  KEY `idx_assessment_response_item` (`item_id`),
  KEY `idx_assessment_response_tenant` (`tenant_id`),
  KEY `idx_assessment_response_answered_at` (`answered_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='逐题作答记录表';

-- =============================================
-- 测评总结果表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_result`;
CREATE TABLE `sys_assessment_result`
(
  `result_id`            bigint(20)    NOT NULL COMMENT '结果ID',
  `session_id`           bigint(20)    NOT NULL COMMENT '作答会话ID',
  `form_id`              bigint(20)    NOT NULL COMMENT '测评ID（冗余）',
  `publish_id`           bigint(20)    NOT NULL COMMENT '发布实例ID（冗余）',
  `user_id`              bigint(20)    DEFAULT NULL COMMENT '系统用户ID',
  `raw_total_score`      decimal(10,4) DEFAULT NULL COMMENT '原始总分',
  `weighted_total_score` decimal(10,4) DEFAULT NULL COMMENT '加权总分',
  `standard_score`       decimal(10,4) DEFAULT NULL COMMENT '标准分（常模转换后）',
  `norm_score_type`      varchar(30)   DEFAULT NULL COMMENT '标准分类型: t_score/z_score/percentile/stanine/sten/grade_equivalent/raw_grade',
  `percentile`           decimal(6,2)  DEFAULT NULL COMMENT '百分位（0-100）',
  `grade_label`          varchar(100)  DEFAULT NULL COMMENT '综合等级标签',
  `norm_set_id`          bigint(20)    DEFAULT NULL COMMENT '命中的常模集ID',
  `norm_segment_id`      bigint(20)    DEFAULT NULL COMMENT '命中的常模分层ID',
  `risk_level`           varchar(20)   DEFAULT 'none' COMMENT '综合风险等级: none/low/medium/high/confirmed',
  `answered_count`       int(11)       DEFAULT 0 COMMENT '作答题数',
  `total_item_count`     int(11)       DEFAULT 0 COMMENT '应答总题数',
  `report_json`          longtext      DEFAULT NULL COMMENT '生成的报告 JSON',
  `score_trace_json` longtext DEFAULT NULL COMMENT '计分审计轨迹 JSON',
  `tenant_id`            varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`            varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`          datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`            varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`          datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`              char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`result_id`),
  UNIQUE KEY `uk_assessment_result_session` (`session_id`, `deleted`),
  KEY `idx_assessment_result_form` (`form_id`),
  KEY `idx_assessment_result_publish` (`publish_id`),
  KEY `idx_assessment_result_user` (`user_id`),
  KEY `idx_assessment_result_risk` (`risk_level`),
  KEY `idx_assessment_result_tenant` (`tenant_id`),
  KEY `idx_assessment_result_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测评总结果表';

-- =============================================
-- 维度得分结果表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_result_dimension`;
CREATE TABLE `sys_assessment_result_dimension`
(
  `id`              bigint(20)    NOT NULL COMMENT '记录ID',
  `result_id`       bigint(20)    NOT NULL COMMENT '总结果ID',
  `session_id`      bigint(20)    NOT NULL COMMENT '会话ID（冗余）',
  `dimension_id`    bigint(20)    NOT NULL COMMENT '维度ID',
  `dimension_code`  varchar(100)  DEFAULT NULL COMMENT '维度编码（冗余）',
  `raw_score`       decimal(10,4) DEFAULT NULL COMMENT '维度原始分',
  `mean_score`      decimal(10,4) DEFAULT NULL COMMENT '维度均分',
  `standard_score`  decimal(10,4) DEFAULT NULL COMMENT '维度标准分（常模转换后）',
  `percentile`      decimal(6,2)  DEFAULT NULL COMMENT '维度百分位',
  `grade_label`     varchar(100)  DEFAULT NULL COMMENT '维度等级标签',
  `norm_segment_id` bigint(20)    DEFAULT NULL COMMENT '命中的常模分层ID',
  `item_count`      int(11)       DEFAULT 0 COMMENT '参与计分的题数',
  `tenant_id`       varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`       varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_assessment_result_dim_result` (`result_id`),
  KEY `idx_assessment_result_dim_session` (`session_id`),
  KEY `idx_assessment_result_dim_dimension` (`dimension_id`),
  KEY `idx_assessment_result_dim_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='维度得分结果表';

-- =============================================
-- 人口学档案快照表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_demographic_profile`;
CREATE TABLE `sys_assessment_demographic_profile`
(
  `profile_id`      bigint(20)    NOT NULL COMMENT '档案ID',
  `session_id`      bigint(20)    NOT NULL COMMENT '关联会话ID',
  `user_id`         bigint(20)    DEFAULT NULL COMMENT '系统用户ID（匿名时为 null）',
  `gender`          varchar(10)   DEFAULT NULL COMMENT '性别（对应字典值）',
  `birth_year`      int(11)       DEFAULT NULL COMMENT '出生年份',
  `age`             int(11)       DEFAULT NULL COMMENT '年龄',
  `age_group`       varchar(20)   DEFAULT NULL COMMENT '年龄段编码（如 18-25）',
  `education_level` varchar(30)   DEFAULT NULL COMMENT '学历（对应字典值）',
  `occupation`      varchar(50)   DEFAULT NULL COMMENT '职业（对应字典值）',
  `region_code`     varchar(30)   DEFAULT NULL COMMENT '地区编码（关联 sys_region.region_code）',
  `province_code`   varchar(10)   DEFAULT NULL COMMENT '省级编码（冗余，便于地区常模分层）',
  `marital_status`  varchar(20)   DEFAULT NULL COMMENT '婚姻状况（对应字典值）',
  `ethnicity`       varchar(30)   DEFAULT NULL COMMENT '民族（对应字典值）',
  `extra_fields`    text          DEFAULT NULL COMMENT '扩展人口学字段（JSON Map）',
  `norm_weight`     decimal(10,4) DEFAULT 1.0000 COMMENT '常模分层权重（默认 1.0）',
  `tenant_id`       varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`       varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `uk_assessment_demographic_session` (`session_id`, `deleted`),
  KEY `idx_assessment_demographic_user` (`user_id`),
  KEY `idx_assessment_demographic_region` (`region_code`),
  KEY `idx_assessment_demographic_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='人口学档案快照表';

-- =============================================
-- 作答事件日志表（行为埋点）
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_event_log`;
CREATE TABLE `sys_assessment_event_log`
(
  `event_id`        bigint(20)   NOT NULL COMMENT '事件ID',
  `session_id`      bigint(20)   NOT NULL COMMENT '所属会话ID',
  `event_type`      varchar(60)  NOT NULL COMMENT '事件类型: focus/blur/paste/copy_blocked/fullscreen_exit/visibility_change/…',
  `item_code`       varchar(100) DEFAULT NULL COMMENT '事件发生时当前题目编码',
  `event_payload`   text         DEFAULT NULL COMMENT '事件附加数据（JSON）',
  `occurred_at`     datetime(3)  DEFAULT NULL COMMENT '事件发生时间（毫秒精度）',
  `elapsed_seconds` int(11)      DEFAULT NULL COMMENT '事件发生时的累计用时（秒）',
  `tenant_id`       varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`       varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`event_id`),
  KEY `idx_assessment_event_session` (`session_id`),
  KEY `idx_assessment_event_type` (`event_type`),
  KEY `idx_assessment_event_occurred_at` (`occurred_at`),
  KEY `idx_assessment_event_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='作答事件日志表（行为埋点）';

-- =============================================
-- 作弊/风险标记表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_cheat_flag`;
CREATE TABLE `sys_assessment_cheat_flag`
(
  `flag_id`           bigint(20)   NOT NULL COMMENT '标记ID',
  `session_id`        bigint(20)   NOT NULL COMMENT '关联会话ID',
  `rule_code`         varchar(100) NOT NULL COMMENT '触发的风控规则编码',
  `rule_desc`         varchar(500) DEFAULT NULL COMMENT '规则描述',
  `risk_level`        varchar(20)  DEFAULT 'low' COMMENT '风险等级: none/low/medium/high/confirmed',
  `evidence`          text         DEFAULT NULL COMMENT '证据/触发详情（JSON）',
  `detected_at`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `reviewed_by`       varchar(64)  DEFAULT NULL COMMENT '人工复核人',
  `reviewed_at`       datetime     DEFAULT NULL COMMENT '人工复核时间',
  `review_conclusion` varchar(30)  DEFAULT 'pending' COMMENT '复核结论: confirmed_cheat/false_positive/pending',
  `review_note`       varchar(500) DEFAULT NULL COMMENT '复核备注',
  `tenant_id`         varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`         varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`           char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`flag_id`),
  KEY `idx_assessment_cheat_session` (`session_id`),
  KEY `idx_assessment_cheat_risk` (`risk_level`),
  KEY `idx_assessment_cheat_conclusion` (`review_conclusion`),
  KEY `idx_assessment_cheat_detected_at` (`detected_at`),
  KEY `idx_assessment_cheat_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='作弊/风险标记表';

-- =============================================
-- 常模集表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_norm_set`;
CREATE TABLE `sys_assessment_norm_set`
(
  `norm_set_id`       bigint(20)   NOT NULL COMMENT '常模集ID',
  `form_id`           bigint(20)   NOT NULL COMMENT '所属测评ID',
  `norm_set_name`     varchar(200) NOT NULL COMMENT '常模集名称',
  `norm_set_code`     varchar(100) NOT NULL COMMENT '常模集编码',
  `sample_size`       int(11)      DEFAULT NULL COMMENT '样本量',
  `collection_start`  date         DEFAULT NULL COMMENT '样本采集开始日期',
  `collection_end`    date         DEFAULT NULL COMMENT '样本采集结束日期',
  `source_desc`       text         DEFAULT NULL COMMENT '数据来源说明',
  `applicability_desc` text        DEFAULT NULL COMMENT '适用范围说明',
  `is_default`        tinyint(1)   DEFAULT 0 COMMENT '是否为测评默认常模集（0-否，1-是）',
  `status`            tinyint(1)   DEFAULT 1 COMMENT '状态: 1=启用 0=停用',
  `tenant_id`         varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`         varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`           char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`norm_set_id`),
  KEY `idx_assessment_norm_set_form` (`form_id`),
  KEY `idx_assessment_norm_set_status` (`status`),
  KEY `idx_assessment_norm_set_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='常模集表';

-- =============================================
-- 常模分层表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_norm_segment`;
CREATE TABLE `sys_assessment_norm_segment`
(
  `segment_id`         bigint(20)   NOT NULL COMMENT '分层ID',
  `norm_set_id`        bigint(20)   NOT NULL COMMENT '所属常模集ID',
  `segment_code`       varchar(100) NOT NULL COMMENT '分层编码（常模集内唯一）',
  `segment_name`       varchar(200) NOT NULL COMMENT '分层名称（如 男性18-25岁）',
  `gender_filter`      varchar(10)  DEFAULT NULL COMMENT '性别过滤（null=不限）',
  `age_min`            int(11)      DEFAULT NULL COMMENT '年龄下限（null=不限）',
  `age_max`            int(11)      DEFAULT NULL COMMENT '年龄上限（null=不限）',
  `education_filter`   varchar(30)  DEFAULT NULL COMMENT '学历过滤（null=不限）',
  `region_code_filter` varchar(30)  DEFAULT NULL COMMENT '地区编码过滤（支持前缀匹配）',
  `sample_size`        int(11)      DEFAULT NULL COMMENT '本分层样本量',
  `extra_filter`       text         DEFAULT NULL COMMENT '扩展过滤条件（JSON）',
  `sort_priority`      int(11)      DEFAULT 100 COMMENT '匹配优先级（数字越小越先匹配）',
  `tenant_id`          varchar(64)  DEFAULT NULL COMMENT '租户ID',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            char(1)      DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`segment_id`),
  KEY `idx_assessment_norm_segment_set` (`norm_set_id`),
  KEY `idx_assessment_norm_segment_priority` (`norm_set_id`, `sort_priority`),
  KEY `idx_assessment_norm_segment_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='常模分层表';

-- =============================================
-- 常模转换映射表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_norm_conversion`;
CREATE TABLE `sys_assessment_norm_conversion`
(
  `conversion_id`  bigint(20)    NOT NULL COMMENT '映射ID',
  `segment_id`     bigint(20)    NOT NULL COMMENT '所属分层ID',
  `dimension_id`   bigint(20)    DEFAULT NULL COMMENT '所属维度ID（null 表示总分转换）',
  `norm_score_type` varchar(30)  NOT NULL COMMENT '目标标准分类型: t_score/z_score/percentile/stanine/sten/grade_equivalent/raw_grade',
  `raw_score_min`  decimal(10,4) DEFAULT NULL COMMENT '原始分区间下限（区间映射时使用）',
  `raw_score_max`  decimal(10,4) DEFAULT NULL COMMENT '原始分区间上限（区间映射时使用）',
  `raw_score_exact` decimal(10,4) DEFAULT NULL COMMENT '精确原始分（精确查表时使用）',
  `standard_score` decimal(10,4) DEFAULT NULL COMMENT '对应的标准分值',
  `percentile`     decimal(6,2)  DEFAULT NULL COMMENT '对应的百分位（0-100）',
  `grade_label`    varchar(100)  DEFAULT NULL COMMENT '对应的等级标签',
  `sort_order`     int(11)       DEFAULT 0 COMMENT '排序值',
  `tenant_id`      varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`      varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`    datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`    datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`conversion_id`),
  KEY `idx_assessment_norm_conv_segment` (`segment_id`),
  KEY `idx_assessment_norm_conv_dimension` (`dimension_id`),
  KEY `idx_assessment_norm_conv_type` (`segment_id`, `norm_score_type`),
  KEY `idx_assessment_norm_conv_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='常模转换映射表';

-- =============================================
-- 题目项目分析统计表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_item_stat`;
CREATE TABLE `sys_assessment_item_stat`
(
  `stat_id`              bigint(20)    NOT NULL COMMENT '统计记录ID',
  `item_id`              bigint(20)    NOT NULL COMMENT '题目ID',
  `version_id`           bigint(20)    NOT NULL COMMENT '版本ID',
  `publish_id`           bigint(20)    DEFAULT NULL COMMENT '发布批次ID（null 表示汇总所有批次）',
  `sample_size`          int(11)       DEFAULT 0 COMMENT '统计样本量',
  `item_difficulty`      decimal(8,6)  DEFAULT NULL COMMENT '难度系数 P（0-1，CTT）',
  `item_discrimination`  decimal(8,6)  DEFAULT NULL COMMENT '区分度 D（点二列相关）',
  `missing_rate`         decimal(8,6)  DEFAULT NULL COMMENT '缺失率（0-1）',
  `ceiling_rate`         decimal(8,6)  DEFAULT NULL COMMENT '天花板效应比例',
  `floor_rate`           decimal(8,6)  DEFAULT NULL COMMENT '地板效应比例',
  `alpha_if_deleted`     decimal(8,6)  DEFAULT NULL COMMENT '删除该题后的量表 alpha 系数',
  `omega_if_deleted`     decimal(8,6)  DEFAULT NULL COMMENT '删除该题后的量表 omega 系数',
  `dif_flag`             varchar(10)   DEFAULT 'none' COMMENT 'DIF 标记（A/B/C 或 none）',
  `dif_reference_group`  varchar(100)  DEFAULT NULL COMMENT 'DIF 参照组描述',
  `stat_json`            longtext      DEFAULT NULL COMMENT '完整统计结果 JSON（含 IRTparams 等扩展数据）',
  `computed_at`          datetime      DEFAULT NULL COMMENT '统计计算时间',
  `tenant_id`            varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`            varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`          datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`            varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`          datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`              char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`stat_id`),
  KEY `idx_assessment_item_stat_item` (`item_id`),
  KEY `idx_assessment_item_stat_version` (`version_id`),
  KEY `idx_assessment_item_stat_publish` (`publish_id`),
  KEY `idx_assessment_item_stat_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目项目分析统计表';

-- =============================================
-- 等值/链接记录表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_equating_record`;
CREATE TABLE `sys_assessment_equating_record`
(
  `equating_id`          bigint(20)    NOT NULL COMMENT '等值记录ID',
  `form_id`              bigint(20)    NOT NULL COMMENT '所属测评ID',
  `source_version_id`    bigint(20)    NOT NULL COMMENT '源版本ID（被等值的旧版本）',
  `target_version_id`    bigint(20)    NOT NULL COMMENT '目标版本ID（新版本）',
  `equating_method`      varchar(50)   NOT NULL COMMENT '等值方法: mean_sigma/irt_linking/equipercentile/…',
  `anchor_item_codes`    text          DEFAULT NULL COMMENT '锚题编码列表（JSON 数组）',
  `anchor_count`         int(11)       DEFAULT 0 COMMENT '锚题数量',
  `slope`                decimal(12,6) DEFAULT NULL COMMENT '等值系数 A（线性等值斜率）',
  `intercept`            decimal(12,6) DEFAULT NULL COMMENT '等值系数 B（线性等值截距）',
  `rmsea`                decimal(10,6) DEFAULT NULL COMMENT 'RMSEA（拟合指数）',
  `equating_result_json` longtext      DEFAULT NULL COMMENT '完整等值结果 JSON',
  `computed_at`          datetime      DEFAULT NULL COMMENT '计算时间',
  `computed_by`          varchar(64)   DEFAULT NULL COMMENT '操作人',
  `tenant_id`            varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`            varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`          datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`            varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`          datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`              char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`equating_id`),
  KEY `idx_assessment_equating_form` (`form_id`),
  KEY `idx_assessment_equating_source_ver` (`source_version_id`),
  KEY `idx_assessment_equating_target_ver` (`target_version_id`),
  KEY `idx_assessment_equating_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='等值/链接记录表';

-- =============================================
-- BI 预聚合快照表
-- =============================================
DROP TABLE IF EXISTS `sys_assessment_bi_snapshot`;
CREATE TABLE `sys_assessment_bi_snapshot`
(
  `snapshot_id`            bigint(20)    NOT NULL COMMENT '快照ID',
  `form_id`                bigint(20)    NOT NULL COMMENT '测评ID',
  `publish_id`             bigint(20)    DEFAULT NULL COMMENT '发布批次ID（null 表示全批次汇总）',
  `snapshot_type`          varchar(30)   NOT NULL COMMENT '快照类型: daily/weekly/publish_close/manual',
  `snapshot_date`          datetime      NOT NULL COMMENT '快照日期（截止时间）',
  `total_sessions`         bigint(20)    DEFAULT 0 COMMENT '总会话数',
  `completed_sessions`     bigint(20)    DEFAULT 0 COMMENT '已完成会话数',
  `avg_score`              decimal(10,4) DEFAULT NULL COMMENT '平均总分',
  `metrics_json`           longtext      DEFAULT NULL COMMENT '扩展指标 JSON（由 ETL 模块填充）',
  `dimension_stats_json`   longtext      DEFAULT NULL COMMENT '分维度统计 JSON',
  `demographic_stats_json` longtext      DEFAULT NULL COMMENT '人口学分层统计 JSON',
  `tenant_id`              varchar(64)   DEFAULT NULL COMMENT '租户ID',
  `create_by`              varchar(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`            datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`              varchar(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`            datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`                char(1)       DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_assessment_bi_snapshot_form` (`form_id`),
  KEY `idx_assessment_bi_snapshot_publish` (`publish_id`),
  KEY `idx_assessment_bi_snapshot_type` (`snapshot_type`),
  KEY `idx_assessment_bi_snapshot_date` (`snapshot_date`),
  KEY `idx_assessment_bi_snapshot_tenant` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='BI 预聚合快照表';
