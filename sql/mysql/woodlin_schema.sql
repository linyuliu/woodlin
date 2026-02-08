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
-- 第七部分：可搜索加密示例表（可选）
-- =============================================

-- =============================================
-- 敏感数据表（可搜索加密示例）
-- =============================================
DROP TABLE IF EXISTS `sys_sensitive_data`;
CREATE TABLE `sys_sensitive_data`
(
  `data_id`                    BIGINT(20) NOT NULL COMMENT '数据ID',
  `real_name`                  VARCHAR(500)  DEFAULT NULL COMMENT '真实姓名（加密）',
  `real_name_search_index`     TEXT          DEFAULT NULL COMMENT '真实姓名搜索索引',
  `id_card`                    VARCHAR(500)  DEFAULT NULL COMMENT '身份证号（加密）',
  `mobile`                     VARCHAR(500)  DEFAULT NULL COMMENT '手机号（加密）',
  `mobile_search_index`        TEXT          DEFAULT NULL COMMENT '手机号搜索索引',
  `email_address`              VARCHAR(500)  DEFAULT NULL COMMENT '邮箱地址（加密）',
  `email_address_search_index` TEXT          DEFAULT NULL COMMENT '邮箱搜索索引',
  `home_address`               VARCHAR(1000) DEFAULT NULL COMMENT '家庭住址（加密）',
  `home_address_search_index`  TEXT          DEFAULT NULL COMMENT '家庭住址搜索索引',
  `bank_card`                  VARCHAR(500)  DEFAULT NULL COMMENT '银行卡号（加密）',
  `data_type`                  VARCHAR(50)   DEFAULT NULL COMMENT '数据类型',
  `status`                     CHAR(1)       DEFAULT '1' COMMENT '状态（1-正常，0-禁用）',
  `tenant_id`                  VARCHAR(20)   DEFAULT NULL COMMENT '租户ID',
  `remark`                     VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `create_by`                  VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  `create_time`                DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`                  VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  `update_time`                DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`                   TINYINT(1)    DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`data_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='敏感数据表（可搜索加密示例）';

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
  `column_mapping`      text         DEFAULT NULL COMMENT '字段映射配置（JSON格式）',
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
