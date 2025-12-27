-- =============================================
-- 动态字典系统数据库架构（PostgreSQL版）
-- 作者: mumu
-- 描述: 统一的动态字典管理系统，支持类型查询和数据查询分离
-- 版本: 1.0.0
-- 时间: 2025-12-27
-- =============================================

-- =============================================
-- 字典类型表
-- =============================================
DROP TABLE IF EXISTS sys_dict_type CASCADE;
CREATE TABLE sys_dict_type (
    dict_id BIGSERIAL PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_type VARCHAR(100) NOT NULL,
    dict_category VARCHAR(50) DEFAULT 'system',
    status CHAR(1) DEFAULT '1',
    remark VARCHAR(500),
    tenant_id VARCHAR(64),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0',
    CONSTRAINT uk_dict_type UNIQUE (dict_type, tenant_id, deleted)
);

CREATE INDEX idx_dict_category ON sys_dict_type(dict_category);
CREATE INDEX idx_dict_status ON sys_dict_type(status);
CREATE INDEX idx_dict_tenant ON sys_dict_type(tenant_id);

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_id IS '字典主键';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_type.dict_category IS '字典分类（system-系统字典，business-业务字典，custom-自定义字典）';
COMMENT ON COLUMN sys_dict_type.status IS '状态（1-启用，0-禁用）';
COMMENT ON COLUMN sys_dict_type.tenant_id IS '租户ID（NULL表示通用字典）';

-- =============================================
-- 字典数据表
-- =============================================
DROP TABLE IF EXISTS sys_dict_data CASCADE;
CREATE TABLE sys_dict_data (
    data_id BIGSERIAL PRIMARY KEY,
    dict_type VARCHAR(100) NOT NULL,
    dict_label VARCHAR(100) NOT NULL,
    dict_value VARCHAR(100) NOT NULL,
    dict_desc VARCHAR(500),
    dict_sort INTEGER DEFAULT 0,
    css_class VARCHAR(100),
    list_class VARCHAR(100),
    is_default CHAR(1) DEFAULT '0',
    status CHAR(1) DEFAULT '1',
    extra_data TEXT,
    tenant_id VARCHAR(64),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_dict_data_type ON sys_dict_data(dict_type);
CREATE INDEX idx_dict_data_sort ON sys_dict_data(dict_sort);
CREATE INDEX idx_dict_data_status ON sys_dict_data(status);
CREATE INDEX idx_dict_data_tenant ON sys_dict_data(tenant_id);

COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.data_id IS '字典数据主键';
COMMENT ON COLUMN sys_dict_data.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值';
COMMENT ON COLUMN sys_dict_data.extra_data IS '扩展数据（JSON格式）';

-- =============================================
-- 行政区划表（树形结构）
-- =============================================
DROP TABLE IF EXISTS sys_region CASCADE;
CREATE TABLE sys_region (
    region_id BIGSERIAL PRIMARY KEY,
    region_code VARCHAR(20) NOT NULL,
    region_name VARCHAR(100) NOT NULL,
    parent_code VARCHAR(20),
    region_level INTEGER DEFAULT 1,
    region_type VARCHAR(20),
    short_name VARCHAR(50),
    pinyin VARCHAR(100),
    pinyin_abbr VARCHAR(20),
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6),
    sort_order INTEGER DEFAULT 0,
    is_municipality CHAR(1) DEFAULT '0',
    status CHAR(1) DEFAULT '1',
    remark VARCHAR(500),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0',
    CONSTRAINT uk_region_code UNIQUE (region_code, deleted)
);

CREATE INDEX idx_region_parent ON sys_region(parent_code);
CREATE INDEX idx_region_level ON sys_region(region_level);
CREATE INDEX idx_region_type ON sys_region(region_type);
CREATE INDEX idx_region_status ON sys_region(status);

COMMENT ON TABLE sys_region IS '行政区划表';
COMMENT ON COLUMN sys_region.region_code IS '区划代码（GB/T 2260标准6位代码）';
COMMENT ON COLUMN sys_region.region_name IS '区划名称';
COMMENT ON COLUMN sys_region.region_level IS '区划层级（1-省级，2-市级，3-区县级，4-街道级）';

-- =============================================
-- 初始化字典类型数据
-- =============================================
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, dict_category, status, remark) VALUES
(1, '性别', 'gender', 'system', '1', 'GB/T 2261.1-2003 标准'),
(2, '民族', 'ethnicity', 'system', '1', 'GB/T 3304-1991 标准，56个民族'),
(3, '学历', 'education', 'system', '1', 'GB/T 4658-2006 标准'),
(4, '婚姻状况', 'marital', 'system', '1', 'GB/T 2261.2-2003 标准'),
(5, '政治面貌', 'political', 'system', '1', 'GB/T 4762-1984 标准'),
(6, '证件类型', 'idtype', 'system', '1', 'GB/T 2261.4 标准'),
(7, '用户状态', 'user_status', 'system', '1', '用户账号状态');

-- 设置序列值
SELECT setval('sys_dict_type_dict_id_seq', 7, true);

-- =============================================
-- 初始化性别字典数据
-- =============================================
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, status) VALUES
('gender', '未知的性别', '0', 'GB/T 2261.1-2003标准', 1, '1'),
('gender', '男性', '1', 'GB/T 2261.1-2003标准', 2, '1'),
('gender', '女性', '2', 'GB/T 2261.1-2003标准', 3, '1'),
('gender', '未说明的性别', '9', 'GB/T 2261.1-2003标准', 4, '1');

-- =============================================
-- 初始化用户状态字典数据
-- =============================================
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_desc, dict_sort, list_class, status) VALUES
('user_status', '启用', '1', '用户账号启用状态', 1, 'success', '1'),
('user_status', '禁用', '0', '用户账号禁用状态', 2, 'danger', '1');

-- 注：其他字典数据（民族、学历、婚姻、政治面貌、证件类型）请参考MySQL版本的SQL脚本
