-- =============================================
-- PostgreSQL version - Auto-converted from MySQL
-- Source: oss_management_schema.sql
-- Database: PostgreSQL 12+
-- =============================================

-- =============================================
-- OSS资源管理系统数据库脚本
-- 作者: mumu
-- 描述: OSS存储平台管理、上传策略、文件元数据等表结构
-- 版本: 1.0.0
-- 时间: 2025-01-30
-- =============================================

-- \c woodlin;

-- =============================================
-- 存储平台配置表
-- =============================================
DROP TABLE IF EXISTS sys_storage_config CASCADE;
CREATE TABLE sys_storage_config (
    config_id bigint(20) NOT NULL COMMENT '配置ID',
    config_name varchar(100) NOT NULL COMMENT '配置名称',
    storage_type varchar(20) NOT NULL COMMENT '存储类型（local-本地，minio-MinIO，s3-AWS S3，oss-阿里云OSS，cos-腾讯云COS，obs-华为云OBS）',
    access_key varchar(200) DEFAULT NULL COMMENT '访问密钥',
    secret_key varchar(500) DEFAULT NULL COMMENT '密钥（加密存储）',
    endpoint varchar(500) DEFAULT NULL COMMENT '终端节点地址',
    bucket_name varchar(100) DEFAULT NULL COMMENT '存储桶名称',
    region varchar(50) DEFAULT NULL COMMENT '区域',
    base_path varchar(200) DEFAULT '/' COMMENT '基础路径',
    domain varchar(500) DEFAULT NULL COMMENT '自定义域名',
    is_default char(1) DEFAULT '0' COMMENT '是否为默认配置（0-否，1-是）',
    is_public char(1) DEFAULT '0' COMMENT '是否公开访问（0-私有，1-公开）',
    status char(1) DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
    max_file_size bigint(20) DEFAULT 104857600 COMMENT '最大文件大小（字节），默认100MB',
    allowed_extensions varchar(500) DEFAULT NULL COMMENT '允许的文件扩展名，逗号分隔',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (config_id),
    KEY idx_storage_type (storage_type),
    KEY idx_tenant_id (tenant_id),
    KEY idx_is_default (is_default)
) -- Comment: 存储平台配置表;

-- =============================================
-- 上传策略表
-- =============================================
DROP TABLE IF EXISTS sys_upload_policy CASCADE;
CREATE TABLE sys_upload_policy (
    policy_id bigint(20) NOT NULL COMMENT '策略ID',
    policy_name varchar(100) NOT NULL COMMENT '策略名称',
    policy_code varchar(50) NOT NULL COMMENT '策略编码',
    storage_config_id bigint(20) NOT NULL COMMENT '存储配置ID',
    detect_file_type char(1) DEFAULT '0' COMMENT '是否检测文件真实类型（0-否，1-是，使用Apache Tika）',
    check_file_size char(1) DEFAULT '1' COMMENT '是否检查文件大小（0-否，1-是）',
    max_file_size bigint(20) DEFAULT 104857600 COMMENT '最大文件大小（字节），默认100MB',
    allowed_extensions varchar(500) DEFAULT NULL COMMENT '允许的文件扩展名，逗号分隔，如：jpg,png,pdf',
    allowed_mime_types varchar(500) DEFAULT NULL COMMENT '允许的MIME类型，逗号分隔',
    check_md5 char(1) DEFAULT '1' COMMENT '是否校验MD5（0-否，1-是）',
    allow_duplicate char(1) DEFAULT '1' COMMENT '是否允许重复上传（0-否，1-是）',
    generate_thumbnail char(1) DEFAULT '0' COMMENT '是否生成缩略图（0-否，1-是）',
    thumbnail_width int(11) DEFAULT 200 COMMENT '缩略图宽度',
    thumbnail_height int(11) DEFAULT 200 COMMENT '缩略图高度',
    path_pattern varchar(200) DEFAULT '/{yyyy}/{MM}/{dd}/' COMMENT '文件路径模式，支持占位符：{yyyy}年，{MM}月，{dd}日，{userId}用户ID，{tenantId}租户ID',
    file_name_pattern varchar(100) DEFAULT '{timestamp}_{random}' COMMENT '文件名模式，支持占位符：{original}原始文件名，{timestamp}时间戳，{random}随机字符串，{uuid}UUID',
    signature_expires int(11) DEFAULT 3600 COMMENT '签名有效期（秒），默认1小时',
    callback_url varchar(500) DEFAULT NULL COMMENT '上传回调地址',
    status char(1) DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_policy_code (policy_code),
    KEY idx_storage_config_id (storage_config_id),
    KEY idx_tenant_id (tenant_id)
) -- Comment: 上传策略表;

-- =============================================
-- 更新文件信息表（增强版）
-- =============================================
DROP TABLE IF EXISTS sys_file CASCADE;
CREATE TABLE sys_file (
    file_id bigint(20) NOT NULL COMMENT '文件ID',
    file_name varchar(100) NOT NULL COMMENT '文件名称',
    original_name varchar(100) DEFAULT NULL COMMENT '原始文件名',
    file_path varchar(255) NOT NULL COMMENT '文件路径',
    file_url varchar(500) DEFAULT NULL COMMENT '文件URL',
    file_size bigint(20) DEFAULT 0 COMMENT '文件大小（字节）',
    file_extension varchar(20) DEFAULT NULL COMMENT '文件扩展名',
    file_type varchar(50) DEFAULT NULL COMMENT '文件类型',
    mime_type varchar(100) DEFAULT NULL COMMENT 'MIME类型',
    detected_mime_type varchar(100) DEFAULT NULL COMMENT '检测到的真实MIME类型（通过Apache Tika）',
    file_md5 varchar(32) DEFAULT NULL COMMENT '文件MD5',
    file_sha256 varchar(64) DEFAULT NULL COMMENT '文件SHA256',
    storage_type varchar(20) DEFAULT 'local' COMMENT '存储位置（local-本地，minio-MinIO，s3-AWS S3，oss-阿里云OSS，cos-腾讯云COS，obs-华为云OBS）',
    storage_config_id bigint(20) DEFAULT NULL COMMENT '存储配置ID',
    upload_policy_id bigint(20) DEFAULT NULL COMMENT '上传策略ID',
    bucket_name varchar(100) DEFAULT NULL COMMENT '存储桶名称',
    object_key varchar(500) DEFAULT NULL COMMENT '对象键',
    is_image char(1) DEFAULT '0' COMMENT '是否为图片（0-否，1-是）',
    image_width int(11) DEFAULT NULL COMMENT '图片宽度',
    image_height int(11) DEFAULT NULL COMMENT '图片高度',
    thumbnail_path varchar(255) DEFAULT NULL COMMENT '缩略图路径',
    thumbnail_url varchar(500) DEFAULT NULL COMMENT '缩略图URL',
    is_public char(1) DEFAULT '0' COMMENT '是否公开（0-否，1-是）',
    access_count int(11) DEFAULT 0 COMMENT '访问次数',
    last_access_time datetime DEFAULT NULL COMMENT '最后访问时间',
    expired_time datetime DEFAULT NULL COMMENT '过期时间',
    upload_ip varchar(128) DEFAULT NULL COMMENT '上传IP',
    user_agent varchar(500) DEFAULT NULL COMMENT '用户代理',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    business_type varchar(50) DEFAULT NULL COMMENT '业务类型',
    business_id varchar(100) DEFAULT NULL COMMENT '业务ID',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    create_by varchar(64) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by varchar(64) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
    deleted char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (file_id),
    KEY idx_file_md5 (file_md5),
    KEY idx_file_sha256 (file_sha256),
    KEY idx_tenant_id (tenant_id),
    KEY idx_storage_config_id (storage_config_id),
    KEY idx_upload_policy_id (upload_policy_id),
    KEY idx_business (business_type, business_id),
    KEY idx_create_time (create_time)
) -- Comment: 文件信息表;

-- =============================================
-- 上传令牌表（用于签名验证）
-- =============================================
DROP TABLE IF EXISTS sys_upload_token CASCADE;
CREATE TABLE sys_upload_token (
    token_id bigint(20) NOT NULL COMMENT '令牌ID',
    token varchar(200) NOT NULL COMMENT '上传令牌',
    policy_id bigint(20) NOT NULL COMMENT '上传策略ID',
    user_id bigint(20) DEFAULT NULL COMMENT '用户ID',
    signature varchar(500) NOT NULL COMMENT '签名',
    max_file_size bigint(20) DEFAULT NULL COMMENT '最大文件大小',
    allowed_extensions varchar(500) DEFAULT NULL COMMENT '允许的文件扩展名',
    expire_time datetime NOT NULL COMMENT '过期时间',
    is_used char(1) DEFAULT '0' COMMENT '是否已使用（0-否，1-是）',
    used_time datetime DEFAULT NULL COMMENT '使用时间',
    file_id bigint(20) DEFAULT NULL COMMENT '关联文件ID',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (token_id),
    UNIQUE KEY uk_token (token),
    KEY idx_policy_id (policy_id),
    KEY idx_user_id (user_id),
    KEY idx_expire_time (expire_time),
    KEY idx_tenant_id (tenant_id)
) -- Comment: 上传令牌表;

-- =============================================
-- 插入默认存储配置
-- =============================================
INSERT INTO sys_storage_config (
    config_id, config_name, storage_type, bucket_name, base_path, 
    is_default, is_public, status, max_file_size, remark, create_by
) VALUES (
    1, '本地存储', 'local', 'uploads', '/uploads/', 
    '1', '0', '1', 104857600, '默认本地文件存储配置', 'system'
);

-- =============================================
-- 插入默认上传策略
-- =============================================
INSERT INTO sys_upload_policy (
    policy_id, policy_name, policy_code, storage_config_id, 
    detect_file_type, check_file_size, max_file_size, 
    allowed_extensions, check_md5, allow_duplicate, 
    path_pattern, file_name_pattern, signature_expires, 
    status, remark, create_by
) VALUES (
    1, '通用上传策略', 'default', 1, 
    '0', '1', 104857600, 
    'jpg,jpeg,png,gif,bmp,webp,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar', 
    '1', '1', 
    '/{yyyy}/{MM}/{dd}/', '{timestamp}_{random}', 3600, 
    '1', '默认上传策略，适用于大多数文件上传场景', 'system'
);

INSERT INTO sys_upload_policy (
    policy_id, policy_name, policy_code, storage_config_id, 
    detect_file_type, check_file_size, max_file_size, 
    allowed_extensions, allowed_mime_types, check_md5, allow_duplicate, 
    generate_thumbnail, thumbnail_width, thumbnail_height,
    path_pattern, file_name_pattern, signature_expires, 
    status, remark, create_by
) VALUES (
    2, '图片上传策略', 'image', 1, 
    '1', '1', 10485760, 
    'jpg,jpeg,png,gif,bmp,webp', 
    'image/jpeg,image/png,image/gif,image/bmp,image/webp', 
    '1', '1', 
    '1', 200, 200,
    '/images/{yyyy}/{MM}/{dd}/', '{timestamp}_{random}', 3600, 
    '1', '图片上传策略，启用真实类型检测和缩略图生成', 'system'
);
