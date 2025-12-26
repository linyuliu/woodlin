-- =============================================
-- Oracle version - Auto-converted from MySQL
-- Source: woodlin_schema.sql
-- Database: Oracle 12c+
-- =============================================

-- =============================================
-- Woodlin 多租户中后台管理系统数据库脚本
-- 作者: mumu
-- 描述: 数据库表结构创建脚本
-- 版本: 1.0.0
-- 时间: 2025-01-01
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS woodlin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ALTER SESSION SET CURRENT_SCHEMA = woodlin;

-- =============================================
-- 租户管理表
-- =============================================
DROP TABLE sys_tenant CASCADE CONSTRAINTS;
CREATE TABLE sys_tenant (
    tenant_id varchar(64) NOT NULL COMMENT '租户ID',
    tenant_name varchar(100) NOT NULL COMMENT '租户名称',
    tenant_code varchar(50) NOT NULL COMMENT '租户编码',
    contact_name varchar(50) DEFAULT NULL COMMENT '联系人',
    contact_phone varchar(20) DEFAULT NULL COMMENT '联系电话',
    contact_email varchar(100) DEFAULT NULL COMMENT '联系邮箱',
    status char(1) DEFAULT '1' COMMENT '租户状态（1-启用，0-禁用）',
    expire_time datetime DEFAULT NULL COMMENT '过期时间',
    user_limit int(11) DEFAULT 100 COMMENT '用户数量限制',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (tenant_id),
    UNIQUE KEY uk_tenant_code (tenant_code),
    KEY idx_tenant_status (status),
    KEY idx_create_time (create_time)
) -- Comment: 租户信息表;

-- =============================================
-- 部门管理表
-- =============================================
DROP TABLE sys_dept CASCADE CONSTRAINTS;
CREATE TABLE sys_dept (
    dept_id bigint(20) NOT NULL COMMENT '部门ID',
    parent_id bigint(20) DEFAULT 0 COMMENT '父部门ID',
    ancestors varchar(500) DEFAULT '' COMMENT '祖级列表',
    dept_name varchar(30) NOT NULL COMMENT '部门名称',
    dept_code varchar(50) DEFAULT NULL COMMENT '部门编码',
    sort_order int(11) DEFAULT 0 COMMENT '显示顺序',
    leader varchar(20) DEFAULT NULL COMMENT '负责人',
    phone varchar(11) DEFAULT NULL COMMENT '联系电话',
    email varchar(50) DEFAULT NULL COMMENT '邮箱',
    status char(1) DEFAULT '1' COMMENT '部门状态（1-启用，0-禁用）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (dept_id),
    KEY idx_parent_id (parent_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status)
) -- Comment: 部门表;

-- =============================================
-- 用户信息表
-- =============================================
DROP TABLE sys_user CASCADE CONSTRAINTS;
CREATE TABLE sys_user (
    user_id bigint(20) NOT NULL COMMENT '用户ID',
    username varchar(30) NOT NULL COMMENT '用户账号',
    nickname varchar(30) NOT NULL COMMENT '用户昵称',
    real_name varchar(30) DEFAULT NULL COMMENT '真实姓名',
    password varchar(100) DEFAULT '' COMMENT '密码',
    email varchar(50) DEFAULT '' COMMENT '用户邮箱',
    mobile varchar(11) DEFAULT '' COMMENT '手机号码',
    avatar varchar(200) DEFAULT '' COMMENT '头像路径',
    gender tinyint(1) DEFAULT 0 COMMENT '用户性别（GB/T 2261.1标准：0-未知的性别，1-男性，2-女性，9-未说明的性别）',
    birthday datetime DEFAULT NULL COMMENT '生日',
    status char(1) DEFAULT '1' COMMENT '帐号状态（1-启用，0-禁用）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
    last_login_time datetime DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip varchar(128) DEFAULT '' COMMENT '最后登录IP',
    login_count int(11) DEFAULT 0 COMMENT '登录次数',
    pwd_error_count int(11) DEFAULT 0 COMMENT '密码错误次数',
    lock_time datetime DEFAULT NULL COMMENT '账号锁定时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status)
) -- Comment: 用户信息表;

-- =============================================
-- 角色信息表
-- =============================================
DROP TABLE sys_role CASCADE CONSTRAINTS;
CREATE TABLE sys_role (
    role_id bigint(20) NOT NULL COMMENT '角色ID',
    role_name varchar(30) NOT NULL COMMENT '角色名称',
    role_code varchar(100) NOT NULL COMMENT '角色权限字符串',
    sort_order int(11) DEFAULT 0 COMMENT '显示顺序',
    data_scope char(1) DEFAULT '1' COMMENT '数据范围（1-全部数据权限，2-自定数据权限，3-本部门数据权限，4-本部门及以下数据权限，5-仅本人数据权限）',
    status char(1) DEFAULT '1' COMMENT '角色状态（1-启用，0-禁用）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status)
) -- Comment: 角色信息表;

-- =============================================
-- 权限信息表
-- =============================================
DROP TABLE sys_permission CASCADE CONSTRAINTS;
CREATE TABLE sys_permission (
    permission_id bigint(20) NOT NULL COMMENT '权限ID',
    parent_id bigint(20) DEFAULT 0 COMMENT '父权限ID',
    permission_name varchar(50) NOT NULL COMMENT '权限名称',
    permission_code varchar(100) DEFAULT NULL COMMENT '权限标识',
    permission_type char(1) DEFAULT 'M' COMMENT '权限类型（M-目录，C-菜单，F-按钮）',
    path varchar(200) DEFAULT '' COMMENT '路由地址',
    component varchar(255) DEFAULT NULL COMMENT '组件路径',
    icon varchar(100) DEFAULT '#' COMMENT '权限图标',
    sort_order int(11) DEFAULT 0 COMMENT '显示顺序',
    status char(1) DEFAULT '1' COMMENT '权限状态（1-启用，0-禁用）',
    is_frame char(1) DEFAULT '0' COMMENT '是否为外链（0-否，1-是）',
    is_cache char(1) DEFAULT '0' COMMENT '是否缓存（0-不缓存，1-缓存）',
    visible char(1) DEFAULT '1' COMMENT '显示状态（0-隐藏，1-显示）',
    remark varchar(500) DEFAULT '' COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (permission_id),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) -- Comment: 权限表;

-- =============================================
-- 用户和角色关联表
-- =============================================
DROP TABLE sys_user_role CASCADE CONSTRAINTS;
CREATE TABLE sys_user_role (
    user_id bigint(20) NOT NULL COMMENT '用户ID',
    role_id bigint(20) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) -- Comment: 用户和角色关联表;

-- =============================================
-- 角色和权限关联表
-- =============================================
DROP TABLE sys_role_permission CASCADE CONSTRAINTS;
CREATE TABLE sys_role_permission (
    role_id bigint(20) NOT NULL COMMENT '角色ID',
    permission_id bigint(20) NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id)
) -- Comment: 角色和权限关联表;

-- =============================================
-- 角色和部门关联表（数据权限）
-- =============================================
DROP TABLE sys_role_dept CASCADE CONSTRAINTS;
CREATE TABLE sys_role_dept (
    role_id bigint(20) NOT NULL COMMENT '角色ID',
    dept_id bigint(20) NOT NULL COMMENT '部门ID',
    PRIMARY KEY (role_id, dept_id)
) -- Comment: 角色和部门关联表;

-- =============================================
-- 系统配置表
-- =============================================
DROP TABLE sys_config CASCADE CONSTRAINTS;
CREATE TABLE sys_config (
    config_id bigint(20) NOT NULL COMMENT '参数主键',
    config_name varchar(100) DEFAULT '' COMMENT '参数名称',
    config_key varchar(100) DEFAULT '' COMMENT '参数键名',
    config_value varchar(500) DEFAULT '' COMMENT '参数键值',
    config_type char(1) DEFAULT 'N' COMMENT '系统内置（Y-是，N-否）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_tenant_id (tenant_id)
) -- Comment: 参数配置表;

-- =============================================
-- 操作日志表
-- =============================================
DROP TABLE sys_oper_log CASCADE CONSTRAINTS;
CREATE TABLE sys_oper_log (
    oper_id bigint(20) NOT NULL COMMENT '日志主键',
    title varchar(50) DEFAULT '' COMMENT '模块标题',
    business_type int(2) DEFAULT 0 COMMENT '业务类型（0-其它，1-新增，2-修改，3-删除）',
    method varchar(200) DEFAULT '' COMMENT '方法名称',
    request_method varchar(10) DEFAULT '' COMMENT '请求方式',
    oper_url varchar(255) DEFAULT '' COMMENT '请求URL',
    oper_ip varchar(128) DEFAULT '' COMMENT '主机地址',
    oper_location varchar(255) DEFAULT '' COMMENT '操作地点',
    oper_param varchar(2000) DEFAULT '' COMMENT '请求参数',
    json_result varchar(2000) DEFAULT '' COMMENT '返回参数',
    status int(1) DEFAULT 0 COMMENT '操作状态（0-正常，1-异常）',
    error_msg varchar(2000) DEFAULT '' COMMENT '错误消息',
    oper_time datetime DEFAULT SYSTIMESTAMP COMMENT '操作时间',
    cost_time bigint(20) DEFAULT 0 COMMENT '消耗时间（毫秒）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (oper_id),
    KEY idx_business_type (business_type),
    KEY idx_status (status),
    KEY idx_oper_time (oper_time),
    KEY idx_tenant_id (tenant_id)
) -- Comment: 操作日志记录;

-- =============================================
-- 文件信息表
-- =============================================
DROP TABLE sys_file CASCADE CONSTRAINTS;
CREATE TABLE sys_file (
    file_id bigint(20) NOT NULL COMMENT '文件ID',
    file_name varchar(100) NOT NULL COMMENT '文件名称',
    original_name varchar(100) DEFAULT NULL COMMENT '原始文件名',
    file_path varchar(255) NOT NULL COMMENT '文件路径',
    file_url varchar(500) DEFAULT NULL COMMENT '文件URL',
    file_size bigint(20) DEFAULT 0 COMMENT '文件大小（字节）',
    file_type varchar(50) DEFAULT NULL COMMENT '文件类型',
    mime_type varchar(100) DEFAULT NULL COMMENT 'MIME类型',
    file_md5 varchar(32) DEFAULT NULL COMMENT '文件MD5',
    storage_type varchar(20) DEFAULT 'local' COMMENT '存储位置（local-本地，minio-MinIO，oss-阿里云OSS）',
    is_image char(1) DEFAULT '0' COMMENT '是否为图片（0-否，1-是）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (file_id),
    KEY idx_file_md5 (file_md5),
    KEY idx_tenant_id (tenant_id),
    KEY idx_create_time (create_time)
) -- Comment: 文件信息表;

-- =============================================
-- 定时任务表
-- =============================================
DROP TABLE sys_job CASCADE CONSTRAINTS;
CREATE TABLE sys_job (
    job_id bigint(20) NOT NULL COMMENT '任务ID',
    job_name varchar(64) NOT NULL DEFAULT '' COMMENT '任务名称',
    job_group varchar(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target varchar(500) NOT NULL COMMENT '调用目标字符串',
    cron_expression varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
    misfire_policy varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1-立即执行，2-执行一次，3-放弃执行）',
    concurrent char(1) DEFAULT '1' COMMENT '是否并发执行（0-禁止，1-允许）',
    status char(1) DEFAULT '0' COMMENT '状态（0-暂停，1-正常）',
    next_execute_time datetime DEFAULT NULL COMMENT '下次执行时间',
    last_execute_time datetime DEFAULT NULL COMMENT '上次执行时间',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT '' COMMENT '备注信息',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (job_id, job_name, job_group),
    KEY idx_status (status),
    KEY idx_tenant_id (tenant_id)
) -- Comment: 定时任务调度表;

-- =============================================
-- 代码生成业务表
-- =============================================
DROP TABLE gen_table CASCADE CONSTRAINTS;
CREATE TABLE gen_table (
    table_id bigint(20) NOT NULL COMMENT '编号',
    table_name varchar(200) DEFAULT '' COMMENT '表名称',
    table_comment varchar(500) DEFAULT '' COMMENT '表描述',
    class_name varchar(100) DEFAULT '' COMMENT '实体类名称',
    package_name varchar(100) DEFAULT NULL COMMENT '生成包路径',
    module_name varchar(30) DEFAULT NULL COMMENT '生成模块名',
    business_name varchar(30) DEFAULT NULL COMMENT '生成业务名',
    function_name varchar(50) DEFAULT NULL COMMENT '生成功能名',
    function_author varchar(50) DEFAULT NULL COMMENT '生成功能作者',
    gen_type char(1) DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
    gen_path varchar(200) DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
    pk_column varchar(100) DEFAULT NULL COMMENT '主键信息',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT SYSTIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT SYSTIMESTAMP ON UPDATE SYSTIMESTAMP COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (table_id)
) -- Comment: 代码生成业务表;