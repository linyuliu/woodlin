-- =============================================
-- Woodlin 多租户中后台管理系统数据库脚本 (PostgreSQL)
-- 作者: mumu
-- 描述: PostgreSQL 数据库表结构创建脚本
-- 版本: 1.0.0
-- 时间: 2025-01-01
-- 数据库: PostgreSQL 12+
-- 说明: 从 MySQL 转换而来，已适配 PostgreSQL 语法
-- =============================================

-- 创建数据库（如果不存在）
-- 注意：在 PostgreSQL 中，需要以超级用户身份执行此命令
-- CREATE DATABASE woodlin WITH ENCODING 'UTF8' LC_COLLATE='zh_CN.UTF-8' LC_CTYPE='zh_CN.UTF-8' TEMPLATE=template0;

-- 连接到 woodlin 数据库
-- \c woodlin;

-- =============================================
-- 租户管理表
-- 说明: 存储多租户系统的租户信息，包括租户基本信息、联系方式、状态等
-- =============================================
DROP TABLE IF EXISTS sys_tenant CASCADE;
CREATE TABLE sys_tenant (
    tenant_id VARCHAR(64) NOT NULL,  -- 租户唯一标识
    tenant_name VARCHAR(100) NOT NULL,  -- 租户名称
    tenant_code VARCHAR(50) NOT NULL,  -- 租户编码，用于系统识别
    contact_name VARCHAR(50),  -- 租户联系人姓名
    contact_phone VARCHAR(20),  -- 租户联系电话
    contact_email VARCHAR(100),  -- 租户联系邮箱
    status CHAR(1) DEFAULT '1',  -- 租户状态：1-启用，0-禁用
    expire_time TIMESTAMP,  -- 租户过期时间
    user_limit INTEGER DEFAULT 100,  -- 租户用户数量限制
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (tenant_id),
    CONSTRAINT uk_tenant_code UNIQUE (tenant_code)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_tenant_status ON sys_tenant(status);
CREATE INDEX idx_tenant_create_time ON sys_tenant(create_time);

-- 添加表注释
COMMENT ON TABLE sys_tenant IS '租户信息表';
COMMENT ON COLUMN sys_tenant.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN sys_tenant.tenant_code IS '租户编码';
COMMENT ON COLUMN sys_tenant.contact_name IS '联系人';
COMMENT ON COLUMN sys_tenant.contact_phone IS '联系电话';
COMMENT ON COLUMN sys_tenant.contact_email IS '联系邮箱';
COMMENT ON COLUMN sys_tenant.status IS '租户状态（1-启用，0-禁用）';
COMMENT ON COLUMN sys_tenant.expire_time IS '过期时间';
COMMENT ON COLUMN sys_tenant.user_limit IS '用户数量限制';

-- =============================================
-- 部门管理表
-- 说明: 存储组织架构的部门信息，支持树形结构
-- =============================================
DROP TABLE IF EXISTS sys_dept CASCADE;
CREATE TABLE sys_dept (
    dept_id BIGINT NOT NULL,  -- 部门唯一标识
    parent_id BIGINT DEFAULT 0,  -- 父部门ID，0表示顶级部门
    ancestors VARCHAR(500) DEFAULT '',  -- 祖级列表，用于快速查询层级关系
    dept_name VARCHAR(30) NOT NULL,  -- 部门名称
    dept_code VARCHAR(50),  -- 部门编码
    sort_order INTEGER DEFAULT 0,  -- 显示顺序
    leader VARCHAR(20),  -- 部门负责人
    phone VARCHAR(11),  -- 联系电话
    email VARCHAR(50),  -- 邮箱地址
    status CHAR(1) DEFAULT '1',  -- 部门状态：1-启用，0-禁用
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (dept_id)
);

-- 创建索引
CREATE INDEX idx_dept_parent_id ON sys_dept(parent_id);
CREATE INDEX idx_dept_tenant_id ON sys_dept(tenant_id);
CREATE INDEX idx_dept_status ON sys_dept(status);

-- 添加表注释
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.dept_id IS '部门ID';
COMMENT ON COLUMN sys_dept.parent_id IS '父部门ID';
COMMENT ON COLUMN sys_dept.ancestors IS '祖级列表';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';

-- =============================================
-- 用户信息表
-- 说明: 存储系统用户的基本信息、登录凭证、状态等
-- =============================================
DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
    user_id BIGINT NOT NULL,  -- 用户唯一标识
    username VARCHAR(30) NOT NULL,  -- 用户账号，用于登录
    nickname VARCHAR(30) NOT NULL,  -- 用户昵称，显示名称
    real_name VARCHAR(30),  -- 用户真实姓名
    password VARCHAR(100) DEFAULT '',  -- 密码（加密存储）
    email VARCHAR(50) DEFAULT '',  -- 用户邮箱
    mobile VARCHAR(11) DEFAULT '',  -- 手机号码
    avatar VARCHAR(200) DEFAULT '',  -- 头像图片路径
    gender SMALLINT DEFAULT 0,  -- 用户性别：0-未知，1-男，2-女
    birthday TIMESTAMP,  -- 生日
    status CHAR(1) DEFAULT '1',  -- 账号状态：1-启用，0-禁用
    tenant_id VARCHAR(64),  -- 所属租户ID
    dept_id BIGINT,  -- 所属部门ID
    last_login_time TIMESTAMP,  -- 最后登录时间
    last_login_ip VARCHAR(128) DEFAULT '',  -- 最后登录IP地址
    login_count INTEGER DEFAULT 0,  -- 累计登录次数
    pwd_error_count INTEGER DEFAULT 0,  -- 密码错误次数
    lock_time TIMESTAMP,  -- 账号锁定时间
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (user_id),
    CONSTRAINT uk_username UNIQUE (username)
);

-- 创建索引
CREATE INDEX idx_user_dept_id ON sys_user(dept_id);
CREATE INDEX idx_user_tenant_id ON sys_user(tenant_id);
CREATE INDEX idx_user_status ON sys_user(status);

-- 添加表注释
COMMENT ON TABLE sys_user IS '用户信息表';
COMMENT ON COLUMN sys_user.user_id IS '用户ID';
COMMENT ON COLUMN sys_user.username IS '用户账号';
COMMENT ON COLUMN sys_user.nickname IS '用户昵称';
COMMENT ON COLUMN sys_user.password IS '密码';

-- =============================================
-- 角色信息表
-- 说明: 存储系统角色信息，用于权限控制
-- =============================================
DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
    role_id BIGINT NOT NULL,  -- 角色唯一标识
    role_name VARCHAR(30) NOT NULL,  -- 角色名称
    role_key VARCHAR(100) NOT NULL,  -- 角色权限字符串
    sort_order INTEGER DEFAULT 0,  -- 显示顺序
    data_scope CHAR(1) DEFAULT '1',  -- 数据范围：1-全部数据，2-自定义，3-本部门，4-本部门及以下，5-仅本人
    status CHAR(1) DEFAULT '1',  -- 角色状态：1-启用，0-禁用
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (role_id)
);

-- 创建索引
CREATE INDEX idx_role_tenant_id ON sys_role(tenant_id);
CREATE INDEX idx_role_status ON sys_role(status);

-- 添加表注释
COMMENT ON TABLE sys_role IS '角色信息表';
COMMENT ON COLUMN sys_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_key IS '角色权限字符串';

-- =============================================
-- 权限信息表
-- 说明: 存储系统权限信息，支持菜单和按钮权限
-- =============================================
DROP TABLE IF EXISTS sys_permission CASCADE;
CREATE TABLE sys_permission (
    permission_id BIGINT NOT NULL,  -- 权限唯一标识
    parent_id BIGINT DEFAULT 0,  -- 父权限ID，0表示顶级权限
    permission_name VARCHAR(50) NOT NULL,  -- 权限名称
    permission_type CHAR(1) DEFAULT 'M',  -- 权限类型：M-目录，C-菜单，F-按钮
    perms VARCHAR(100),  -- 权限标识字符串
    path VARCHAR(200),  -- 路由地址
    component VARCHAR(255),  -- 组件路径
    icon VARCHAR(100),  -- 图标
    sort_order INTEGER DEFAULT 0,  -- 显示顺序
    visible CHAR(1) DEFAULT '1',  -- 是否可见：1-显示，0-隐藏
    is_frame CHAR(1) DEFAULT '0',  -- 是否外链：0-否，1-是
    is_cache CHAR(1) DEFAULT '1',  -- 是否缓存：0-不缓存，1-缓存
    status CHAR(1) DEFAULT '1',  -- 权限状态：1-启用，0-禁用
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (permission_id)
);

-- 创建索引
CREATE INDEX idx_permission_parent_id ON sys_permission(parent_id);
CREATE INDEX idx_permission_status ON sys_permission(status);

-- 添加表注释
COMMENT ON TABLE sys_permission IS '权限信息表';
COMMENT ON COLUMN sys_permission.permission_id IS '权限ID';
COMMENT ON COLUMN sys_permission.permission_name IS '权限名称';
COMMENT ON COLUMN sys_permission.permission_type IS '权限类型（M-目录，C-菜单，F-按钮）';

-- =============================================
-- 用户和角色关联表
-- 说明: 多对多关系，一个用户可以拥有多个角色
-- =============================================
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,  -- 用户ID
    role_id BIGINT NOT NULL,  -- 角色ID
    PRIMARY KEY (user_id, role_id)
);

-- 添加表注释
COMMENT ON TABLE sys_user_role IS '用户和角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

-- =============================================
-- 角色和权限关联表
-- 说明: 多对多关系，一个角色可以拥有多个权限
-- =============================================
DROP TABLE IF EXISTS sys_role_permission CASCADE;
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,  -- 角色ID
    permission_id BIGINT NOT NULL,  -- 权限ID
    PRIMARY KEY (role_id, permission_id)
);

-- 添加表注释
COMMENT ON TABLE sys_role_permission IS '角色和权限关联表';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';

-- =============================================
-- 系统配置表
-- 说明: 存储系统级别的配置参数
-- =============================================
DROP TABLE IF EXISTS sys_config CASCADE;
CREATE TABLE sys_config (
    config_id BIGINT NOT NULL,  -- 配置唯一标识
    config_name VARCHAR(100) NOT NULL,  -- 配置名称
    config_key VARCHAR(100) NOT NULL,  -- 配置键名
    config_value VARCHAR(500) NOT NULL,  -- 配置值
    config_type CHAR(1) DEFAULT 'N',  -- 是否系统内置：Y-是，N-否
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (config_id),
    CONSTRAINT uk_config_key UNIQUE (config_key)
);

-- 创建索引
CREATE INDEX idx_config_tenant_id ON sys_config(tenant_id);

-- 添加表注释
COMMENT ON TABLE sys_config IS '系统配置表';
COMMENT ON COLUMN sys_config.config_id IS '配置ID';
COMMENT ON COLUMN sys_config.config_name IS '配置名称';
COMMENT ON COLUMN sys_config.config_key IS '配置键名';
COMMENT ON COLUMN sys_config.config_value IS '配置值';

-- =============================================
-- 字典类型表
-- 说明: 存储字典类型信息
-- =============================================
DROP TABLE IF EXISTS sys_dict_type CASCADE;
CREATE TABLE sys_dict_type (
    dict_id BIGINT NOT NULL,  -- 字典类型唯一标识
    dict_name VARCHAR(100) NOT NULL,  -- 字典名称
    dict_type VARCHAR(100) NOT NULL,  -- 字典类型
    status CHAR(1) DEFAULT '1',  -- 状态：1-启用，0-禁用
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (dict_id),
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
);

-- 创建索引
CREATE INDEX idx_dict_type_tenant_id ON sys_dict_type(tenant_id);

-- 添加表注释
COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_id IS '字典ID';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型';

-- =============================================
-- 字典数据表
-- 说明: 存储字典数据项
-- =============================================
DROP TABLE IF EXISTS sys_dict_data CASCADE;
CREATE TABLE sys_dict_data (
    dict_code BIGINT NOT NULL,  -- 字典数据唯一标识
    dict_sort INTEGER DEFAULT 0,  -- 字典排序
    dict_label VARCHAR(100) NOT NULL,  -- 字典标签
    dict_value VARCHAR(100) NOT NULL,  -- 字典键值
    dict_type VARCHAR(100) NOT NULL,  -- 字典类型
    css_class VARCHAR(100),  -- 样式属性
    list_class VARCHAR(100),  -- 列表样式
    is_default CHAR(1) DEFAULT 'N',  -- 是否默认：Y-是，N-否
    status CHAR(1) DEFAULT '1',  -- 状态：1-启用，0-禁用
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (dict_code)
);

-- 创建索引
CREATE INDEX idx_dict_data_type ON sys_dict_data(dict_type);
CREATE INDEX idx_dict_data_tenant_id ON sys_dict_data(tenant_id);

-- 添加表注释
COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.dict_code IS '字典编码';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值';

-- =============================================
-- 操作日志记录表
-- 说明: 记录用户的操作行为，用于审计追踪
-- =============================================
DROP TABLE IF EXISTS sys_oper_log CASCADE;
CREATE TABLE sys_oper_log (
    oper_id BIGINT NOT NULL,  -- 日志唯一标识
    title VARCHAR(50) DEFAULT '',  -- 操作模块
    business_type INTEGER DEFAULT 0,  -- 业务类型：0-其它，1-新增，2-修改，3-删除
    method VARCHAR(100) DEFAULT '',  -- 方法名称
    request_method VARCHAR(10) DEFAULT '',  -- 请求方式
    operator_type INTEGER DEFAULT 0,  -- 操作类别：0-其它，1-后台用户，2-手机端用户
    oper_name VARCHAR(50) DEFAULT '',  -- 操作人员
    dept_name VARCHAR(50) DEFAULT '',  -- 部门名称
    oper_url VARCHAR(255) DEFAULT '',  -- 请求URL
    oper_ip VARCHAR(128) DEFAULT '',  -- 主机地址
    oper_location VARCHAR(255) DEFAULT '',  -- 操作地点
    oper_param VARCHAR(2000) DEFAULT '',  -- 请求参数
    json_result VARCHAR(2000) DEFAULT '',  -- 返回参数
    status INTEGER DEFAULT 0,  -- 操作状态：0-正常，1-异常
    error_msg VARCHAR(2000) DEFAULT '',  -- 错误消息
    oper_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 操作时间
    cost_time BIGINT DEFAULT 0,  -- 消耗时间（毫秒）
    tenant_id VARCHAR(64),  -- 所属租户ID
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (oper_id)
);

-- 创建索引
CREATE INDEX idx_oper_log_business_type ON sys_oper_log(business_type);
CREATE INDEX idx_oper_log_status ON sys_oper_log(status);
CREATE INDEX idx_oper_log_oper_time ON sys_oper_log(oper_time);
CREATE INDEX idx_oper_log_tenant_id ON sys_oper_log(tenant_id);

-- 添加表注释
COMMENT ON TABLE sys_oper_log IS '操作日志记录';
COMMENT ON COLUMN sys_oper_log.oper_id IS '日志主键';
COMMENT ON COLUMN sys_oper_log.title IS '模块标题';
COMMENT ON COLUMN sys_oper_log.business_type IS '业务类型（0其它 1新增 2修改 3删除）';

-- =============================================
-- 文件信息表
-- 说明: 存储上传文件的元数据信息
-- =============================================
DROP TABLE IF EXISTS sys_file CASCADE;
CREATE TABLE sys_file (
    file_id BIGINT NOT NULL,  -- 文件唯一标识
    file_name VARCHAR(100) NOT NULL,  -- 文件名称
    original_name VARCHAR(100),  -- 原始文件名
    file_path VARCHAR(255) NOT NULL,  -- 文件存储路径
    file_url VARCHAR(500),  -- 文件访问URL
    file_size BIGINT DEFAULT 0,  -- 文件大小（字节）
    file_type VARCHAR(50),  -- 文件类型
    mime_type VARCHAR(100),  -- MIME类型
    file_md5 VARCHAR(32),  -- 文件MD5值
    storage_type VARCHAR(20) DEFAULT 'local',  -- 存储位置：local-本地，minio-MinIO，oss-阿里云OSS
    is_image CHAR(1) DEFAULT '0',  -- 是否为图片：0-否，1-是
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (file_id)
);

-- 创建索引
CREATE INDEX idx_file_md5 ON sys_file(file_md5);
CREATE INDEX idx_file_tenant_id ON sys_file(tenant_id);
CREATE INDEX idx_file_create_time ON sys_file(create_time);

-- 添加表注释
COMMENT ON TABLE sys_file IS '文件信息表';
COMMENT ON COLUMN sys_file.file_id IS '文件ID';
COMMENT ON COLUMN sys_file.file_name IS '文件名称';
COMMENT ON COLUMN sys_file.file_size IS '文件大小（字节）';

-- =============================================
-- 定时任务表
-- 说明: 存储系统定时任务配置信息
-- =============================================
DROP TABLE IF EXISTS sys_job CASCADE;
CREATE TABLE sys_job (
    job_id BIGINT NOT NULL,  -- 任务唯一标识
    job_name VARCHAR(64) NOT NULL DEFAULT '',  -- 任务名称
    job_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT',  -- 任务组名
    invoke_target VARCHAR(500) NOT NULL,  -- 调用目标字符串
    cron_expression VARCHAR(255) DEFAULT '',  -- cron执行表达式
    misfire_policy VARCHAR(20) DEFAULT '3',  -- 计划执行错误策略：1-立即执行，2-执行一次，3-放弃执行
    concurrent CHAR(1) DEFAULT '1',  -- 是否并发执行：0-禁止，1-允许
    status CHAR(1) DEFAULT '0',  -- 状态：0-暂停，1-正常
    next_execute_time TIMESTAMP,  -- 下次执行时间
    last_execute_time TIMESTAMP,  -- 上次执行时间
    tenant_id VARCHAR(64),  -- 所属租户ID
    remark VARCHAR(500) DEFAULT '',  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (job_id, job_name, job_group)
);

-- 创建索引
CREATE INDEX idx_job_status ON sys_job(status);
CREATE INDEX idx_job_tenant_id ON sys_job(tenant_id);

-- 添加表注释
COMMENT ON TABLE sys_job IS '定时任务调度表';
COMMENT ON COLUMN sys_job.job_id IS '任务ID';
COMMENT ON COLUMN sys_job.job_name IS '任务名称';
COMMENT ON COLUMN sys_job.cron_expression IS 'cron执行表达式';

-- =============================================
-- 代码生成业务表
-- 说明: 存储代码生成器的业务表配置信息
-- =============================================
DROP TABLE IF EXISTS gen_table CASCADE;
CREATE TABLE gen_table (
    table_id BIGINT NOT NULL,  -- 表唯一标识
    table_name VARCHAR(200) DEFAULT '',  -- 表名称
    table_comment VARCHAR(500) DEFAULT '',  -- 表描述
    class_name VARCHAR(100) DEFAULT '',  -- 实体类名称
    package_name VARCHAR(100),  -- 生成包路径
    module_name VARCHAR(30),  -- 生成模块名
    business_name VARCHAR(30),  -- 生成业务名
    function_name VARCHAR(50),  -- 生成功能名
    function_author VARCHAR(50),  -- 生成功能作者
    gen_type CHAR(1) DEFAULT '0',  -- 生成代码方式：0-zip压缩包，1-自定义路径
    gen_path VARCHAR(200) DEFAULT '/',  -- 生成路径（不填默认项目路径）
    pk_column VARCHAR(100),  -- 主键信息
    remark VARCHAR(500),  -- 备注信息
    create_by VARCHAR(64),  -- 创建者用户ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_by VARCHAR(64),  -- 更新者用户ID
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    deleted CHAR(1) DEFAULT '0',  -- 删除标识：0-正常，1-已删除
    PRIMARY KEY (table_id)
);

-- 添加表注释
COMMENT ON TABLE gen_table IS '代码生成业务表';
COMMENT ON COLUMN gen_table.table_id IS '编号';
COMMENT ON COLUMN gen_table.table_name IS '表名称';
COMMENT ON COLUMN gen_table.class_name IS '实体类名称';

-- =============================================
-- 创建触发器函数用于自动更新 update_time
-- 说明: PostgreSQL 需要手动创建触发器来实现类似 MySQL 的 ON UPDATE CURRENT_TIMESTAMP
-- =============================================
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为所有需要自动更新时间的表创建触发器
CREATE TRIGGER update_sys_tenant_modtime BEFORE UPDATE ON sys_tenant FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_dept_modtime BEFORE UPDATE ON sys_dept FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_user_modtime BEFORE UPDATE ON sys_user FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_role_modtime BEFORE UPDATE ON sys_role FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_permission_modtime BEFORE UPDATE ON sys_permission FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_config_modtime BEFORE UPDATE ON sys_config FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_dict_type_modtime BEFORE UPDATE ON sys_dict_type FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_dict_data_modtime BEFORE UPDATE ON sys_dict_data FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_oper_log_modtime BEFORE UPDATE ON sys_oper_log FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_file_modtime BEFORE UPDATE ON sys_file FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_sys_job_modtime BEFORE UPDATE ON sys_job FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_gen_table_modtime BEFORE UPDATE ON gen_table FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- =============================================
-- 脚本执行完成
-- =============================================
-- 说明: 
-- 1. 本脚本已将 MySQL 语法转换为 PostgreSQL 语法
-- 2. 数据类型映射: 
--    - TINYINT -> SMALLINT
--    - INT(11) -> INTEGER
--    - BIGINT(20) -> BIGINT
--    - DATETIME -> TIMESTAMP
--    - CHAR/VARCHAR 保持不变
-- 3. 索引创建使用 CREATE INDEX 语句
-- 4. 注释使用 COMMENT ON 语句
-- 5. 自动更新时间使用触发器实现
-- =============================================
